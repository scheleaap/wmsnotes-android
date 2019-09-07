package info.maaskant.wmsnotes.android.app.upgrades

import android.content.res.Resources
import com.f2prateek.rx.preferences2.Preference
import info.maaskant.wmsnotes.R
import info.maaskant.wmsnotes.android.app.PreferencesModule.ServerHostname
import info.maaskant.wmsnotes.android.app.PreferencesModule.SynchronizationEnabled
import info.maaskant.wmsnotes.utilities.logger
import javax.inject.Inject

class ServerHostnameUpgrade @Inject constructor(
    @ServerHostname private val serverHostnamePreference: Preference<String>,
    @SynchronizationEnabled private val synchronizationEnabledPreference: Preference<Boolean>,
    val resources: Resources
) {
    private val logger by logger()

    fun run() {
        val newDefaultValue = resources.getString(R.string.pref_server_hostname_default)

        val upgrade: Upgrade =
            calculateUpgrade(synchronizationEnabledPreference, serverHostnamePreference, newDefaultValue)

        if (upgrade.enabledChange != null) {
            logger.info(
                "Upgrade: Changed synchronization enabled from {} to {}",
                upgrade.enabledBefore,
                upgrade.enabledChange
            )
            synchronizationEnabledPreference.set(upgrade.enabledChange)
        }
        if (upgrade.hostnameChange != null) {
            logger.info(
                "Upgrade: Changed server hostname from {} to {}",
                upgrade.hostnameBefore,
                upgrade.hostnameChange
            )
            serverHostnamePreference.set(upgrade.hostnameChange)
        }
    }

    private fun calculateUpgrade(
        synchronizationEnabledPreference: Preference<Boolean>,
        serverHostnamePreference: Preference<String>,
        newDefaultValue: String
    ): Upgrade {
        return if (!synchronizationEnabledPreference.isSet) {
            if (serverHostnamePreference.isSet) {
                val oldValue = serverHostnamePreference.get()
                if (oldValue.isBlank() || oldValue in setOf(LOCALHOST, ANDROID_EMULATOR_HOST_IP)) {
                    Upgrade(
                        enabledBefore = NOT_SET,
                        hostnameBefore = oldValue,
                        enabledChange = false,
                        hostnameChange = newDefaultValue
                    )
                } else {
                    Upgrade(
                        enabledBefore = NOT_SET,
                        hostnameBefore = oldValue,
                        enabledChange = true,
                        hostnameChange = NO_CHANGE
                    )
                }
            } else {
                Upgrade(
                    enabledBefore = NOT_SET,
                    hostnameBefore = NOT_SET,
                    enabledChange = false,
                    hostnameChange = newDefaultValue
                )
            }
        } else {
            Upgrade(NOT_SET, NOT_SET, NO_CHANGE, NO_CHANGE)
        }
    }

    companion object {
        private const val LOCALHOST = "localhost"
        private const val ANDROID_EMULATOR_HOST_IP = "10.0.2.2"
        private val NOT_SET = null
        private val NO_CHANGE = null
    }

    data class Upgrade(
        val enabledBefore: Boolean?,
        val hostnameBefore: String?,
        val enabledChange: Boolean?,
        val hostnameChange: String?
    )
}

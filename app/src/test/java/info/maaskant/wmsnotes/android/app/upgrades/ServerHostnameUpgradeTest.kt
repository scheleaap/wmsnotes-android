package info.maaskant.wmsnotes.android.app.upgrades

import android.content.res.Resources
import com.f2prateek.rx.preferences2.Preference
import info.maaskant.wmsnotes.R
import io.mockk.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

internal class ServerHostnameUpgradeTest {
private    val blankHostname = "  "
    private val nonBlankHostname = "hostname"
    private val defaultHostname = "default hostname"

    data class Fixture(
        val enabledBefore: Boolean?,
        val hostnameBefore: String?,
        val enabledChange: Boolean?,
        val hostnameChange: String?
    )

    @TestFactory
    fun test(): List<DynamicTest> {
        val notSet = null
        val noChange = null
        val items = listOf(
            Fixture(enabledBefore = notSet, hostnameBefore = notSet, enabledChange = false, hostnameChange = defaultHostname),
            Fixture(enabledBefore = true, hostnameBefore = nonBlankHostname, enabledChange = noChange, hostnameChange = noChange),
            Fixture(enabledBefore = true, hostnameBefore = blankHostname, enabledChange = noChange, hostnameChange = noChange),
            Fixture(enabledBefore = notSet, hostnameBefore = nonBlankHostname, enabledChange = true, hostnameChange = noChange),
            Fixture(enabledBefore = notSet, hostnameBefore = blankHostname, enabledChange = false, hostnameChange = defaultHostname),
            Fixture(enabledBefore = notSet, hostnameBefore = "10.0.2.2", enabledChange = false, hostnameChange = defaultHostname),
            Fixture(enabledBefore = notSet, hostnameBefore = "localhost", enabledChange = false, hostnameChange = defaultHostname)
        )
        return items.map { fixture ->
            DynamicTest.dynamicTest(fixture.toString()) {
                // Given
                val enabled: Preference<Boolean> = givenEnabled(fixture.enabledBefore)
                val hostname: Preference<String> = givenHostname(fixture.hostnameBefore)
                val resources: Resources = givenResources()
                val upgrade = ServerHostnameUpgrade(hostname, enabled, resources)

                // When
                upgrade.run()

                // Then
                if (fixture.enabledChange != null) {
                    enabledWasChanged(enabled, fixture.enabledChange)
                } else {
                    enabledWasNotChanged(enabled)
                }
                if (fixture.hostnameChange != null) {
                    hostnameWasChanged(hostname, fixture.hostnameChange)
                } else {
                    hostnameWasNotChanged(hostname)
                }
            }
        }
    }

    private fun enabledWasChanged(enabled: Preference<Boolean>, newValue: Boolean) {
        verify(exactly = 1) {
            enabled.set(newValue)
        }
    }

    private fun enabledWasNotChanged(enabled: Preference<Boolean>) {
        verify(exactly = 0) {
            enabled.set(any())
        }
    }

    private fun givenResources(): Resources =
        mockk {
            every { getString(R.string.pref_server_hostname_default) } returns defaultHostname
        }

    private fun givenEnabled(enabled: Boolean?): Preference<Boolean> = mockk {
        every { isSet() } returns (enabled != null)
        every { get() } returns (enabled ?: false)
        every { set(any()) } just Runs
    }

    private fun givenHostname(hostname: String?): Preference<String> = mockk {
        every { isSet() } returns (hostname != null)
        every { get() } returns (hostname ?: "")
        every { set(any()) } just Runs
    }

    private fun hostnameWasChanged(hostname: Preference<String>, newValue: String) {
        verify(exactly = 1) {
            hostname.set(newValue)
        }
    }

    private fun hostnameWasNotChanged(hostname: Preference<String>) {
        verify(exactly = 0) {
            hostname.set(any())
        }
    }
}

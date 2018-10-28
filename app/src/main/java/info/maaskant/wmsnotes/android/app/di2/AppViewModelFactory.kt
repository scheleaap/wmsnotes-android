/**
 * Source: https://medium.com/chili-labs/android-viewmodel-injection-with-dagger-f0061d3402ff
 * Source: https://github.com/chili-android/viewmodel-dagger-example
 */
package info.maaskant.wmsnotes.android.app.di2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Provider

class AppViewModelFactory(
    private val providers: Map<Class<out ViewModel>, Provider<ViewModel>>
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        requireNotNull(getProvider(modelClass).get()) {
            "Provider for $modelClass returned null"
        }

    @Suppress("UNCHECKED_CAST")
    private fun <T : ViewModel> getProvider(modelClass: Class<T>): Provider<T> =
        try {
            requireNotNull(providers[modelClass] as Provider<T>) {
                "No ViewModel provider is bound for class $modelClass"
            }
        } catch (cce: ClassCastException) {
            error("Wrong provider type registered for ViewModel type $modelClass")
        }

}

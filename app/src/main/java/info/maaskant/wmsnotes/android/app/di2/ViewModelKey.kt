/**
 * Source: https://medium.com/chili-labs/android-viewmodel-injection-with-dagger-f0061d3402ff
 * Source: https://github.com/chili-android/viewmodel-dagger-example
 */
package info.maaskant.wmsnotes.android.app.di2

import androidx.lifecycle.ViewModel
import dagger.MapKey
import kotlin.reflect.KClass

@MapKey
@Target(AnnotationTarget.FUNCTION)
annotation class ViewModelKey(
    val value: KClass<out ViewModel>
)

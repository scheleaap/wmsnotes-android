package info.maaskant.wmsnotes.android.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import info.maaskant.wmsnotes.android.ui.di.DefaultFragmentFactory

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        DefaultFragmentFactory.setUp(this)
        super.onCreate(savedInstanceState)

        supportFragmentManager
            .beginTransaction()
            .replace(
                android.R.id.content, supportFragmentManager.fragmentFactory.instantiate(
                    classLoader,
                    SettingsFragment::class.java.name
                )
            )
            .commit()
    }
}

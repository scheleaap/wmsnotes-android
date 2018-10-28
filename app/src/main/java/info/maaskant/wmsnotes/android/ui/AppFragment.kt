package info.maaskant.wmsnotes.android.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import dagger.android.support.AndroidSupportInjection

abstract class AppFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }
}

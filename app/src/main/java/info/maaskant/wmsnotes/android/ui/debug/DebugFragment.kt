package info.maaskant.wmsnotes.android.ui.debug

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import dagger.android.support.AndroidSupportInjection
import info.maaskant.wmsnotes.R
import javax.inject.Inject

class DebugFragment : Fragment() {

    @Inject
    lateinit var viewModel: DebugViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.debug_fragment, container, false).apply {
            findViewById<Button>(R.id.create_test_note).setOnClickListener { viewModel.createTestNote() }
            findViewById<ImageView>(R.id.image).apply {
                Glide.with(requireContext())
                    .load("https://images.ctfassets.net/5jh3ceokw2vz/3ebN9YM0qF9aJde5qwXWJt/11ebd1e832f65d54cbb725f17562cab8/Agnes_067-2.jpg")
                    .into(this)
            }
        }
    }
}

package info.maaskant.wmsnotes.android.ui.debug

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import info.maaskant.wmsnotes.R
import javax.inject.Inject

@AndroidEntryPoint
class DebugFragment @Inject constructor() : Fragment() {

    private val viewModel: DebugViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.debug_fragment, container, false).apply {
            findViewById<TextView>(R.id.debug_text_view).text = viewModel.createText()
            findViewById<Button>(R.id.debug_create_test_note).setOnClickListener { viewModel.createTestNote() }
            findViewById<ImageView>(R.id.debug_image).apply {
                Glide.with(requireContext())
                    .load("https://images.ctfassets.net/5jh3ceokw2vz/3ebN9YM0qF9aJde5qwXWJt/11ebd1e832f65d54cbb725f17562cab8/Agnes_067-2.jpg")
                    .into(this)
            }
        }
    }
}

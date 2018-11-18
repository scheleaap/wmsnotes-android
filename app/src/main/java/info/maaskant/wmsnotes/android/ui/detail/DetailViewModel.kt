package info.maaskant.wmsnotes.android.ui.detail

import androidx.lifecycle.ViewModel
import info.maaskant.wmsnotes.model.projection.NoteProjector
import javax.inject.Inject

class DetailViewModel @Inject constructor(private val noteProjector: NoteProjector) : ViewModel() {
}

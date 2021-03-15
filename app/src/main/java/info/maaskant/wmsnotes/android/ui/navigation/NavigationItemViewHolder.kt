package info.maaskant.wmsnotes.android.ui.navigation

import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import info.maaskant.wmsnotes.R
import info.maaskant.wmsnotes.utilities.logger

internal class NavigationItemViewHolder(
    view: View,
    private val listener: NavigationListAdapter.NavigationListAdapterListener
) :
    RecyclerView.ViewHolder(view),
    View.OnClickListener,
    View.OnLongClickListener {
    private val logger by logger()

    lateinit var navigationItem: NavigationItem
    val main: LinearLayout = view.findViewById(R.id.navigation_item)
    val iconFront: RelativeLayout = view.findViewById(R.id.icon_front)
    val iconFrontIcon: ImageView = view.findViewById(R.id.icon_front_icon)
    val iconBack: RelativeLayout = view.findViewById(R.id.icon_back)
    val title: TextView = view.findViewById(R.id.title)

    init {
        view.setOnClickListener(this)
        view.setOnLongClickListener(this)
    }

    fun bind(navigationItem: NavigationItem) {
        logger.trace("Binding (old: ${if (this::navigationItem.isInitialized) this.navigationItem else null}, new: $navigationItem)")
        this.navigationItem = navigationItem
        main.isActivated = navigationItem.isSelected
        when (navigationItem) {
            is Note -> {
                title.text = navigationItem.title
                iconFrontIcon.setImageDrawable(
                    IconicsDrawable(
                        iconFrontIcon.context,
                        GoogleMaterial.Icon.gmd_insert_drive_file
                    )
                )
            }
            is Folder -> {
                title.text = navigationItem.title
                iconFrontIcon.setImageDrawable(
                    IconicsDrawable(
                        iconFrontIcon.context,
                        GoogleMaterial.Icon.gmd_folder
                    )
                )
            }
        }
    }

    override fun onClick(view: View) {
        listener.onClick(navigationItem)
    }

    override fun onLongClick(view: View): Boolean {
        val consumed = listener.onLongClick(navigationItem)
        if (consumed) {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
        return consumed
    }
}

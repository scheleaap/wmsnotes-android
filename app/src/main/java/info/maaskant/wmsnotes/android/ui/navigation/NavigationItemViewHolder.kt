package info.maaskant.wmsnotes.android.ui.navigation

import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import info.maaskant.wmsnotes.R

internal class NavigationItemViewHolder(
    view: View,
    private val listener: NavigationListAdapter.NavigationListAdapterListener
) :
    RecyclerView.ViewHolder(view),
    View.OnClickListener,
    View.OnLongClickListener {
    private lateinit var navigationItem: NavigationItem
    private val main: LinearLayout = view.findViewById(R.id.navigation_item)
    private val icon: ImageView = view.findViewById(R.id.icon)
    private val title: TextView = view.findViewById(R.id.title)

    init {
        view.setOnClickListener(this)
        view.setOnLongClickListener(this)
    }

    fun bind(navigationItem: NavigationItem) {
        this.navigationItem = navigationItem
        main.isActivated = navigationItem.isSelected
        when (navigationItem) {
            is Note -> {
                title.text = navigationItem.title
                icon.setImageDrawable(
                    IconicsDrawable(
                        icon.context,
                        GoogleMaterial.Icon.gmd_insert_drive_file
                    )
                )
            }
            is Folder -> {
                title.text = navigationItem.title
                icon.setImageDrawable(
                    IconicsDrawable(
                        icon.context,
                        GoogleMaterial.Icon.gmd_folder
                    )
                )
            }
        }
    }

    fun setSelection(isSelected: Boolean) {
        main.isActivated = isSelected
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

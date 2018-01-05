package info.maaskant.wmsnotes.detail;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class DetailPagerAdapter extends FragmentPagerAdapter {

    private final EditorFragment editorFragment;
    private final ViewerFragment viewerFragment;

    DetailPagerAdapter(FragmentManager fragmentManager, EditorFragment editorFragment,
            ViewerFragment viewerFragment) {
        super(fragmentManager);
        this.editorFragment = editorFragment;
        this.viewerFragment = viewerFragment;
    }

    @Override
    public Fragment getItem(int position) {
        if (isEditorPage(position)) {
            return editorFragment;
        } else {
            return viewerFragment;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    boolean isEditorPage(int position) {
        return position == 1;
    }

    boolean isViewerPage(int position) {
        return position != 1;
    }

}

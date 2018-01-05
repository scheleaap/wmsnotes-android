package info.maaskant.wouttest2;

import java.lang.reflect.Field;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class MemoryUtil {

    /**
     * fixInputMethod
     *
     * Copied from https://issuetracker.google.com/issues/37043700#comment17
     *
     * @author androidmalin
     * @param context
     *            Context
     */
    public static void fixInputMethod(Context context) {
        if (context == null)
            return;
        InputMethodManager inputMethodManager = null;
        try {
            inputMethodManager = (InputMethodManager) context.getApplicationContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
        } catch (Throwable th) {
            th.printStackTrace();
        }
        if (inputMethodManager == null)
            return;
        String[] strArr = new String[] { "mCurRootView", "mServedView", "mNextServedView" };
        for (int i = 0; i < 3; i++) {
            try {
                Field declaredField = inputMethodManager.getClass().getDeclaredField(strArr[i]);
                if (declaredField == null)
                    continue;
                if (!declaredField.isAccessible()) {
                    declaredField.setAccessible(true);
                }
                Object obj = declaredField.get(inputMethodManager);
                if (obj == null || !(obj instanceof View))
                    continue;
                View view = (View) obj;
                if (view.getContext() == context) {
                    declaredField.set(inputMethodManager, null);
                } else {
                    return;
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

}

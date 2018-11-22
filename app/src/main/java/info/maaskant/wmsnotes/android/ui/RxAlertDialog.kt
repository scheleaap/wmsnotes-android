package info.maaskant.wmsnotes.android.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import io.reactivex.Observable

/**
 * A lightweight version of (and inspired by) https://github.com/xavierlepretre/rx-alert-dialog
 * Modified for RxJava2 from https://gist.github.com/sambroad/7fbcc6b54cadd2224d5c58c3441826c8
 *
 * Usage:
 *  observable
 *      .observeOn(AndroidSchedulers.mainThread())
 *      .flatMap({ event ->
 *          RxAlertDialog.show(context, R.string.title, R.string.message, android.R.string.ok)
 *      })
 *      .subscribe({ button ->
 *          when (button) {
 *              RxAlertDialog.BUTTON_POSITIVE -> Log.v("OK")
 *              RxAlertDialog.BUTTON_NEGATIVE-> Log.v("Cancel")
 *              RxAlertDialog.DISMISS_ALERT-> Log.v("Dismissed")
 *          }
 *     })
 *
 */
class RxAlertDialog {
    enum class Event {
        CANCEL_ALERT,
        DISMISS_ALERT,
        BUTTON_POSITIVE,
        BUTTON_NEGATIVE,
        BUTTON_NEUTRAL,
    }

    companion object {

        @JvmStatic
        @JvmOverloads
        fun show(
            context: Context,
            @StringRes title: Int? = null,
            @StringRes message: Int? = null,
            @StringRes positiveButton: Int? = null,
            @StringRes negativeButton: Int? = null,
            @StringRes neutralButton: Int? = null
        ): Observable<Event> {

            return Observable.create<Event> { emitter ->
                val builder = AlertDialog.Builder(context)
                    .setOnDismissListener { emitter.onNext(Event.DISMISS_ALERT) }
                    .setOnCancelListener { emitter.onNext(Event.CANCEL_ALERT) }

                title?.let { builder.setTitle(it) }
                message?.let { builder.setMessage(it) }
                positiveButton?.let { builder.setPositiveButton(positiveButton) { _, _ -> emitter.onNext(Event.BUTTON_POSITIVE) } }
                negativeButton?.let { builder.setNegativeButton(negativeButton) { _, _ -> emitter.onNext(Event.BUTTON_NEGATIVE) } }
                neutralButton?.let { builder.setNeutralButton(neutralButton) { _, _ -> emitter.onNext(Event.BUTTON_NEUTRAL) } }
                val dialog = builder.show()

                emitter.setCancellable(dialog::dismiss)
            }
        }
    }
}

//package info.maaskant.wmsnotes.android.ui.detail
//
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.Observer
//
//class LiveDataTestObserver<T>(liveData: LiveData<T>) : Observer<T> {
//    private val values = mutableListOf<T>()
//
//    init {
//        liveData.observeForever(this)
//    }
//
//    /**
//     * Called when the data is changed.
//     * @param t  The new data
//     */
//    override fun onChanged(t: T) {
//        values.add(t)
//    }
//
//    fun values(): List<T> {
//        return values
//    }
//}
//
//fun <T> LiveData<T>.test(): LiveDataTestObserver<T> {
//    return LiveDataTestObserver(this)
//}

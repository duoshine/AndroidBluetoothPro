package duoshine.rxandroidbluetooth.observable

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

/**
 *Created by chen on 2019
 */
class CharacteristicOnceObservable private constructor(private val once: ByteArray) :
    Observable<Response>() {

    override fun subscribeActual(observer: Observer<in Response>?) {
        val onceObserver = CharacteristicOnceObserver(observer)
        observer?.onSubscribe(onceObserver)
        val upstream = BleGattCallbackObservable.get(onceObserver)
        upstream.writeOnce(once)
    }

    companion object {
        fun create(once: ByteArray): CharacteristicOnceObservable {
            return CharacteristicOnceObservable(once)
        }
    }

    private class CharacteristicOnceObserver(private val observer: Observer<in Response>?) : Observer<Response>,
        Disposable {
        private val tag: String = "duo_shine"

        private var upDisposable: Disposable? = null


        override fun isDisposed(): Boolean {
            return upDisposable?.isDisposed ?: false
        }

        override fun dispose() {
            upDisposable?.dispose()
        }

        override fun onComplete() {
            observer?.onComplete()
        }

        override fun onSubscribe(d: Disposable) {
            upDisposable = d
        }

        override fun onNext(t: Response) {
            observer?.onNext(t)
        }

        override fun onError(e: Throwable) {
            observer?.onError(e)
        }
    }
}
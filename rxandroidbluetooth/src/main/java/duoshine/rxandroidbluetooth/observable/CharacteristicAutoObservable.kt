package duoshine.rxandroidbluetooth.observable

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

/**
 *Created by chen on 2019
 */
class CharacteristicAutoObservable private constructor(private val more: MutableList<ByteArray>) :
    Observable<Response>() {

    override fun subscribeActual(observer: Observer<in Response>?) {
        val autoObserver = CharacteristicAutoObserver(observer)
        observer?.onSubscribe(autoObserver)
        val upstream = BleGattCallbackObservable.get(autoObserver)
        upstream.writeAutoCharacteristic(more)
    }

    companion object {

        fun create(more: MutableList<ByteArray>): CharacteristicAutoObservable {
            return CharacteristicAutoObservable(more)
        }
    }

    private class CharacteristicAutoObserver(private val observer: Observer<in Response>?) : Observer<Response>,
        Disposable {
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
package duoshine.rxandroidbluetooth.observable

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

/**
 *Created by chen on 2019
 */
class CharacteristicNextObservable private constructor(private val more: MutableList<ByteArray>) :
    Observable<Response>() {
    /**
     * function
     * ByteArray：远程设备的返回包 由外部来处理
     * Boolean：是否继续发送 由外部来处理
     */
    private var function: io.reactivex.functions.Function<ByteArray, Int>? = null


    override fun subscribeActual(observer: Observer<in Response>?) {
        val nextObserver= CharacteristicNextObserver(observer)
        observer?.onSubscribe(nextObserver)
        val upstream = BleGattCallbackObservable.get(nextObserver)
        upstream.writeNextCharacteristic(more,function)
    }

    companion object {
        fun create(more: MutableList<ByteArray>): CharacteristicNextObservable {
            return CharacteristicNextObservable(more)
        }
    }

    /**
     *适用于每个写操作都有结果的多包指令 如果不需要检查每次的写操作结果 你可以使用 writeAuto() 而非writeNext
     *我们假设List(size = 3)如下：
     *byte1:01 02 03 04
     *byte2:01 02 03 04
     *byte3:01 02 03 04
     *
     *ByteArray：byte1的结果 由远程ble设备返回  框架内部不负责处理 通过apply方法回调出去 由框架调用者来判断byte1的结果
     *是否满足发送byte2的前提,满足则返回true  不满足返回False 如果返回false 本次指令终止 返回true继续发送byte2
     *Boolean：是否继续发送byte2
     */
    fun doOnNext(function: io.reactivex.functions.Function<ByteArray, Int>): CharacteristicNextObservable {
        this.function = function
        return this
    }

    private class CharacteristicNextObserver(private val observer: Observer<in Response>?) : Observer<Response>,
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
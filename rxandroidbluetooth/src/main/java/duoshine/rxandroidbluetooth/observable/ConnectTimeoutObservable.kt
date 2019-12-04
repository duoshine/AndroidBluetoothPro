package duoshine.rxandroidbluetooth.observable

import duoshine.rxandroidbluetooth.bluetoothprofile.BluetoothConnectProfile
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

/**
 *Created by chen on 2019
 */
class ConnectTimeoutObservable(
    private val source: Observable<Response>,
    private val time: Long,
    private val timeUnit: TimeUnit
) : Observable<Response>() {
    private val tag: String = "duo_shine"
    private var timeout: Disposable? = null

    override fun subscribeActual(observer: Observer<in Response>?) {
        val timeoutObserver = ConnectTimeoutObserver(observer)
        observer?.onSubscribe(timeoutObserver)
        source.subscribe(timeoutObserver)
        timeout = Observable
            .timer(time, timeUnit)
            .subscribe {
                //时间到后判断任务是否完成
                if (!timeoutObserver.isSucceed) {
                    timeoutObserver.onNext(Response(BluetoothConnectProfile.connectTimeout))
                }
            }
    }

    private class ConnectTimeoutObserver(private val observer: Observer<in Response>?) : Observer<Response>,
        Disposable {
        private var upstream: Disposable? = null
        private val tag: String = "duo_shine"


        /**
         * 是否成功
         */
        var isSucceed = false

        override fun isDisposed(): Boolean {
            return upstream?.isDisposed ?: false
        }

        override fun dispose() {
            upstream?.dispose()
        }

        override fun onComplete() {
            observer?.onComplete()
        }

        override fun onSubscribe(d: Disposable) {
            upstream = d
        }

        override fun onNext(t: Response) {
            observer?.onNext(t)
            when {
                t.code == BluetoothConnectProfile.connected -> isSucceed = true
                //超时执行dispose的目的是取消一个正在进行中的连接
                t.code == BluetoothConnectProfile.connectTimeout -> dispose()
                //防止有一种情况是没有连接成功 但是状态确给了连接断开(确实有) 既然连接任务已经不在继续了 就停止超时判断
                t.code == BluetoothConnectProfile.disconnected -> isSucceed = true
            }
        }

        override fun onError(e: Throwable) {
            observer?.onError(e)
        }
    }
}
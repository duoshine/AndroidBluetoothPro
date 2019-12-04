package duoshine.rxandroidbluetooth.observable

import duoshine.rxandroidbluetooth.util.ScanResult
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

/**
 *Created by chen on 2019
 */
 class ScanTimerObservable(
    private val source: Observable<ScanResult>,
    private val time: Long,
    private val timeUnit: TimeUnit
) : Observable<ScanResult>() {
    private val tag: String = "duo_shine"

    override fun subscribeActual(observer: Observer<in ScanResult>?) {
        val timerObserver = ScanTimerObserver(observer, time, timeUnit)
        source.subscribe(timerObserver)
        observer?.onSubscribe(timerObserver)
    }

    private class ScanTimerObserver(
        private val observer: Observer<in ScanResult>?,
        time: Long,
        timeUnit: TimeUnit
    ) : Observer<ScanResult>,
        Disposable {
        private var upstream: Disposable? = null
        private var d: Disposable? = null


        init {
            d = Observable
                .timer(time, timeUnit)
                .subscribe {
                    //时间到后任务完成
                    onComplete()
                    //停止扫描
                    dispose()
                }
        }

        override fun isDisposed(): Boolean {
            return false
        }

        override fun dispose() {
            cancel()
            upstream?.dispose()
        }

        override fun onComplete() {
            observer?.onComplete()
        }

        override fun onSubscribe(d: Disposable) {
            upstream = d
        }

        override fun onNext(t: ScanResult) {
            observer?.onNext(t)
        }

        override fun onError(e: Throwable) {
            cancel()
            observer?.onError(e)
        }

        /**
         * 取消定时任务
         */
        private fun cancel() {
            d?.let {
                if (!it.isDisposed) {
                    it.dispose()
                }
            }
        }
    }
}



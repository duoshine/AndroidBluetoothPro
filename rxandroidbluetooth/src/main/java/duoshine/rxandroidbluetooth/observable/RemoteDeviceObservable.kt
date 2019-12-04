package duoshine.rxandroidbluetooth.observable

import android.bluetooth.BluetoothDevice
import duoshine.rxandroidbluetooth.exception.DeviceNotFoundException
import io.reactivex.Observable
import io.reactivex.Observer

/**
 *Created by chen on 2019
 */
class RemoteDeviceObservable : Observable<BluetoothDevice>() {

    private val tag: String = "duo_shine"

    override fun subscribeActual(observer: Observer<in BluetoothDevice?>?) {
        val callback = BleGattCallbackObservable.get()
        val device = callback.getDevice()
        if (device == null) {
            observer?.onError(DeviceNotFoundException("device null"))
        } else {
            observer?.onNext(device)
            observer?.onComplete()
        }
    }

    companion object {
        fun create(): RemoteDeviceObservable {
            return RemoteDeviceObservable()
        }
    }
}
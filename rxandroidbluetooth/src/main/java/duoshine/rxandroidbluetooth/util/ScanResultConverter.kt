package duoshine.rxandroidbluetooth.util

import android.bluetooth.BluetoothDevice
import android.os.Build
import android.support.annotation.RequiresApi


/**
 *Created by chen on 2019
 */
class ScanResultConverter {

    companion object {
        private val tag: String = "duo_shine"
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        fun convert(callbackType: Int, result: android.bluetooth.le.ScanResult): ScanResult? {
            val device = result.device
            val record = result.scanRecord
            device?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    return ScanResult(
                        device,
                        result.isLegacy,
                        result.isConnectable,
                        result.dataStatus,
                        result.primaryPhy,
                        result.secondaryPhy,
                        result.advertisingSid,
                        result.txPower,
                        result.rssi,
                        result.periodicAdvertisingInterval,
                        ScanRecord(
                            record.serviceUuids,
                            record.manufacturerSpecificData,
                            record.serviceData,
                            record.advertiseFlags,
                            record.txPowerLevel,
                            record.deviceName,
                            record.bytes
                        ),
                        result.timestampNanos,
                        callbackType
                    )
                } else {
                    return ScanResult(
                        device,
                        result.rssi,
                        ScanRecord(
                            record.serviceUuids,
                            record.manufacturerSpecificData,
                            record.serviceData,
                            record.advertiseFlags,
                            record.txPowerLevel,
                            record.deviceName,
                            record.bytes
                        ),
                        result.timestampNanos,
                        callbackType
                    )
                }
            } ?: return null
        }

        fun convert0(device: BluetoothDevice?, rssi: Int, scanRecord: ByteArray?): ScanResult? {
            device?.let {
                return ScanResult(device, rssi, scanRecord)
            } ?: return null
        }
    }
}
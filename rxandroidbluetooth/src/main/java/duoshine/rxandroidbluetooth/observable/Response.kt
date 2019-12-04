package duoshine.rxandroidbluetooth.observable

/**
 *Created by chen on 2019
 */
data class Response(
    var code: Int,  //BluetoothNextProfile or BluetoothWriteProfile BluetoothConnectProfile,
    var data: ByteArray? = null//这可能是一个写入成功的数组 也可以是收到的新值 通过code来决定
)


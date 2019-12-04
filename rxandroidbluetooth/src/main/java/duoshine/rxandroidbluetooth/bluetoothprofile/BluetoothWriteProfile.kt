package duoshine.rxandroidbluetooth.bluetoothprofile

/**
 *Created by chen on 2019
 */
class BluetoothWriteProfile {
    companion object {
        //writeOnce 相关
        /**
         * 写入成功
         */
        val writeSucceed = 16

        /**
         * 写入失败
         */
        val writeFail = 17

        /**
         *  Characteristic变化 new值
         */
        val characteristicChanged = 18
    }
}
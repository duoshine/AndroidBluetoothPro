package duoshine.rxandroidbluetooth.bluetoothprofile

/**
 *Created by chen on 2019
 */
class BluetoothNextProfile {
    companion object {
        /**
         * 继续发送下一包
         */
        val next: Int = 9

        /**
         * 继续发送下一包但不拦截doOnNext 数据将会推到onNext  一般用于最后一包时返回
         */
        val nextAll: Int = 10

        /**
         * 终止  default
         */
        val termination: Int = 11

        /**
         * 重发
         */
        val retry: Int = 12
    }
}
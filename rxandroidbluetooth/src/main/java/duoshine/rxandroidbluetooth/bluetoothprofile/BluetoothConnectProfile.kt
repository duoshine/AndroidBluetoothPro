package duoshine.rxandroidbluetooth.bluetoothprofile
/**
 *Created by chen on 2019
 */
class BluetoothConnectProfile {
    companion object {
        /**
         * 连接成功
         */
        val connected = 1

        /**
         * 已断开连接
         */
        val disconnected = 2

        /**
         * 连接超时
         */
        val connectTimeout = 3

        /**
         * 启用通知特征成功
         */
        val enableNotifySucceed = 4

        /**
         * 启用通知特征失败
         */
        val enableNotifyFail = 5

        /**
         * 未获取到对应uuid的服务特征
         */
        val serviceNotfound = 6

        /**
         * 未获取到对应uuid的通知特征
         */
        val notifyNotFound = 7

        /**
         * 正在重连
         */
        val reconnection = 8
    }
}
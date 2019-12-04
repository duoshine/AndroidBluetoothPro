package duoshine.rxandroidbluetooth

import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import duoshine.rxandroidbluetooth.observable.*

/**
 *Created by chen on 2019
 */
interface BluetoothWorker {
    /**
     * scan
     * settings:scan mode 不传使用默认SCAN_MODE_LOW_LATENCY
     * filters:filter 不传则不过滤 过滤规则用调用者处理 这个规则它可能是： name? address? uuid?....
     *
     * note:此方法需要API>=21 否则s会提示找不到ScanSetting、ScanFilter这个类
     */
    fun startLeScan(
        settings: ScanSettings?,
        filters: MutableList<ScanFilter>?
    ): ScanLeObservable

    /**
     * scan
     * API小于21 调用此方法扫描  但是使用它你不需要考虑兼容性的问题 它的兼容性更好
     */
    fun startLeScan(): ScanLeObservable

    /**
     * writeOnce 单包
     * byteArray：需要写的数据 如果你想使用到重发等功能 同样可以使用 writeNext
     */
    fun writeOnce(byteArray: ByteArray): CharacteristicOnceObservable

    /**
     * writeAuto 多包 自动发送
     *
     *适用于不需要检查结果就能继续发送下一包的指令 规则是收到当前包的写入成功 继续发送下一包
     * 一般我不推荐使用该操作 为什么？
     * 原因是你知道远程ble设备他究竟有没有收到,只是Android端单方面认为写入成功 如果使用该方法 请做好测试
     * more：需要写的数据集合
     */
    fun writeAuto(more: MutableList<ByteArray>): CharacteristicAutoObservable

    /**
     * writeNext 多包 非自动发送  用户通过doOnNext决定是否发送下一包
     * 适用于多包指令 每包都需要检验结果才能发送下一包或上一包重发,它可以使用doOnNext来实现重发功能,但注意不是代码导致的错误重发
     * 这样可能会导致死循环发送
     * more：需要写的数据集合
     */
    fun writeNext(more: MutableList<ByteArray>): CharacteristicNextObservable

    /**
     * connect
     * address：远程设备mac地址
     */
    fun connect(address: String): ConnectObservable

    /**
     * 蓝牙是否启用
     * true为已打开
     */
    fun isEnabled(): Boolean

    /**
     * 获取gatt对应的远程设备(不处于连接中也可以调用)  这个设备可能是当前正在连接的设备或是上一次连接的设备
     * 这个方法在测试中发现并不是极稳定 address可以稳定获取 但是远程设备名称并不是很稳定，原因是此名称是蓝牙适配器在扫描时
     * 将会缓存远程设备名称,故可以获取到,反之如果没扫描过,也就是未缓存自然也就获取不到远程设备名称,使用时请做好判断
     */
    fun device(): RemoteDeviceObservable

    /**
     * 开启蓝牙
     * true表示已启动适配器
     */
    fun enable(): Boolean
}
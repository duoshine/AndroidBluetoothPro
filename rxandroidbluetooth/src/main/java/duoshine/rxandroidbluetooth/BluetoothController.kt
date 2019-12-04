package duoshine.rxandroidbluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import duoshine.rxandroidbluetooth.observable.*
import java.util.*

/**
 *Created by chen on 2019
 */
class BluetoothController private constructor(
    private val context: Context,
    /**
     * 服务uuid
     */
    private var serviceUuid: UUID?,
    /**
     * 通知uuid
     */
    private var notifyUuid: UUID?,
    /**
     * 写uuid
     */
    private var writeUuid: UUID?
) : BluetoothWorker {



    private var bluetoothAdapter: BluetoothAdapter? = null
    private val tag: String = "duo_shine"


    init {
        val mBluetoothManager = context
            .getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = mBluetoothManager.adapter
    }

    /**
     * 获取gatt对应的远程设备
     */
    override fun device(): RemoteDeviceObservable {
        return RemoteDeviceObservable.create()
    }

    /**
     *写操作 适用于不需要检查结果就能继续发送下一包的指令 规则是收到当前包的写入成功 继续发送下一包
     * 一般我不推荐使用该操作 为什么？
     * 原因是你知道远程ble设备他究竟有没有收到,只是Android端单方面认为写入成功 如果使用该方法 请做好测试
     */
    override fun writeAuto(more: MutableList<ByteArray>): CharacteristicAutoObservable {
        return CharacteristicAutoObservable.create(more)
    }

    /**
     *写操作 适用于多包指令 每包都需要检验结果才能发送下一包或上一包重发
     */
    override fun writeNext(more: MutableList<ByteArray>): CharacteristicNextObservable {
        return CharacteristicNextObservable.create(more)
    }

    /**
     *连接
     * address：mac地址
     */
    override fun connect(address: String): ConnectObservable {
        return ConnectObservable.create(context, bluetoothAdapter!!, address, serviceUuid, writeUuid, notifyUuid)
    }

    /**
     *写操作 适用于单包指令
     */
    override fun writeOnce(byteArray: ByteArray): CharacteristicOnceObservable {
        return CharacteristicOnceObservable.create(byteArray)
    }

    /**
     * 开启蓝牙
     */
    override fun enable(): Boolean = bluetoothAdapter?.enable() ?: false

    /**
     * 蓝牙是否启用
     */
    override fun isEnabled(): Boolean = bluetoothAdapter?.isEnabled ?: false

    /**
     * 扫描ble设备 注意每次扫描之前必须先停止之前的扫描 这个功能由用户控制
     * dispose来停止线程(扫描也会停止)
     * 如果没有停止上一次的扫描就开启本次扫描 上一次扫描将在扫描时间结束后停止扫描
     */
    override fun startLeScan(
        settings: ScanSettings?,
        filters: MutableList<ScanFilter>?
    ): ScanLeObservable {
        return ScanLeObservable.create(bluetoothAdapter, settings, filters)
    }

    /**
     * 扫描ble设备 注意每次扫描之前必须先停止之前的扫描 这个功能由用户控制
     * dispose来停止线程(扫描也会停止)
     * 如果没有停止上一次的扫描就开启本次扫描 上一次扫描将在扫描时间结束后停止扫描
     */
    override fun startLeScan(): ScanLeObservable {
        return ScanLeObservable.create(bluetoothAdapter, null, null)
    }

    class Builder constructor(private val context: Context) {
        /**
         * 服务uuid
         */
        private var serviceUuid: UUID? = null

        /**
         * 通知uuid
         */
        private var notifyUuid: UUID? = null

        /**
         * 写uuid
         */
        private var writeUuid: UUID? = null


        fun setServiceUuid(serviceUuid: UUID): Builder {
            this.serviceUuid = serviceUuid
            return this
        }

        fun setNotifyUuid(notifyUuid: UUID): Builder {
            this.notifyUuid = notifyUuid
            return this
        }

        fun setWriteUuid(writeUuid: UUID): Builder {
            this.writeUuid = writeUuid
            return this
        }

        fun build(): BluetoothController {
            return BluetoothController(context, serviceUuid, notifyUuid, writeUuid)
        }
    }
}
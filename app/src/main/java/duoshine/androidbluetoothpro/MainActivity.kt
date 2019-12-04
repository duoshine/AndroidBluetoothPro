package duoshine.androidbluetoothpro

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import duoshine.rxandroidbluetooth.BluetoothController
import duoshine.rxandroidbluetooth.BluetoothWorker
import duoshine.rxandroidbluetooth.bluetoothprofile.BluetoothConnectProfile
import duoshine.rxandroidbluetooth.bluetoothprofile.BluetoothNextProfile
import duoshine.rxandroidbluetooth.bluetoothprofile.BluetoothWriteProfile
import duoshine.rxandroidbluetooth.observable.Response
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 1.是不是所有的发送数据到 方法都支持重发等操作  ×
 * 2.异常创建对应的异常 这样用户可以定义对应的异常解决方案
 * 3.检查所有异常可能产生的地方 框架的严谨性  √
 * 4.内存泄漏?  √
 * 5.参数或者操作符乱使用 √
 * 6.考虑将collback的连接监听由外部传入? 用户估计会懵逼 ×
 * 7.断开自动连接功能  √
 * 8.返回当前gatt对应的远程设备  √
 * 9.支持心跳包指令-用户实现 使用rx举例
 * 10.考虑支持传输等级  当前版本不支持 ×
 * 11.考虑支持mtu扩展 当前版本不支持 ×
 */

class MainActivity : AppCompatActivity() {
    private val tag: String = "duo_shine"
    private var bluetoothController: BluetoothWorker? = null
    private var timer: Timer? = null
    private var timer2: Timer? = null

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        timer = Timer()
        timer2 = Timer()
        requestPermission()
        val serviceUUID = UUID.fromString("f000c0e0-0451-4000-b000-000000000000")
        val notifyUUID = UUID.fromString("f000c0e1-0451-4000-b000-000000000000")
        val writeUuid = UUID.fromString("f000c0e1-0451-4000-b000-000000000000")

        bluetoothController =
                BluetoothController.Builder(this)
                    .setNotifyUuid(notifyUUID)
                    .setServiceUuid(serviceUUID)
                    .setWriteUuid(writeUuid)
                    .build()
        //扫描  测试完毕 虽然在扫描中退出会导致停止扫描持有this无法释放 但是在停止扫描后 会释放
        startScan()
        //连接  测试完毕
        connect()
        //发送单包数据  测试完毕
        sendOnce()
        //发送多包数据  不需要根据回调决定是否发送下一包
        sendAutoMore()
        //发送多包数据 根据设备返回的指令决定是否发送下一包
        sendMore()
        //获取gatt对应的远程设备
        device()
        newConnect()
    }

    override fun onDestroy() {
        super.onDestroy()
        scanDispose?.dispose()
        connectDisposable?.dispose()
    }

    private fun device() {
        device.setOnClickListener {
            bluetoothController!!.device()
                .subscribe(
                    { Log.d(tag, "device:${it.name}") },
                    { error -> checkError(error) },
                    { Log.d(tag, "获取远程设备完成") }
                )
        }
    }

    private fun newConnect() {
        val byteArray = byteArrayOf(0x1D, 0x00, 0x00, 0xC6.toByte(), 0xE1.toByte(), 0x00)
        val list = mutableListOf(byteArray, byteArray, byteArray, byteArray, byteArray, byteArray, byteArray)
        newConnect.setOnClickListener {
            timer!!.schedule(object : TimerTask() {
                override fun run() {
                    while (true) {
                        Thread.sleep(20)
                        bluetoothController!!
                            .writeNext(list)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe { response ->
                                checkResult(response)
                            }
                    }
                }
            }, 20)

            timer2!!.schedule(object : TimerTask() {
                override fun run() {
                    while (true) {
                        Thread.sleep(20)
                        bluetoothController!!
                            .writeNext(list)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe { response ->
                                checkResult(response)
                            }
                    }
                }

            }, 20)
        }
    }

    private fun sendMore() {
        moreCallback.setOnClickListener {
            val byteArray = byteArrayOf(0x1D, 0x00, 0x00, 0xC6.toByte(), 0xE1.toByte(), 0x00)
            val list = mutableListOf(byteArray, byteArray, byteArray, byteArray, byteArray, byteArray, byteArray)
            bluetoothController!!
                .writeNext(list)
                .doOnNext(Function { byte ->
                    BluetoothNextProfile.next
                })
                .subscribe { response -> checkResult(response) }
        }
    }

    private fun sendAutoMore() {
        val byteArray = byteArrayOf(0x1D, 0x00, 0x00, 0xC6.toByte(), 0xE1.toByte(), 0x00)
        val list = mutableListOf(byteArray, byteArray, byteArray, byteArray, byteArray, byteArray, byteArray)
        more.setOnClickListener {
            bluetoothController!!
                .writeAuto(list)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { response -> checkResult(response) }
        }
    }

    private fun sendOnce() {
        send.setOnClickListener {
            val byteArray = byteArrayOf(0x1D, 0x00, 0x00, 0xC6.toByte(), 0xE1.toByte(), 0x00)
            bluetoothController!!
                .writeOnce(byteArray)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { response -> checkResult(response) },
                    { error -> checkError(error) }
                )
        }

        ceshi1.setOnClickListener {

        }
    }

    var scanDispose: Disposable? = null

    private fun startScan() {
        scanObservable.setOnClickListener {
            scanDispose = bluetoothController!!
                .startLeScan()
                .timer(6000, TimeUnit.MILLISECONDS)
                .filter { response ->
                    !TextUtils.isEmpty(response.getDevice()?.name)
                }
                .map {
                    it.getDevice()
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { checkScanResult(it) },
                    { error -> checkError(error) },
                    { Log.d(tag, "扫描完成") })
        }

        stopScan.setOnClickListener {
            scanDispose?.dispose()
        }
    }

    private fun checkError(error: Throwable) {
        Log.d(tag, "error:$error")
    }

    var connectDisposable: Disposable? = null

    private fun connect() {
        connect.setOnClickListener {
            connectDisposable = bluetoothController!!
                .connect("BB:A0:50:04:15:12")//BB-A0-50-04-15-12
                .auto()
                .timer(6000, TimeUnit.MILLISECONDS)
                .subscribe(
                    { response -> checkResultState(response) },
                    { error -> checkError(error) }
                )
        }

        //断开连接
        disconnected.setOnClickListener {
            connectDisposable?.dispose()
        }
    }

    private fun checkScanResult(it: BluetoothDevice?) {
        Log.d(tag, " 扫描到设备:${it!!.name}")
    }

    private fun checkResultState(response: Response) {
        when (response.code) {
            BluetoothConnectProfile.connected -> Log.d(tag, "连接成功")
            BluetoothConnectProfile.disconnected -> Log.d(tag, "断开连接")
            BluetoothConnectProfile.connectTimeout -> Log.d(tag, "连接超时")
            BluetoothConnectProfile.enableNotifySucceed -> Log.d(tag, "启用通知特征成功")
            BluetoothConnectProfile.enableNotifyFail -> Log.d(tag, "启用通知特征失败")
            BluetoothConnectProfile.serviceNotfound -> Log.d(tag, "未获取到对应uuid的服务特征")
            BluetoothConnectProfile.notifyNotFound -> Log.d(tag, "未获取到对应uuid的通知特征")
            BluetoothConnectProfile.reconnection -> Log.d(tag, "重连中")
        }
    }

    private fun checkResult(response: Response) {
        when (response.code) {
            BluetoothWriteProfile.writeSucceed -> Log.d(tag, "写入成功")
            BluetoothWriteProfile.writeFail -> Log.d(tag, "写入失败")
            BluetoothWriteProfile.characteristicChanged -> Log.d(tag, "收到新值-${Arrays.toString(response.data)}")
        }
    }

    /*
   请求授权
    */
    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            val permissions = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE
            )
            val permissionslist = ArrayList<String>()
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionslist.add(permission)
                }
            }
            if (permissionslist.size != 0) {
                val permissionsArray = permissionslist.toTypedArray()
                ActivityCompat.requestPermissions(
                    this, permissionsArray,
                    22
                )
            }
        }
    }

    private fun isMainThread(): Boolean {
        return Looper.getMainLooper().thread.id == Thread.currentThread().id
    }
}



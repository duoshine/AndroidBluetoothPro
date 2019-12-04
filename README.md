# RxAndroidBluetooth


基于rxjava2的ble库

详细使用请移步:   
[csdn](https://blog.csdn.net/duo_shine/article/details/89672883)      
[简书](https://www.jianshu.com/p/087c21e70877)   
#### 依赖

	implementation 'duoshine.rxjava2:rxandroidbluetooth:1.4'
	
#### 初始化

	private var bluetoothController: BluetoothWorker? = null

	
		bluetoothController = BluetoothController
		    .Builder(this)
                    .setNotifyUuid(notifyUUID)
                    .setServiceUuid(serviceUUID)
                    .setWriteUuid(writeUuid)
                    .build()

### startLeScan
开启扫描
	
		scanDispose = bluetoothController!!
                .startLeScan()
                .timer(6000, TimeUnit.MILLISECONDS)
		    .subscribe(
                    { checkScanResult(it) },
                    { error -> checkError(error) },
                    { Log.d(tag, "扫描完成") }) 

停止扫描

	scanDispose?.dispose()


### writeOnce
写操作
	
	 bluetoothController!!
                .writeOnce(byteArray)
		    .subscribe(
                    { response -> checkResult(response) },
                    { error -> checkError(error) }
                )

### connect
连接远程设备

	 connectDisposable = bluetoothController!!
                .connect("xx:xx:xx:xx:xx:xx")
                .auto()
                .timer(6000, TimeUnit.MILLISECONDS)
                .subscribe(
                    { response -> checkResultState(response) },
                    { error -> checkError(error) }
                )


connect支持断开自动连接(非手动,如调用dispose后则不会重连),你只需要一个auto即可支持断开自动重连   
connect支持连接超时限制,你只需要一个timer操作符即可实现   
扫描结果处理（更多功能码请参考BluetoothConnectProfile）:


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

断开连接：

	connectDisposable?.dispose()

ps:每次连接任务之前最好都需要.dispose(),否则你将开启两个连接任务

### note


- 你可能需要在不需要扫描及断开连接的地方合适地调用dispose，这和平时使用rxjava是一样的,避免内存泄漏   


- 你如果不在subscribe中处理onError,那么它将由Android捕获,RxAndroidBluetooth维护的异常都在这个包内
		
	duoshine.rxandroidbluetooth.exception




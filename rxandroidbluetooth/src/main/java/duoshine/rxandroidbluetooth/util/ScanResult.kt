package duoshine.rxandroidbluetooth.util

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult

/**
 *Created by chen on 2019
 */
class ScanResult {

    /**
     * For chained advertisements, inidcates tha the data contained in this
     * scan result is complete.
     */
    private val DATA_COMPLETE = 0x00

    /**
     * For chained advertisements, indicates that the controller was
     * unable to receive all chained packets and the scan result contains
     * incomplete truncated data.
     */
    private val DATA_TRUNCATED = 0x02

    /**
     * Indicates that the secondary physical layer was not used.
     */
    private val PHY_UNUSED = 0x00

    /**
     * Advertising Set ID is not present in the packet.
     */
    private val SID_NOT_PRESENT = 0xFF

    /**
     * TX power is not present in the packet.
     */
    private val TX_POWER_NOT_PRESENT = 0x7F

    /**
     * Periodic advertising inteval is not present in the packet.
     */
    private val PERIODIC_INTERVAL_NOT_PRESENT = 0x00

    /**
     * Mask for checking whether event type represents legacy advertisement.
     */
    private val ET_LEGACY_MASK = 0x10

    /**
     * Mask for checking whether event type represents connectable advertisement.
     */
    private val ET_CONNECTABLE_MASK = 0x01

    // Remote Bluetooth device.
    private var mDevice: BluetoothDevice? = null

    // Scan record, including advertising data and scan response data.
    private var mScanRecord: ScanRecord? = null

    // Received signal strength.
    private var mRssi: Int = 0

    //callbackType
    private var mCallbackType: Int = 0


    // Device timestamp when the result was last seen.
    private var mTimestampNanos: Long = 0

    private var mIsLegacy: Boolean = false
    private var mIsConnectable: Boolean = false
    private var mPrimaryPhy: Int = 0
    private var mDataStatus: Int = 0
    private var mSecondaryPhy: Int = 0
    private var mAdvertisingSid: Int = 0
    private var mTxPower: Int = 0
    private var mPeriodicAdvertisingInterval: Int = 0


    /**
     * Constructs a new ScanResult.
     *
     * @param device Remote Bluetooth device found.
     * @param eventType Event type.
     * @param primaryPhy Primary advertising phy.
     * @param secondaryPhy Secondary advertising phy.
     * @param advertisingSid Advertising set ID.
     * @param txPower Transmit power.
     * @param rssi Received signal strength.
     * @param periodicAdvertisingInterval Periodic advertising interval.
     * @param scanRecord Scan record including both advertising data and scan response data.
     * @param timestampNanos Timestamp at which the scan result was observed.
     */
    constructor(
        device: BluetoothDevice,
        isLegacy: Boolean,
        isConnectable: Boolean,
        dataStatus: Int,
        primaryPhy: Int,
        secondaryPhy: Int,
        advertisingSid: Int,
        txPower: Int,
        rssi: Int,
        periodicAdvertisingInterval: Int,
        scanRecord: ScanRecord?,
        timestampNanos: Long,
        callbackType: Int
    ) {
        mDevice = device
        mIsLegacy = isLegacy
        mIsConnectable = isConnectable
        mDataStatus = dataStatus
        mPrimaryPhy = primaryPhy
        mSecondaryPhy = secondaryPhy
        mAdvertisingSid = advertisingSid
        mTxPower = txPower
        mRssi = rssi
        mPeriodicAdvertisingInterval = periodicAdvertisingInterval
        mScanRecord = scanRecord
        mTimestampNanos = timestampNanos
        mCallbackType = callbackType
    }

    constructor(
        device: BluetoothDevice,
        rssi: Int,
        scanRecord: ScanRecord?,
        timestampNanos: Long,
        callbackType: Int
    ) {
        mDevice = device
        mRssi = rssi
        mScanRecord = scanRecord
        mTimestampNanos = timestampNanos
        mCallbackType = callbackType
    }

    constructor(device: BluetoothDevice, rssi: Int, scanRecord: ByteArray?) {
        mDevice = device
        mScanRecord = ScanRecord().parseFromBytes(scanRecord)
        mRssi = rssi
    }

    /**
     * Returns the remote Bluetooth device identified by the Bluetooth device address.
     */
    fun getDevice(): BluetoothDevice? {
        return mDevice
    }

    /**
     * Returns the scan record, which is a combination of advertisement and scan response.
     */
    fun getScanRecord(): ScanRecord? {
        return mScanRecord
    }

    /**
     * Returns the received signal strength in dBm. The valid range is [-127, 126].
     */
    fun getRssi(): Int {
        return mRssi
    }

    /**
     * Returns timestamp since boot when the scan record was observed.
     */
    fun getTimestampNanos(): Long {
        return mTimestampNanos
    }

    /**
     * Returns callbackType
     */
    fun getCallbackType(): Int {
        return mCallbackType
    }

    /**
     * Returns true if this object represents legacy scan result.
     * Legacy scan results do not contain advanced advertising information
     * as specified in the Bluetooth Core Specification v5.
     */
    fun isLegacy(): Boolean {
        return mIsLegacy
    }

    /**
     * Returns true if this object represents connectable scan result.
     */
    fun isConnectable(): Boolean {
        return mIsConnectable
    }

    /**
     * Returns the data status.
     * Can be one of [ScanResult.DATA_COMPLETE] or
     * [ScanResult.DATA_TRUNCATED].
     */
    fun getDataStatus(): Int {
        // return bit 5 and 6
        return mDataStatus
    }

    /**
     * Returns the primary Physical Layer
     * on which this advertisment was received.
     * Can be one of [BluetoothDevice.PHY_LE_1M] or
     * [BluetoothDevice.PHY_LE_CODED].
     */
    fun getPrimaryPhy(): Int {
        return mPrimaryPhy
    }

    /**
     * Returns the secondary Physical Layer
     * on which this advertisment was received.
     * Can be one of [BluetoothDevice.PHY_LE_1M],
     * [BluetoothDevice.PHY_LE_2M], [BluetoothDevice.PHY_LE_CODED]
     * or [ScanResult.PHY_UNUSED] - if the advertisement
     * was not received on a secondary physical channel.
     */
    fun getSecondaryPhy(): Int {
        return mSecondaryPhy
    }

    /**
     * Returns the advertising set id.
     * May return [ScanResult.SID_NOT_PRESENT] if
     * no set id was is present.
     */
    fun getAdvertisingSid(): Int {
        return mAdvertisingSid
    }

    /**
     * Returns the transmit power in dBm.
     * Valid range is [-127, 126]. A value of [ScanResult.TX_POWER_NOT_PRESENT]
     * indicates that the TX power is not present.
     */
    fun getTxPower(): Int {
        return mTxPower
    }

    /**
     * Returns the periodic advertising interval in units of 1.25ms.
     * Valid range is 6 (7.5ms) to 65536 (81918.75ms). A value of
     * [ScanResult.PERIODIC_INTERVAL_NOT_PRESENT] means periodic
     * advertising interval is not present.
     */
    fun getPeriodicAdvertisingInterval(): Int {
        return mPeriodicAdvertisingInterval
    }
}

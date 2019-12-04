package duoshine.rxandroidbluetooth.util

import android.bluetooth.le.ScanRecord
import android.os.ParcelUuid
import android.util.Log
import android.util.SparseArray
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.collections.HashMap
import kotlin.experimental.and

/**
 *Created by chen on 2019
 */
class ScanRecord {

    private val TAG = "ScanRecord"

    // The following data type values are assigned by Bluetooth SIG.
    // For more details refer to Bluetooth 4.1 specification, Volume 3, Part C, Section 18.
    private val DATA_TYPE_FLAGS = 0x01
    private val DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL = 0x02
    private val DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE = 0x03
    private val DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL = 0x04
    private val DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE = 0x05
    private val DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL = 0x06
    private val DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE = 0x07
    private val DATA_TYPE_LOCAL_NAME_SHORT = 0x08
    private val DATA_TYPE_LOCAL_NAME_COMPLETE = 0x09
    private val DATA_TYPE_TX_POWER_LEVEL = 0x0A
    private val DATA_TYPE_SERVICE_DATA_16_BIT = 0x16
    private val DATA_TYPE_SERVICE_DATA_32_BIT = 0x20
    private val DATA_TYPE_SERVICE_DATA_128_BIT = 0x21
    private val DATA_TYPE_MANUFACTURER_SPECIFIC_DATA = 0xFF

   private val BASE_UUID = ParcelUuid.fromString("00000000-0000-1000-8000-00805F9B34FB")

    // Flags of the advertising data.
    private var mAdvertiseFlags: Int = 0

    private var mServiceUuids: List<ParcelUuid>? = null

    private var mManufacturerSpecificData: SparseArray<ByteArray>? = null

    private var mServiceData: Map<ParcelUuid, ByteArray>? = null

    // Transmission power level(in dB).
    private var mTxPowerLevel: Int = 0

    // Local name of the Bluetooth LE device.
    private var mDeviceName: String? = null

    // Raw bytes of scan record.
    private var mBytes: ByteArray? = null

    /**
     * Returns the advertising flags indicating the discoverable mode and capability of the device.
     * Returns -1 if the flag field is not set.
     */
    fun getAdvertiseFlags(): Int {
        return mAdvertiseFlags
    }

    /**
     * Returns a list of service UUIDs within the advertisement that are used to identify the
     * bluetooth GATT services.
     */
    fun getServiceUuids(): List<ParcelUuid>? {
        return mServiceUuids
    }

    /**
     * Returns a sparse array of manufacturer identifier and its corresponding manufacturer specific
     * data.
     */
    fun getManufacturerSpecificData(): SparseArray<ByteArray>? {
        return mManufacturerSpecificData
    }

    /**
     * Returns the manufacturer specific data associated with the manufacturer id. Returns
     * `null` if the `manufacturerId` is not found.
     */
    fun getManufacturerSpecificData(manufacturerId: Int): ByteArray? {
        return mManufacturerSpecificData?.get(manufacturerId)
    }

    /**
     * Returns a map of service UUID and its corresponding service data.
     */
    fun getServiceData(): Map<ParcelUuid, ByteArray>? {
        return mServiceData
    }

    /**
     * Returns the service data byte array associated with the `serviceUuid`. Returns
     * `null` if the `serviceDataUuid` is not found.
     */
    fun getServiceData(serviceDataUuid: ParcelUuid?): ByteArray? {
        return if (serviceDataUuid == null || mServiceData == null) {
            null
        } else mServiceData!![serviceDataUuid]
    }

    /**
     * Returns the transmission power level of the packet in dBm. Returns [Integer.MIN_VALUE]
     * if the field is not set. This value can be used to calculate the path loss of a received
     * packet using the following equation:
     *
     *
     * `pathloss = txPowerLevel - rssi`
     */
    fun getTxPowerLevel(): Int {
        return mTxPowerLevel
    }

    /**
     * Returns the local name of the BLE device. The is a UTF-8 encoded string.
     */
    fun getDeviceName(): String? {
        return mDeviceName
    }

    /**
     * Returns raw bytes of scan record.
     */
    fun getBytes(): ByteArray? {
        return mBytes
    }

     constructor(
        serviceUuids: List<ParcelUuid>?,
        manufacturerData: SparseArray<ByteArray>?,
        serviceData: Map<ParcelUuid, ByteArray>?,
        advertiseFlags: Int, txPowerLevel: Int,
        localName: String?, bytes: ByteArray?
    ) {
        mServiceUuids = serviceUuids
        mManufacturerSpecificData = manufacturerData
        mServiceData = serviceData
        mDeviceName = localName
        mAdvertiseFlags = advertiseFlags
        mTxPowerLevel = txPowerLevel
        mBytes = bytes
    }

    constructor()

    /**
     * Parse scan record bytes to [ScanRecord].
     *
     *
     * The format is defined in Bluetooth 4.1 specification, Volume 3, Part C, Section 11 and 18.
     *
     *
     * All numerical multi-byte entities and values shall use little-endian **byte**
     * order.
     *
     * @param scanRecord The scan record of Bluetooth LE advertisement and/or scan response.
     */
      fun parseFromBytes(scanRecord: ByteArray?): duoshine.rxandroidbluetooth.util.ScanRecord? {
        if (scanRecord == null) {
            return null
        }
        var currentPos = 0
        var advertiseFlag = -1
        var serviceUuids: MutableList<ParcelUuid>? = ArrayList()
        var localName: String? = null
        var txPowerLevel = Integer.MIN_VALUE

        val manufacturerData = SparseArray<ByteArray>()
        val serviceData = HashMap<ParcelUuid, ByteArray>()

        try {
            while (currentPos < scanRecord.size) {
                // length is unsigned int.
                val length = scanRecord[currentPos++] and 0xFF.toByte()
                if (length == 0.toByte()) {
                    break
                }
                // Note the length includes the length of the field type itself.
                val dataLength = length - 1
                // fieldType is unsigned int.
                val fieldType = (scanRecord[currentPos++] and 0xFF.toByte()).toInt()
                when (fieldType) {
                    DATA_TYPE_FLAGS -> advertiseFlag = (scanRecord[currentPos] and 0xFF.toByte()).toInt()
                    DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL, DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE -> parseServiceUuid(
                        scanRecord, currentPos,
                        dataLength, 2, serviceUuids!!
                    )
                    DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL, DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE -> parseServiceUuid(
                        scanRecord, currentPos, dataLength,
                        4, serviceUuids!!
                    )
                    DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL, DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE -> parseServiceUuid(
                        scanRecord, currentPos, dataLength,
                        16, serviceUuids!!
                    )
                    DATA_TYPE_LOCAL_NAME_SHORT, DATA_TYPE_LOCAL_NAME_COMPLETE -> localName = String(
                        extractBytes(scanRecord, currentPos, dataLength)
                    )
                    DATA_TYPE_TX_POWER_LEVEL -> txPowerLevel = scanRecord[currentPos].toInt()
                    DATA_TYPE_SERVICE_DATA_16_BIT, DATA_TYPE_SERVICE_DATA_32_BIT, DATA_TYPE_SERVICE_DATA_128_BIT -> {
                        var serviceUuidLength = 2
                        if (fieldType == DATA_TYPE_SERVICE_DATA_32_BIT) {
                            serviceUuidLength = 4
                        } else if (fieldType == DATA_TYPE_SERVICE_DATA_128_BIT) {
                            serviceUuidLength = 16
                        }

                        val serviceDataUuidBytes = extractBytes(
                            scanRecord, currentPos,
                            serviceUuidLength
                        )
                        val serviceDataUuid = parseUuidFrom(
                            serviceDataUuidBytes
                        )
                        val serviceDataArray = extractBytes(
                            scanRecord,
                            currentPos + serviceUuidLength, dataLength - serviceUuidLength
                        )
                        serviceData[serviceDataUuid] = serviceDataArray
                    }
                    DATA_TYPE_MANUFACTURER_SPECIFIC_DATA -> {
                        // The first two bytes of the manufacturer specific data are
                        // manufacturer ids in little endian.
                        val manufacturerId =
                            ((scanRecord[currentPos + 1] and 0xFF.toByte()).toInt() shl 8) + (scanRecord[currentPos] and 0xFF.toByte())
                        val manufacturerDataBytes = extractBytes(
                            scanRecord, currentPos + 2,
                            dataLength - 2
                        )
                        manufacturerData.put(manufacturerId, manufacturerDataBytes)
                    }
                    else -> {
                    }
                }// Just ignore, we don't handle such data type.
                currentPos += dataLength
            }

            if (serviceUuids!!.isEmpty()) {
                serviceUuids = null
            }
            return ScanRecord(
                serviceUuids, manufacturerData, serviceData,
                advertiseFlag, txPowerLevel, localName, scanRecord
            )
        } catch (e: Exception) {
            Log.e(TAG, "unable to parse scan record: " + Arrays.toString(scanRecord))
            // As the record is invalid, ignore all the parsed results for this packet
            // and return an empty record with raw scanRecord bytes in results
            return ScanRecord(
                null,
                null,
                null,
                -1,
                Integer.MIN_VALUE,
                null,
                scanRecord
            )
        }
    }

    /**
     * Parse UUID from bytes. The `uuidBytes` can represent a 16-bit, 32-bit or 128-bit UUID,
     * but the returned UUID is always in 128-bit format.
     * Note UUID is little endian in Bluetooth.
     *
     * @param uuidBytes Byte representation of uuid.
     * @return [ParcelUuid] parsed from bytes.
     * @throws IllegalArgumentException If the `uuidBytes` cannot be parsed.
     */
    fun parseUuidFrom(uuidBytes: ByteArray?): ParcelUuid {
        if (uuidBytes == null) {
            throw IllegalArgumentException("uuidBytes cannot be null")
        }
        val length = uuidBytes.size
        if (length != 2 && length != 4
            && length != 16
        ) {
            throw IllegalArgumentException("uuidBytes length invalid - $length")
        }

        // Construct a 128 bit UUID.
        if (length == 16) {
            val buf = ByteBuffer.wrap(uuidBytes).order(ByteOrder.LITTLE_ENDIAN)
            val msb = buf.getLong(8)
            val lsb = buf.getLong(0)
            return ParcelUuid(UUID(msb, lsb))
        }

        // For 16 bit and 32 bit UUID we need to convert them to 128 bit value.
        // 128_bit_value = uuid * 2^96 + BASE_UUID
        var shortUuid: Long
        if (length == 2) {
            shortUuid = (uuidBytes[0] and 0xFF.toByte()).toLong()
            shortUuid += (uuidBytes[1] and 0xFF.toByte() and 8).toLong()
        } else {
            shortUuid = (uuidBytes[0] and 0xFF.toByte()).toLong()
            shortUuid += ((uuidBytes[1] and 0xFF.toByte()).toInt() shl 8).toLong()
            shortUuid += ((uuidBytes[2] and 0xFF.toByte()).toInt() shl 16).toLong()
            shortUuid += ((uuidBytes[3] and 0xFF.toByte()).toInt() shl 24).toLong()
        }
        val msb = BASE_UUID.uuid.mostSignificantBits + (shortUuid shl 32)
        val lsb = BASE_UUID.uuid.leastSignificantBits
        return ParcelUuid(UUID(msb, lsb))
    }

    // Parse service UUIDs.
     fun parseServiceUuid(
        scanRecord: ByteArray, currentPos: Int, dataLength: Int,
        uuidLength: Int, serviceUuids: MutableList<ParcelUuid>
    ): Int {
        var currentPos = currentPos
        var dataLength = dataLength
        while (dataLength > 0) {
            val uuidBytes = extractBytes(
                scanRecord, currentPos,
                uuidLength
            )
            serviceUuids.add(parseUuidFrom(uuidBytes))
            dataLength -= uuidLength
            currentPos += uuidLength
        }
        return currentPos
    }

    // Helper method to extract bytes from byte array.
     fun extractBytes(scanRecord: ByteArray?, start: Int, length: Int): ByteArray {
        val bytes = ByteArray(length)
        System.arraycopy(scanRecord!!, start, bytes, 0, length)
        return bytes
    }
}
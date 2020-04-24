package dp3t.gatt;

import android.annotation.SuppressLint;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.ScanCallback;
import android.content.Context;

import dp3t.internal.BroadcastHelper;

public final class BluetoothServiceStatus {

	public static final int SCAN_OK = 0;
	public static final int ADVERTISE_OK = 0;

	@SuppressLint("StaticFieldLeak")
	private static BluetoothServiceStatus instance;

	private Context context;

	private int scanStatus = SCAN_OK;
	private int advertiseStatus = ADVERTISE_OK;

	private BluetoothServiceStatus(Context context) {
		this.context = context;
	}

	public static synchronized BluetoothServiceStatus getInstance(Context context) {
		if (instance == null) {
			instance = new BluetoothServiceStatus(context.getApplicationContext());
		}
		return instance;
	}

	public static synchronized void resetInstance() {
		instance = null;
	}

	/**
	 * @param scanStatus {@link BluetoothServiceStatus#SCAN_OK} or error code (one of {@link ScanCallback}#SCAN_FAILED_*) for scan
	 * failure.
	 */
	void updateScanStatus(int scanStatus) {
		if (this.scanStatus != scanStatus) {
			this.scanStatus = scanStatus;
			BroadcastHelper.sendErrorUpdateBroadcast(context);
		}
	}

	/**
	 * @return {@link BluetoothServiceStatus#SCAN_OK} or error code (one of {@link ScanCallback}#SCAN_FAILED_*) for scan failure.
	 */
	public int getScanStatus() {
		return scanStatus;
	}

	/**
	 * @param advertiseStatus {@link BluetoothServiceStatus#ADVERTISE_OK} or error code
	 * (see {@link AdvertiseCallback}#ADVERTISE_FAILED_* constants) for advertising start failures.
	 */
	void updateAdvertiseStatus(int advertiseStatus) {
		if (this.advertiseStatus != advertiseStatus) {
			this.advertiseStatus = advertiseStatus;
			BroadcastHelper.sendErrorUpdateBroadcast(context);
		}
	}

	/**
	 * @return {@link BluetoothServiceStatus#ADVERTISE_OK} or error code
	 * (see {@link AdvertiseCallback}#ADVERTISE_FAILED_* constants) for advertising start failures.
	 */
	public int getAdvertiseStatus() {
		return advertiseStatus;
	}

}

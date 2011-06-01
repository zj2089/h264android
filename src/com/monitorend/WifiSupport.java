package com.monitorend;

import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;

class WifiSupport {

	private WifiManager mWifiManager;

	private WifiInfo mWifiInfo;

	private List<ScanResult> mWifiList;

	private List<WifiConfiguration> mWifiConfiguration;

	WifiLock mWifiLock;

	public WifiSupport(Context context) {

		mWifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);

		mWifiInfo = mWifiManager.getConnectionInfo();
	}

	// open WIFI
	public void openWifi() {
		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		}
	}

	// close WIFI
	public void closeWifi() {
		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
		}
	}

	public void acquireWifiLock() {
		mWifiLock.acquire();
	}

	public void releaseWifiLock() {
		if (mWifiLock.isHeld()) {
			mWifiLock.release();
		}
	}

	public void createWifiLock() {
		mWifiLock = mWifiManager.createWifiLock("Test");
	}

	public List<WifiConfiguration> getConfiguration() {
		return mWifiConfiguration;
	}

	public void connectConfiguration(int index) {
		if (index > mWifiConfiguration.size()) {
			return;
		}
		mWifiManager.enableNetwork(mWifiConfiguration.get(index).networkId,
				true);
	}

	public void startScan() {
		mWifiManager.startScan();

		mWifiList = mWifiManager.getScanResults();

		mWifiConfiguration = mWifiManager.getConfiguredNetworks();
	}

	public List<ScanResult> getWifiList() {
		return mWifiList;
	}

	public StringBuilder lookupScan() {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < mWifiList.size(); i++) {
			stringBuilder
					.append("Index_" + new Integer(i + 1).toString() + ":");
			// 将ScanResult信息转换成一个字符串包
			// 其中把包括：BSSID、SSID、capabilities、frequency、level
			stringBuilder.append((mWifiList.get(i)).toString());
			stringBuilder.append("\n");
		}
		return stringBuilder;
	}

	public String getMacAddress() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
	}

	public String getBSSID() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
	}

	public String getIPAddress() {
		String ipAddr;
		int ip = (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
		int ip4 = ( (ip & 0xff000000) >> 24 );
		int ip3 = ( (ip & 0x00ff0000) >> 16 );
		int ip2 = ( (ip & 0x0000ff00) >> 8 );
		int ip1 = (ip & 0x000000ff);
		ipAddr = String.format("%d.%d.%d.%d", ip1, ip2, ip3, ip4<0?256+ip4:ip4);
		return ipAddr;
	}

	public int getNetworkId() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
	}

	public String getWifiInfo() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
	}

	public void addNetwork(WifiConfiguration wcg) {
		int wcgID = mWifiManager.addNetwork(wcg);
		mWifiManager.enableNetwork(wcgID, true);
	}

	public void disconnectWifi(int netId) {
		mWifiManager.disableNetwork(netId);
		mWifiManager.disconnect();
	}

}

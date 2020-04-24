/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package dp3t.models;

public class ApplicationInfo {

	private String appId;
	private String reportBaseUrl;
	private String bucketBaseUrl;

	public ApplicationInfo(String appId, String reportBaseUrl, String bucketBaseUrl) {
		this.appId = appId;
		this.reportBaseUrl = reportBaseUrl;
		this.bucketBaseUrl = bucketBaseUrl;
	}

	public String getAppId() {
		return appId;
	}

	public String getReportBaseUrl() {
		return reportBaseUrl;
	}

	public String getBucketBaseUrl() {
		return bucketBaseUrl;
	}

}

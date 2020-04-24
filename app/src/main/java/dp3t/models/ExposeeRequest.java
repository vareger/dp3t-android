/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */

package dp3t.models;

import dp3t.util.DayDate;

public class ExposeeRequest {

	private String key;
	private DayDate onset;
	private ExposeeAuthMethodJson authData;

	public ExposeeRequest(String key, DayDate onset, ExposeeAuthMethodJson authData) {
		this.key = key;
		this.onset = onset;
		this.authData = authData;
	}

	public String getKey() {
		return key;
	}

	public DayDate getOnset() {
		return onset;
	}

	public ExposeeAuthMethodJson getAuthData() {
		return authData;
	}

}

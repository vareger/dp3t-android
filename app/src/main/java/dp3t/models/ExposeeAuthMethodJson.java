/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package dp3t.models;

public class ExposeeAuthMethodJson implements ExposeeAuthMethod {

	private String value;

	public ExposeeAuthMethodJson(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}

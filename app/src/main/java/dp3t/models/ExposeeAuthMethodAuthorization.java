/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package dp3t.models;

public class ExposeeAuthMethodAuthorization implements ExposeeAuthMethod {

	private String authorization;

	public ExposeeAuthMethodAuthorization(String authorization) {
		this.authorization = authorization;
	}

	public String getAuthorization() {
		return authorization;
	}

}

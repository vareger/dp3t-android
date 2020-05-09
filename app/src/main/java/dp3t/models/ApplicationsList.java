/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package dp3t.models;

import java.util.ArrayList;
import java.util.List;

public class ApplicationsList {

	private List<ApplicationInfo> applications;

	public ApplicationsList() {
		applications = new ArrayList<>();

	}

	public List<ApplicationInfo> getApplications() {
		applications.add(new ApplicationInfo("com.vareger.tracker","",""));
		return applications;
	}

}

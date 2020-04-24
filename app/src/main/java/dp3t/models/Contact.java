/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package dp3t.models;

import dp3t.crypto.EphId;

public class Contact {

	private int id;
	private long date;
	private EphId ephId;
	private int windowCount;
	private int associatedKnownCase;

	public Contact(int id, long date, EphId ephId, int windowCount, int associatedKnownCase) {
		this.id = id;
		this.date = date;
		this.ephId = ephId;
		this.windowCount = windowCount;
		this.associatedKnownCase = associatedKnownCase;
	}

	public EphId getEphId() {
		return ephId;
	}

	public long getDate() {
		return date;
	}

	public double getWindowCount() {
		return windowCount;
	}

	public int getAssociatedKnownCase() {
		return associatedKnownCase;
	}

	public int getId() {
		return id;
	}

}

/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */
package dp3t.models;

public class KnownCase {

	private int id;
	private Long day;
	private byte[] key;

	public KnownCase(int id, Long day, byte[] key) {
		this.id = id;
		this.day = day;
		this.key = key;
	}

	public int getId() {
		return id;
	}

	public Long getDay() {
		return day;
	}

	public byte[] getKey() {
		return key;
	}

}

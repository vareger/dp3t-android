/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */

package dp3t.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.text.ParseException;

class DayDateJsonAdapter extends TypeAdapter<DayDate> {

	@Override
	public void write(JsonWriter out, DayDate value) throws IOException {
		out.value(value.formatAsString());
	}

	@Override
	public DayDate read(JsonReader in) throws IOException {
		String value = in.nextString();
		try {
			return new DayDate(value);
		} catch (ParseException e) {
			throw new IOException("Unexpected DayDate format " + value, e);
		}
	}

}

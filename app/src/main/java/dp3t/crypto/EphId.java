package dp3t.crypto;

import java.util.Arrays;

public class EphId {

	private byte[] data;

	public EphId(byte[] data) {
		this.data = data;
	}

	public byte[] getData() {
		return data;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		EphId ephId = (EphId) o;
		return Arrays.equals(data, ephId.data);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(data);
	}

}

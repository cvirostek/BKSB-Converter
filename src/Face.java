import java.util.Arrays;

public class Face {
	
	private int[] indices;
	private int[] uv_indices;
	
	public Face(int[] indices) {
		this.indices = Arrays.copyOf(indices, indices.length);
	}
	
	public Face(int[] indices, int[] uv_indices) {
		this.indices = Arrays.copyOf(indices, indices.length);
		this.uv_indices = Arrays.copyOf(uv_indices, uv_indices.length);
	}
	
	public int getIndex(int num) {
		return indices[num];
	}
	
	public void setIndex(int j, int new_index) {
		indices[j] = new_index;
	}
	
	public int getUVIndex(int num) {
		return uv_indices[num];
	}
	
	public boolean hasUV() {
		return uv_indices != null;
	}
	
	public String f_out() {
		String out = "f";
		for (int index : indices) {
			out += " " + index + "/" + index;
		}
		return out;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Face other = (Face)o;
		return indices.equals(other.indices) && uv_indices.equals(other.uv_indices);
	}
}

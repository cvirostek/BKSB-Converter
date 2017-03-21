
public class Vertex {
	
	private float x;
	private float y;
	private float z;
	private float u;
	private float v;
	private float u_lm;
	private float v_lm;
	private boolean hasUVs;
	
	public Vertex(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		hasUVs = false;
	}
	
	public Vertex(float x, float y, float z, float u, float v) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.u = u;
		this.v = v;
		hasUVs = true;
	}
	
	public Vertex(float x, float y, float z, float u, float v, float u_lm, float v_lm) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.u = u;
		this.v = v;
		this.u_lm = u_lm;
		this.v_lm = v_lm;
		hasUVs = true;
	}
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}
	
	public float getZ() {
		return z;
	}
	
	public float getU() {
		return u;
	}
	
	public float getV() {
		return v;
	}
	
	public float getU_lm() {
		return u_lm;
	}
	
	public float getV_lm() {
		return v_lm;
	}
	
	public boolean hasUVs() {
		return hasUVs;
	}
	
	public void addUVs(float u, float v) {
		this.u = u;
		this.v = v;
		hasUVs = true;
	}
	
	public void addLightmapUVs(float u_lm, float v_lm) {
		this.u_lm = u_lm;
		this.v_lm = v_lm;
	}
	
	public String v_out() {
		return "v "+String.format("%.4f", x)+" "+String.format("%.4f", y)+" "+String.format("%.4f", z);
	}
	
	public String vt_out() {
		return "vt "+String.format("%.4f", u)+" "+String.format("%.4f", v);
	}
	
	public String lm_vt_out() {
		return "vt "+String.format("%.4f", u_lm)+" "+String.format("%.4f", v_lm);
	}
}

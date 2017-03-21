import java.io.*;

public class BksbFrame {
	
	private int numVertices;
	private int numFaces;
	private Vertex[] vertices;
	private int vertexIndex;
	private Face[] faces;
	private int faceIndex;
	
	public BksbFrame(int numVertices, int numFaces) {
		this.numVertices = numVertices;
		this.numFaces = numFaces;
		vertices = new Vertex[numVertices];
		faces = new Face[numFaces];
	}
	
	public int getNumVertices() {
		return numVertices;
	}
	
	public int getNumFaces() {
		return numFaces;
	}
	
	public void addVertex(Vertex vertex) {
		vertices[vertexIndex] = vertex;
		vertexIndex++;
	}
	
	public void addFace(Face face) {
		faces[faceIndex] = face;
		faceIndex++;
	}
	
	public void generateObj(String filename, boolean includeLightmap) throws IOException {
		Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filename+".obj"))));
		for (Vertex vertex : vertices) {
			out.write(vertex.v_out());
			out.write("\n");
		}
		for (Vertex vertex : vertices) {
			out.write(includeLightmap ? vertex.lm_vt_out() : vertex.vt_out());
			out.write("\n");
		}
		for (Face face : faces) {
			out.write(face.f_out());
			out.write("\n");
		}
		out.close();
	}
}

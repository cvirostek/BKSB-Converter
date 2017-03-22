import java.util.*;

import com.google.common.io.LittleEndianDataOutputStream;

import java.io.*;

public class ObjFrame {
	
	private File file;
	private ArrayList<Vertex> vertices;
	private ArrayList<Face> faces;
	private ArrayList<UV> uvs;
	
	public ObjFrame(File file) throws FileNotFoundException {
		this.file = file;
		vertices = new ArrayList<Vertex>();
		faces = new ArrayList<Face>();
		uvs = new ArrayList<UV>();
		parseFile();
	}
	
	private void parseFile() throws FileNotFoundException {
		Scanner in = new Scanner(file);
		Scanner scanLine;
		Scanner scanFace;
		String type;
		
		while (in.hasNextLine()) {
			
			scanLine = new Scanner(in.nextLine());
			scanLine.useDelimiter(" ");
			type = scanLine.hasNext() ? scanLine.next() : "";
			
			// Parse vertices
			if (type.equals("v")) {
				float x, y, z;
				x = scanLine.nextFloat();
				y = scanLine.nextFloat();
				z = scanLine.nextFloat();
				vertices.add(new Vertex(x, y, z));
			}
			
			// Parse UV coordinates
			else if (type.equals("vt")) {
				float u, v;
				u = scanLine.nextFloat();
				v = scanLine.nextFloat();
				uvs.add(new UV(u, 1-v));
			}
			
			// Parse faces
			else if (type.equals("f")) {
				int[] indices = new int[3];
				int[] uv_indices = null;
				for (int i = 2; i >= 0; i--) {
					scanFace = new Scanner(scanLine.next());
					scanFace.useDelimiter("/");
					// Get the vertex index; need to subtract 1 because bksb's indices
					// start at 0 while obj's indices start at 1. 
					indices[i] = scanFace.nextInt()-1;
					// Get the UV index if there is one
					if (scanFace.hasNext()) {
						if (uv_indices == null) {
							uv_indices = new int[3];
						}
						uv_indices[i] = scanFace.nextInt()-1;
					}
					scanFace.close();
				}
				// bksb only supports triangles, not any other type of polygon
				if (scanLine.hasNext()) {
					System.out.println("Warning: non-triangle face found.");
				}
				// If the obj doesn't have a UV map, add the face without UV indices
				if (uv_indices == null) {
					faces.add(new Face(indices));
				}
				// Otherwise add a face with both UV and vertex indices
				else {
					faces.add(new Face(indices, uv_indices));
				}
			}
			scanLine.close();
			
		}
		in.close();
	}
	
	// bksb has its UV coordinates included with each vertex, while obj defines the vertex
	// and UV coordinates separately then combines them by index when defining faces. We may
	// need to add new vertices if a single vertex uses more than one UV.
	public void addUVsToVertices() {
		ArrayList<Vertex> new_vertices = new ArrayList<Vertex>();
		int vert_index;
		int uv_index;
		Vertex tmpVertex;
		UV tmpUV;
		Face tmpFace;
		Vertex newVertex;
		
		// Loop through each face
		for (int i = 0; i < faces.size(); i++) {
			tmpFace = faces.get(i);
			for (int j = 0; j < 3; j++) { // check each vertex of the face
				vert_index = tmpFace.getIndex(j);
				tmpVertex = vertices.get(vert_index);
				// if the vertex index is out of the bounds of the list of vertices,
				// that means it was already modified and doesn't need to be touched again
				if (vert_index < vertices.size()) {
					uv_index = tmpFace.getUVIndex(j);
					tmpUV = uvs.get(uv_index);
					// Add UV coordinates to a vertex if it doesn't have them already
					if (!tmpVertex.hasUVs()) {
						tmpVertex.addUVs(tmpUV.u, tmpUV.v);
						tmpVertex.addLightmapUVs(tmpUV.u_lm, tmpUV.v_lm);
					}
					// If the vertex already has UV coordinates but they don't match the ones indicated by
					// tmpFace, create a new vertex with the same XYZ coordinates and the UV coordinates
					// indicated by tmpFace.
					else if (tmpVertex.getU() != tmpUV.u || tmpVertex.getV() != tmpUV.v) {
						// Update all references to this vertex in the face data to point to a new vertex
						updateFaces(i, vert_index, uv_index, vertices.size()+new_vertices.size());
						// Add the new vertex
						newVertex = new Vertex(tmpVertex.getX(), tmpVertex.getY(), tmpVertex.getZ(), tmpUV.u, tmpUV.v, tmpUV.u_lm, tmpUV.v_lm);
						new_vertices.add(newVertex);
					}
				}
			}
		}
		
		// Combine the original list of vertices with the list of new vertices. 
		vertices.addAll(new_vertices);
		
		// We don't need the list of UVs anymore since they've all been added to the vertices,
		// so we can just clear them from memory.
		uvs = null;
	}
	
	// Go through all of the faces after the given start index, and if they reference the given
	// vertex combined with the given UV point, then set the vertex index to the new vertex index.
	private void updateFaces(int start, int vert_index, int uv_index, int new_vert_index) {
		Face tmpFace;
		for (int i = start; i < faces.size(); i++) {
			for (int j = 0; j < 3; j++) {
				tmpFace = faces.get(j);
				if (tmpFace.getIndex(j) == vert_index && tmpFace.getUVIndex(j) == uv_index) {
					tmpFace.setIndex(j, new_vert_index);
				}
			}
		}
	}
	
	public boolean addLightmap(File file) throws FileNotFoundException {
		ObjFrame lmFrame = new ObjFrame(file);
		if (!lightmapMatches(lmFrame)) {
			System.out.println("Models " + this.file.getName() + " and " + file.getName() + " do not match.");
			return false;
		}
		else {
			for (int i = 0; i < uvs.size(); i++) {
				uvs.get(i).u_lm = lmFrame.getUVs().get(i).u;
				uvs.get(i).v_lm = lmFrame.getUVs().get(i).v;
			}
			return true;
		}
	}
	
	private boolean lightmapMatches(ObjFrame lmFrame) {
		if (lmFrame.getUVs().size() != uvs.size()
				|| lmFrame.getVertices().size() != vertices.size()
				|| lmFrame.getFaces().size() != faces.size()) {
			return false;
		}
		return true;
	}
	
	// Adjust the UVs if you insert the texture into a larger texture. textureSize is the dimensions
	// of the texture being inserted, texturePosition is the position of this texture from the bottom left
	// corner to the bottom left corner of the larger texture, and imageSize is the dimensions of the 
	// larger texture.
	public void transformUV(int[] textureSize, int[] texturePosition, int[] imageSize) {
		float[] pixelsIntoImage = new float[2];
		float u, v;
		Vertex tmpVertex;
		for (int i = 0; i < vertices.size(); i++) {
			tmpVertex = vertices.get(i);
			pixelsIntoImage[0] = tmpVertex.getU()*textureSize[0];
			pixelsIntoImage[1] = (1-tmpVertex.getV()*textureSize[1]);
			u = (texturePosition[0] + pixelsIntoImage[0])/imageSize[0];
			v = 1-(texturePosition[1] + pixelsIntoImage[1])/imageSize[1];
			tmpVertex.addUVs(u, v);
		}
	}
	
	public void writeVertices(LittleEndianDataOutputStream out, boolean writeLightmapUVs) throws IOException {
		for (Vertex vertex : vertices) {
			out.writeFloat(vertex.getX());
			out.writeFloat(vertex.getY());
			out.writeFloat(vertex.getZ());
			if (vertex.hasUVs()) {
				out.writeFloat(vertex.getU());
				out.writeFloat(vertex.getV());
				if (writeLightmapUVs) {
					out.writeFloat(vertex.getU_lm());
					out.writeFloat(vertex.getV_lm());
				}
			}
			else {
				out.writeFloat(0);
				out.writeFloat(0);
				if (writeLightmapUVs) {
					out.writeFloat(0);
					out.writeFloat(0);
				}
			}
		}
	}
	
	public void writeFaces(LittleEndianDataOutputStream out) throws IOException {
		for (Face face : faces) {
			for (int i = 0; i < 3; i++) {
				out.writeShort(face.getIndex(i));
			}
		}
	}
	
	public File getFile() {
		return file;
	}
	
	public int getNumVertices() {
		return vertices.size();
	}
	
	public int getNumFaces() {
		return faces.size();
	}
	
	public Vertex getVertex(int i) {
		return vertices.get(i);
	}
	
	public Face getFace(int i) {
		return faces.get(i);
	}
	
	public ArrayList<Vertex> getVertices() {
		return vertices;
	}
	
	public ArrayList<Face> getFaces() {
		return faces;
	}
	
	public ArrayList<UV> getUVs() {
		return uvs;
	}
	
	private class UV {
		private float u;
		private float v;
		private float u_lm;
		private float v_lm;
		private UV(float u, float v) {
			this.u = u;
			this.v = v;
		}
	}
}

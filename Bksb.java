import java.io.*;
import com.google.common.io.*;

public class Bksb {
	
	private File file;
	private int numFrames;
	private BksbFrame[] frames;
	private boolean hasLightmap;
	
	public Bksb(File file) throws IOException {
		this.file = file;
		parseFile();
	}
	
	private void parseFile() throws IOException {
		// Bksb is little endian
		LittleEndianDataInputStream in = new LittleEndianDataInputStream(new BufferedInputStream(new FileInputStream(file)));
		
		// Header is just 'v'
		if (in.readByte() != 'v') {
			in.close();
			throw new BksbParseException("Bad header.");
		}
		
		// Int after header indicates whether bksb has a lightmap or not.
		// 3 = no lightmap, 4 = yes lightmap
		hasLightmap = in.readInt() == 4;
		
		// Get number of faces and vertices for each frame
		numFrames = in.readInt();
		frames = new BksbFrame[numFrames];
		int numVertices;
		int numFaces;
		for (int i = 0; i < numFrames; i++) {
			numVertices = in.readInt();
			numFaces = in.readInt() / 3; // 3 indices per face (no support for non-triangle polygons)
			in.skip(8); // skip start indices of vertex/face data for each frame since we don't need them
			frames[i] = new BksbFrame(numVertices, numFaces);
		}
		
		// skip some irrelevant and/or redundant data
		in.skip(hasLightmap ? 66 : 47);
		
		// Get vertex data. UV mapping is attached to each vertex.
		float x, y, z, u, v, u_lm, v_lm;
		for (BksbFrame frame : frames) {
			for (int vertex = 0; vertex < frame.getNumVertices(); vertex++) {
				x = in.readFloat();
				y = in.readFloat();
				z = in.readFloat();
				u = in.readFloat();
				v = in.readFloat();
				if (hasLightmap) {
					u_lm = in.readFloat();
					v_lm = in.readFloat();
					frame.addVertex(new Vertex(x, y, z, u, 1-v, u_lm, 1-v_lm));
				}
				else {
					frame.addVertex(new Vertex(x, y, z, u, 1-v));
				}
			}
		}
		
		// more stuff we don't care about
		in.skip(16);
		
		// Get face data
		int[] vertexIndices = new int[3];
		for (BksbFrame frame : frames) {
			for (int face = 0; face < frame.getNumFaces(); face++) {
				// reverse the order of the vertex indices or else our faces will be flipped.
				for (int index = 2; index >= 0; index--) {
					vertexIndices[index] = in.readShort()+1;
				}
				frame.addFace(new Face(vertexIndices));
			}
		}
		
		// If we're not at the end of the file then something went wrong.
		if (in.available() > 0) {
			in.close();
			throw new BksbParseException("More data than expected.");
		}
		
		in.close();
	}
	
	public void generateObj(boolean includeLightmap) throws IOException {
		// If there's just one frame, create file without numbers
		if (numFrames == 1) {
			frames[0].generateObj(file.getName(), false);
			if (includeLightmap && hasLightmap) {
				frames[0].generateObj(file.getName()+"_lightmap", true);
			}
		}
		// If there's multiple frames, number each frame
		else {
			int i = 0;
			for (BksbFrame frame : frames) {
				frame.generateObj(file.getName()+"_"+i, false);
				i++;
			}
			if (includeLightmap && hasLightmap) {
				i = 0;
				for (BksbFrame frame : frames) {
					frame.generateObj(file.getName()+"_lightmap_"+i, true);
					i++;
				}
			}
		}
	}
	
	public void generateObj() throws IOException {
		generateObj(false);
	}
	
	private class BksbParseException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		public BksbParseException(String msg) {
			super(msg);
		}
	}
	
}

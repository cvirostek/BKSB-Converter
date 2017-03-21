import java.io.*;
import java.util.Arrays;
import java.util.Comparator;

import com.google.common.io.LittleEndianDataOutputStream;

public class Obj {
	
	private ObjFrame[] frames;
	private int numFrames;
	private boolean hasLightmap;
	
	public Obj(File[] files) throws FileNotFoundException {
		numFrames = files.length;
		frames = new ObjFrame[numFrames];
		Arrays.sort(files, new SortNumberedFiles());
		for (int i = 0; i < numFrames; i++) {
			frames[i] = new ObjFrame(files[i]);
		}
	}
	
	public Obj(File file) throws FileNotFoundException {
		numFrames = 1;
		frames = new ObjFrame[1];
		frames[0] = new ObjFrame(file);
	}
	
	public void adjustUVs(int[] textureSize, int[] texturePosition, int[] imageSize) {
		for (ObjFrame frame : frames) {
			frame.adjustUVs(textureSize, texturePosition, imageSize);
		}
	}
	
	public boolean addLightmap(File[] files) throws FileNotFoundException {
		Arrays.sort(files, new SortNumberedFiles());
		hasLightmap = true;
		for (int i = 0; i < frames.length; i++) {
			if (!frames[i].addLightmap(files[i])) {
				hasLightmap = false;
				break;
			}
		}
		return hasLightmap;
	}
	
	public boolean addLightmap(File file) throws FileNotFoundException {
		hasLightmap = frames[0].addLightmap(file);
		return hasLightmap;
	}
	
	public void generateBksb() throws IOException {
		for (ObjFrame frame : frames) {
			frame.addUVsToVertices();
		}
		
		LittleEndianDataOutputStream out = new LittleEndianDataOutputStream(new BufferedOutputStream(new FileOutputStream(frames[0].getFile().getPath()+".bksb")));
		out.writeByte('v');
		out.writeInt(hasLightmap ? 4 : 3);
		out.writeInt(numFrames);
		
		// Write general info about each frame
		int vert_start = 0;
		int face_start = 0;
		for (ObjFrame frame : frames) {
			out.writeInt(frame.getNumVertices());
			out.writeInt(frame.getNumFaces()*3);
			out.writeInt(vert_start);
			out.writeInt(face_start);
			vert_start += frame.getNumVertices();
			face_start += frame.getNumFaces()*3;
		}
		
		// Write a bunch of stuff that's we don't really care about
		if (hasLightmap) {
			out.writeInt(0);
			out.writeInt(vert_start);
			out.writeInt(28);
			out.writeInt(0);
			out.writeInt(3);
			out.writeInt(28);
			out.writeInt(0);
			out.writeShort(0);
			out.writeInt(2);
			out.writeInt(0);
			out.writeInt(2);
			out.writeInt(28);
			out.writeInt(12);
			out.writeInt(0);
			out.writeInt(2);
			out.writeInt(28);
			out.writeInt(20);
		}
		else {
			out.writeInt(0);
			out.writeInt(vert_start);
			out.writeInt(20);
			out.writeInt(0);
			out.writeInt(3);
			out.writeInt(20);
			out.writeInt(0);
			out.writeShort(0);
			out.writeInt(1);
			out.writeByte(0);
			out.writeInt(2);
			out.writeInt(20);
			out.writeInt(12);
		}
		
		
		// Write vertex data
		for (ObjFrame frame : frames) {
			frame.writeVertices(out, hasLightmap);
		}
		
		// Write a few bytes that precede face data
		out.writeInt(face_start);
		out.writeInt(0);
		out.writeInt(1);
		out.writeInt(2);
		
		// Write face data
		for (ObjFrame frame : frames) {
			frame.writeFaces(out);
		}
		
		out.close();
	}
	
	public static class SortNumberedFiles implements Comparator<File> {
		public int compare(File f1, File f2) {
			String s1 = f1.getName();
			String s2 = f2.getName();
			return extractInt(s1.substring(0, s1.lastIndexOf("."))) - extractInt(s2.substring(0, s2.lastIndexOf(".")));
		}
	    public int extractInt(String s) {
	        String num = s.replaceAll("\\D", "");
	        return num.isEmpty() ? 0 : Integer.parseInt(num);
	    }
	}
}

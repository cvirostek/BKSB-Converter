import java.io.*;
public class Driver {

	public static void main(String[] args) throws IOException {
		testToObj();
	}

	public static void testToObj() throws IOException {
		Bksb coin = new Bksb(new File("mariocoin.obj.bksb"));
		coin.generateObj(true);
	}
	
	public static void testToBksb() throws IOException {
		Obj coin = new Obj(new File("mariocoin.obj"));
		coin.generateBksb();
	}
}

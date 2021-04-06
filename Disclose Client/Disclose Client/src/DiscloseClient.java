import java.net.*;
import java.io.*;
import java.math.BigInteger;

public class DiscloseClient {
	private Socket socket;
	
	public DiscloseClient(String server, int port) throws IOException {
		Socket socket = new Socket(server, port);
	}
	
	public static void main(String[] args) throws IOException {
		DiscloseClient client = new DiscloseClient("127.0.0.1", 6666);
	}
}

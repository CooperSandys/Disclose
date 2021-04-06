import java.net.*;
import java.util.*;
import java.io.*;

public class DiscloseClient {
	private static int defaultPort = 41337;
	private Socket socket;
	private InputStream inStream;
	private DataInputStream dataInStream;
	private OutputStream outStream;
	private DataOutputStream dataOutStream;
	
	public DiscloseClient(String server, int port) throws IOException {
		this.socket = new Socket(server, port);
		this.inStream = this.socket.getInputStream();
		this.dataInStream = new DataInputStream(this.inStream);
		this.outStream = this.socket.getOutputStream();
		this.dataOutStream = new DataOutputStream(this.outStream);
	}
	
	public void connect(String server, int port) throws IOException {
		socket = new Socket(server, port);
		outStream = socket.getOutputStream();
		dataOutStream = new DataOutputStream(outStream);
	}
	
	public void close() throws IOException {
		sendCommandAsString("stop");
		dataInStream.close();
		inStream.close();
		dataOutStream.close();
		outStream.close();
		socket.close();
	}
	
	public void sendMessageAsString(String data) throws IOException {
		dataOutStream.writeUTF(">" + data);
		dataOutStream.flush();
	}
	
	public void sendCommandAsString(String data) throws IOException {
		dataOutStream.writeUTF("/" + data);
		dataOutStream.flush();
	}
	
	public void sendResponseCode(int responseCode) throws IOException {
		dataOutStream.writeUTF("!" + responseCode);
		dataOutStream.flush();
	}
	
	public void run() throws IOException {
		Scanner scan = new Scanner(System.in);
		while (true) {
			String in = scan.nextLine();
			if (in.equals("/stop")) {
				System.out.println("Shutting down client.");
				scan.close();
				close();
				System.exit(0);
			} else {
				sendMessageAsString(in);
			}
			String recieved = dataInStream.readUTF();
			switch (recieved.charAt(0)) {
				case '!' :
					if (recieved.charAt(1) == 0)
						System.out.println("Response recieved by server: " + recieved.substring(1));
					break;
				case '>' :
					System.out.println("Command recieved by server: " + recieved.substring(1));
					break;
				case '/' :
					System.out.println("Message recieved by server: " + recieved.substring(1));
					break;
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		System.out.println("Connecting to server...");
		DiscloseClient client = new DiscloseClient("127.0.0.1", defaultPort);
		System.out.println("Successfully connected to server!");
		client.run();
		// nothing after this will run :/
	}
}

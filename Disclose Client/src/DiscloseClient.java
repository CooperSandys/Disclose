import java.net.*;
import java.util.*;
import java.io.*;

public class DiscloseClient {
	private static int defaultPort = 41337;
	private Socket socket;
	private OutputStream outStream;
	private DataOutputStream dataOutStream;
	private DiscloseClientServerResponseHandler clientHandler;
	
	public DiscloseClient(String server, int port) throws IOException {
		this.socket = new Socket(server, port);
		this.outStream = this.socket.getOutputStream();
		this.dataOutStream = new DataOutputStream(this.outStream);
		this.clientHandler = new DiscloseClientServerResponseHandler(this.socket);
	}
	
	public void connect(String server, int port) throws IOException {
		socket = new Socket(server, port);
		outStream = socket.getOutputStream();
		dataOutStream = new DataOutputStream(outStream);
	}
	
	public void close() throws IOException {
		sendCommandAsString("stop");
		dataOutStream.close();
		outStream.close();
		clientHandler.close();
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
		clientHandler.start();
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
		}
	}
	
	public static void main(String[] args) throws IOException {
		System.out.println("Connecting to server...");
		DiscloseClient client = new DiscloseClient("127.0.0.1", defaultPort);
		System.out.println("Successfully connected to server!");
		client.run();
		// nothing after this will run :/ OH CAUSE IS A FUCNTIOn hahaha
	}
}

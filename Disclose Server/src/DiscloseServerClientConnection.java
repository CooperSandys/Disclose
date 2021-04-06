import java.io.IOException;
import java.io.*;

public class DiscloseServerClientConnection extends Thread {
	
	public static final int SERVER_RESPONSE_OKAY = 0;
	
	private int clientIndex;
	private DiscloseServer hostServer;
	private DiscloseServerClientIdentifier client;
	private InputStream inStream;
	private DataInputStream dataInStream;
	private OutputStream outStream;
	private DataOutputStream dataOutStream;
	
	public DiscloseServerClientConnection(int index, DiscloseServerClientIdentifier client, DiscloseServer hostServer) throws IOException {
		this.clientIndex = index;
		this.hostServer = hostServer;
		this.client = client;
		this.inStream = this.client.getSocket().getInputStream();
		this.dataInStream = new DataInputStream(this.inStream);
		this.outStream = this.client.getSocket().getOutputStream();
		this.dataOutStream = new DataOutputStream(this.outStream);
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
	
	public void close() throws IOException {
		hostServer.removeClient(clientIndex);
		dataInStream.close();
		inStream.close();
		dataOutStream.close();
		outStream.close();
		client.getSocket().close();
		interrupt();
	}
	
	public void run() {
		while (true) {
			try {
				String in = dataInStream.readUTF();
				if (in.charAt(0) == '/') {
					in = in.substring(1);
					if (in.equals("stop")) {
						close();
						return;
					}
				} else {
					in = in.substring(1);
					System.out.println("Message recieved from client: " + in);
					sendResponseCode(SERVER_RESPONSE_OKAY);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

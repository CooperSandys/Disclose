import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class DiscloseServerClientConnection extends Thread {
	private byte[] id;
	private Socket socket;
	
	public static final int SERVER_RESPONSE_OKAY = 0;
	
	private DiscloseServer hostServer;
	private InputStream inStream;
	private DataInputStream dataInStream;
	private OutputStream outStream;
	private DataOutputStream dataOutStream;
	
	public DiscloseServerClientConnection(byte[] id, Socket socket, DiscloseServer hostServer) throws IOException {
		this.id = id;
		this.socket = socket;
		this.hostServer = hostServer;
		this.inStream = this.socket.getInputStream();
		this.dataInStream = new DataInputStream(this.inStream);
		this.outStream = this.socket.getOutputStream();
		this.dataOutStream = new DataOutputStream(this.outStream);
	}
	
	public byte[] getClientId() { return id; }
	public Socket getSocket() { return socket; }
	
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
		hostServer.removeClient(id);
		dataInStream.close();
		inStream.close();
		dataOutStream.close();
		outStream.close();
		socket.close();
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
					hostServer.broadcastMessageAsString("User " + id + ": " + in, id);
					sendResponseCode(SERVER_RESPONSE_OKAY);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
 }
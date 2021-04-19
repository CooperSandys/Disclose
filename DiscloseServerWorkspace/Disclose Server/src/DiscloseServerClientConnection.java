import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class DiscloseServerClientConnection extends Thread {
	public static final int SERVER_RESPONSE_OKAY = 0;
	private Socket socket;
	private DiscloseServer hostServer;
	private InputStream inStream;
	private DataInputStream dataInStream;
	private OutputStream outStream;
	private DataOutputStream dataOutStream;
	private String username;
	private DiscloseServerClientConnection directMessagee;
	private boolean isDirectMessaging;
	
	public DiscloseServerClientConnection(Socket socket, DiscloseServer hostServer) throws IOException {
		this.socket = socket;
		this.hostServer = hostServer;
		this.inStream = this.socket.getInputStream();
		this.dataInStream = new DataInputStream(this.inStream);
		this.outStream = this.socket.getOutputStream();
		this.dataOutStream = new DataOutputStream(this.outStream);
	}
	
	public Socket getSocket() { return socket; }
	public String getUsername() { return username; }
	
	public boolean connectToMessagee(String user) {
		directMessagee = hostServer.getUserByName(user);
		if (hostServer.getUserByName(user) == null) {
			return false;
		}
		return true;
	}
	
	public String limitStringLength(String str, int len) {
		if (str.length() >= len)
			return str.substring(0, len);
		return str;
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
		hostServer.removeClient(username);
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
						hostServer.updateUsernameList();
						close();
						return;
					} else if (in.split(":")[0].equals("uname")) {
						username = hostServer.verifyUname(in.split(":")[1]);
						hostServer.updateUsernameList();
						sendCommandAsString("uname:" + username);
					} else if (in.split(":")[0].equals("dm")) {
						isDirectMessaging = connectToMessagee(in.split(":")[1]);
						sendCommandAsString("?dm:" + isDirectMessaging);
					} else if (in.split(":")[0].equals("dm/")) {
						isDirectMessaging = false;
					}
				} else {
					in = in.substring(1);
					if (!isDirectMessaging) {
						System.out.println("Message recieved from client " + username + ": " + in);
						hostServer.broadcastMessageAsString(username + ": " + in, username);
						sendResponseCode(SERVER_RESPONSE_OKAY);
					} else {
						directMessagee.sendMessageAsString("(Direct) " + username + ": " + in);
						sendResponseCode(SERVER_RESPONSE_OKAY);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
 }
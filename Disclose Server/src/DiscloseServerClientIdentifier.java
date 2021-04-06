import java.io.IOException;
import java.net.Socket;

public class DiscloseServerClientIdentifier {
	private int id;
	private Socket socket;
	
	public DiscloseServerClientIdentifier(int id, Socket socket) {
		this.id = id;
		this.socket = socket;
	}
	
	public void close() throws IOException { socket.close(); }
	public int getId() { return id; }
	public Socket getSocket() { return socket; }
 }
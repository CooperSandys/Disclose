import java.net.*;
import java.util.*;
import java.io.*;
import java.math.BigInteger;

public class DiscloseServer {
	private ServerSocket serverSocket;
	private List<Socket> connectedClients;
	
	public DiscloseServer(int port) throws IOException {
		this.serverSocket = new ServerSocket(port);
		this.serverSocket.setReuseAddress(true);
		this.connectedClients = new ArrayList<Socket>();
	}
	
	public void acceptConnection() throws IOException {
		Socket newClient = serverSocket.accept();
		connectedClients.add(newClient);
	}
	
	public void close() throws IOException {
		serverSocket.close();
		for (Socket i : connectedClients) {
			i.close();
		}
	}
}

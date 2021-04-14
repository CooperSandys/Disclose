import java.net.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.io.*;

public class DiscloseServer {
	private static int defaultPort = 41337;
	private ServerSocket serverSocket;
	private List<DiscloseServerClientConnection> connectedClients;
	private DiscloseServerConnectionHandler connectionHandler;
	
	public DiscloseServer(int port) throws IOException {
		this.serverSocket = new ServerSocket(port);
		this.serverSocket.setReuseAddress(true);
		this.connectedClients = new ArrayList<DiscloseServerClientConnection>();
		this.connectionHandler = new DiscloseServerConnectionHandler(this);
		connectionHandler.start();
	}
	
	public DiscloseServerClientConnection getClient(int index) {
		return connectedClients.get(index);
	}
	
	public byte[] genClientId() {
		// this returns a UUID (sort of) ~should~ be secure i hope
		byte[] bytes = new byte[16];
		ThreadLocalRandom.current().nextBytes(bytes);
		return bytes;
	}
	
	public void removeClient(byte[] id) {
		System.out.println("User: " + id + " has disconnected.");
		for (int i = 0; i < connectedClients.size(); i++) {
			if (connectedClients.get(i).getClientId() == id) {
				connectedClients.remove(i);
				return;
			}
		}
	}
	
	public void acceptConnection() throws IOException {
		DiscloseServerClientConnection newClient = new DiscloseServerClientConnection(genClientId(), serverSocket.accept(), this);
		newClient.start();
		connectedClients.add(newClient);
	}
	
	public void broadcastMessageAsString(String message, byte[] callingID) throws IOException {
		for (DiscloseServerClientConnection i : connectedClients) {
			if (!i.getClientId().equals(callingID))
				i.sendMessageAsString(message);
		}
	}
	
	public void broadcastCommandAsString(String message, byte[] callingID) throws IOException {
		for (DiscloseServerClientConnection i : connectedClients) {
			if (!i.getClientId().equals(callingID))
				i.sendCommandAsString(message);
		}
	}
	
	public ServerSocket getServerSocket() { return serverSocket; }
	
	public void close() throws IOException {
		connectionHandler.close();
		serverSocket.close();
		for (DiscloseServerClientConnection i : connectedClients) {
			i.close();
		}
	}
	
	public static void main(String[] args) throws IOException {
		Scanner scan = new Scanner(System.in);
		System.out.println("Server is starting...");
		DiscloseServer server = new DiscloseServer(defaultPort);
		System.out.println("Server started at: " + server.getServerSocket().getLocalSocketAddress() + ", waiting for connection.");
		String adminInput;
		while (true) {
			adminInput = scan.nextLine();
			if (adminInput.charAt(0) == '/') {
				adminInput = adminInput.substring(1);
				switch (adminInput) {
					case "stop" : 
						System.out.println("Shutting down server.");
						scan.close();
						server.close();
						System.exit(0);
						break;
					}
			}
		}
	} 
}

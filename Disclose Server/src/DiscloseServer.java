import java.net.*;
import java.util.*;
import java.io.*;

public class DiscloseServer {
	private static int defaultPort = 41337;
	private ServerSocket serverSocket;
	private List<DiscloseServerClientIdentifier> connectedClients;
	
	public DiscloseServer(int port) throws IOException {
		this.serverSocket = new ServerSocket(port);
		this.serverSocket.setReuseAddress(true);
		this.connectedClients = new ArrayList<DiscloseServerClientIdentifier>();
	}
	
	public DiscloseServerClientIdentifier getClient(int index) {
		return connectedClients.get(index);
	}
	
	public void removeClient(int id) {
		System.out.println("User: " + id + " has disconnected.");
		for (int i = 0; i < connectedClients.size(); i++) {
			if (connectedClients.get(i).getId() == id) {
				connectedClients.remove(i);
				return;
			}
		}
	}
	
	public void acceptConnection() throws IOException {
		DiscloseServerClientIdentifier newClient = new DiscloseServerClientIdentifier(connectedClients.size(), serverSocket.accept());
		DiscloseServerClientConnection discloseClient = new DiscloseServerClientConnection(connectedClients.size(), newClient, this);
		discloseClient.start();
		connectedClients.add(newClient);
	}
	
	public ServerSocket getServerSocket() { return serverSocket; }
	
	public void close() throws IOException {
		serverSocket.close();
		for (DiscloseServerClientIdentifier i : connectedClients) {
			i.close();
		}
	}
	
	public static void main(String[] args) throws IOException {
		Scanner scan = new Scanner(System.in);
		System.out.println("Server is starting...");
		DiscloseServer server = new DiscloseServer(defaultPort);
		System.out.println("Server started at: " + server.getServerSocket().getLocalSocketAddress() + ", waiting for connection.");
		DiscloseServerConnectionHandler connectionHandler = new DiscloseServerConnectionHandler(server);
		connectionHandler.start();
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
			} else {
				
			}
		}
	} 
}

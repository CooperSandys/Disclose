import java.net.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.io.*;

public class DiscloseServer {
	private static int defaultPort = 41337;
	private ServerSocket serverSocket;
	private List<DiscloseServerClientConnection> connectedClients;
	
	public DiscloseServer(int port) throws IOException {
		this.serverSocket = new ServerSocket(port);
		this.serverSocket.setReuseAddress(true);
		this.connectedClients = new ArrayList<DiscloseServerClientConnection>();
	}
	
	public DiscloseServerClientConnection getClient(int index) {
		return connectedClients.get(index);
	}
	
	public int genClientId() {
		boolean idFound = false;
		int rand = ThreadLocalRandom.current().nextInt(0, 100000);
		if (connectedClients.size() == 0) {
			return rand;
		}
		else {
			while (true) {
				rand = ThreadLocalRandom.current().nextInt(0, 100000);
				idFound = false;
				for (DiscloseServerClientConnection i : connectedClients) {
					if (i.getId() == rand) {
						idFound = true;
					}
				}
				if (idFound)
					return rand;
			}
		}
	}
	
	public void removeClient(int id) {
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
	
	public void broadcastMessageAsString(String message, int callingID) throws IOException {
		for (DiscloseServerClientConnection i : connectedClients) {
			if (i.getClientId() != callingID)
				i.sendMessageAsString(message);
		}
	}
	
	public void broadcastCommandAsString(String message, int callingID) throws IOException {
		for (DiscloseServerClientConnection i : connectedClients) {
			if (i.getClientId() != callingID)
				i.sendCommandAsString(message);
		}
	}
	
	public ServerSocket getServerSocket() { return serverSocket; }
	
	public void close() throws IOException {
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

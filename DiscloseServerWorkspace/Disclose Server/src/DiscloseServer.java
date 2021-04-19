import java.net.*;
import java.util.*;
import java.io.*;

public class DiscloseServer {
	private static int defaultPort = 41337;
	private ServerSocket serverSocket;
	private List<DiscloseServerClientConnection> connectedClients;
	private DiscloseServerConnectionHandler connectionHandler;
	
	public DiscloseServer(int port) throws IOException {
		this.serverSocket = new ServerSocket();
		this.serverSocket.bind(new InetSocketAddress(defaultPort));
		this.serverSocket.setReuseAddress(true);
		this.connectedClients = new ArrayList<DiscloseServerClientConnection>();
		this.connectionHandler = new DiscloseServerConnectionHandler(this);
		connectionHandler.start();
	}
	
	public void updateUsernameList() throws IOException {
		quicksortUsers(connectedClients, 0, connectedClients.size() - 1);
		String toBroadcast = "";
		for (int s = 0; s < connectedClients.size(); s++) {
			toBroadcast += connectedClients.get(s).getUsername() + ":";
		}
		broadcastCommandAsString("usrlst:" + toBroadcast, "Server");
	}
	
	public void quicksortUsers(List<DiscloseServerClientConnection> A, int low, int high) {
		if (low < high) {
			int partition = partition(A, low, high);
			quicksortUsers(A, low, partition - 1);
			quicksortUsers(A, partition + 1, high);
		}
	}
	
	public int partition(List<DiscloseServerClientConnection> A, int low, int high) {
		DiscloseServerClientConnection pivot = A.get(high);
		int i = low;
		for (int j = low; j < high; j++) {
			if (compareStrings(A.get(j).getUsername(), pivot.getUsername()) == -1) {
				Collections.swap(A, i, j);
				i++;
			}
		}
		Collections.swap(A, i, high);
		return i;
	}
	
	public int compareStrings(String first, String second) { // 1 = first > second, -1 = first < second
		for (int i = 0; i < Math.min(first.length(), second.length()); i++) {
			if (Character.toLowerCase(first.charAt(i)) == Character.toLowerCase(second.charAt(i))) {
				continue;
			} else if (Character.toLowerCase(first.charAt(i)) > Character.toLowerCase(second.charAt(i))) {
				return 1;
			} else if (Character.toLowerCase(first.charAt(i)) < Character.toLowerCase(second.charAt(i))) {
				return -1;
			}
		}
		if (first.length() > second.length()) return 1;
		return -1;
	}
	
	public DiscloseServerClientConnection getClient(int index) {
		return connectedClients.get(index);
	}
	
	public DiscloseServerClientConnection getUserByName(String name) {
		for (int i = 0; i < connectedClients.size(); i++) {
			if (connectedClients.get(i).getUsername().equals(name)) {
				return connectedClients.get(i);
			}
		}
		return null;
	}
	
	public void removeClient(String uname) {
		System.out.println("User: " + uname + " has disconnected.");
		for (int i = 0; i < connectedClients.size(); i++) {
			if (connectedClients.get(i).getUsername() == uname) {
				connectedClients.remove(i);
				return;
			}
		}
	}
	
	public String verifyUname(String uname) {
		String finalName = uname;
		boolean isTaken = true;
		for (int r = 0; r < connectedClients.size(); r++) {
			if (connectedClients.get(r).getUsername() == null) {
				continue;
			} else if (connectedClients.get(r).getUsername().equals(finalName)) {
				isTaken = true;
			}
		}
		if (!isTaken) {
			return finalName;
		}
		
		int counter = 0;
		if (connectedClients.size() == 1) {
			return finalName + "#" + counter;
		}
		counter = -1;
		do {
			counter++;
			isTaken = false;
			for (int y = 0; y < connectedClients.size() - 1; y++) {
				if (connectedClients.get(y).getUsername() == null) {
					continue;
				} else if (connectedClients.get(y).getUsername().equals(finalName + "#" + counter)) {
					isTaken = true;
				}
			}
		} while (isTaken);
		return finalName + "#" + counter;
	}
	
	public void acceptConnection() throws IOException {
		DiscloseServerClientConnection newClient = new DiscloseServerClientConnection(serverSocket.accept(), this);
		newClient.start();
		connectedClients.add(newClient);
	}
	
	public void broadcastMessageAsString(String message, String sender) throws IOException {
		for (DiscloseServerClientConnection i : connectedClients) {
			if (!i.getUsername().equals(sender))
				i.sendMessageAsString(message);
		}
	}
	
	public void broadcastCommandAsString(String message, String sender) throws IOException {
		for (DiscloseServerClientConnection i : connectedClients) {
			if (!i.getUsername().equals(sender))
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
	
	public void run() throws IOException {
		Scanner scan = new Scanner(System.in);
		
		String adminInput;
		while (true) {
			adminInput = scan.nextLine();
			if (adminInput.charAt(0) == '/') {
				adminInput = adminInput.substring(1);
				switch (adminInput) {
					case "stop" : 
						System.out.println("Shutting down server.");
						scan.close();
						close();
						System.exit(0);
						break;
				}
			} else {
				broadcastMessageAsString("Server: " + adminInput, "Admin");
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		System.out.println("Server is starting...");
		DiscloseServer server = new DiscloseServer(defaultPort);
		System.out.println("Server started at: " + server.getServerSocket().toString() + ", waiting for connection.");
		server.run();
	} 
}

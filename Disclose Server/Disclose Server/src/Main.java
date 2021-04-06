import java.io.IOException;
import java.util.*;

public class Main extends Thread {
	private static DiscloseServer server;
	
	public static void main(String[] args) throws IOException {
		Scanner scan = new Scanner(System.in);
		System.out.println("Server is starting...");
		server = new DiscloseServer(6666);
		System.out.println("Server started, waiting for connection.");
		Main connectionThread = new Main();
		connectionThread.start();
		while (true) {
			if (scan.nextLine().equals("stop")) {
				System.out.println("Shutting down server.");
				scan.close();
				server.close();
				System.exit(0);
			}
		}
	} 
	
	public void run() {
		try {
			server.acceptConnection();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Connection made!");
	}
}

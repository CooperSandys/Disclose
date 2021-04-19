import java.io.IOException;

public class DiscloseServerConnectionHandler extends Thread {
	private DiscloseServer server;
	
	public DiscloseServerConnectionHandler (DiscloseServer server) {
		this.server = server;
	}
	
	public void close() {
		interrupt();
	}
	
	public void run() {
		while (true) {
			try {
				server.acceptConnection();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Connection made!");
		}
	}
}

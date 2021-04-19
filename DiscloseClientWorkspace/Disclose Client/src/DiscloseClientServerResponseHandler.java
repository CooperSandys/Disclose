import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class DiscloseClientServerResponseHandler extends Thread {
	private DiscloseClient client;
	private InputStream inStream;
	private DataInputStream dataInStream;
	private boolean running = true;
	
	public DiscloseClientServerResponseHandler(DiscloseClient client, Socket socket) throws IOException {
		this.client = client;
		this.inStream = socket.getInputStream();
		this.dataInStream = new DataInputStream(this.inStream);
	}
	
	public void close() throws IOException {
		inStream.close();
		dataInStream.close();
		this.interrupt();
	}
	
	public void run() {
		String recieved;
		
		while (running) {
			try {
				recieved = dataInStream.readUTF();
				if (recieved.charAt(0) == '!') {
					recieved = recieved.substring(1);
					if (recieved.equals("0")) {
						System.out.println("Server recieved message");
					}
				} else if (recieved.charAt(0) == '/') {
					recieved = recieved.substring(1);
					if (recieved.split(":")[0].equals("?dm")) {
						System.out.println(recieved);
						if (recieved.split(":")[1].equals("true")) {
							client.writeToOutput("You are now in direct communication.");
							client.setIsDming(true);
						} else {
							client.writeToOutput("Could not establish a direct communication.");
							client.setIsDming(false);
						}
					} else if (recieved.split(":")[0].equals("usrlst")) {
						client.updateUsernameList(recieved.substring(7).split(":"));
					} else if (recieved.split(":")[0].equals("uname")) {
						if (client.getShouldWrite()) {
							client.writeUsernameToFile(recieved.split(":")[1].split("#")[0]);
						}
					}
				} else if (recieved.charAt(0) == '>') {
					client.writeToOutput(recieved.substring(1));
				} 
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

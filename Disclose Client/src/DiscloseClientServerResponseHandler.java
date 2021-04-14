import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class DiscloseClientServerResponseHandler extends Thread {
	private InputStream inStream;
	private DataInputStream dataInStream;
	
	public DiscloseClientServerResponseHandler(Socket socket) throws IOException {
		this.inStream = socket.getInputStream();
		this.dataInStream = new DataInputStream(this.inStream);
	}
	
	public void close() throws IOException {
		inStream.close();
		dataInStream.close();
		interrupt();
	}
	
	public void run() {
		String recieved;
		
		while (true) {
			try {
				recieved = dataInStream.readUTF();
				if (recieved.charAt(0) == '!') {
					System.out.println("Server recieved message");
					recieved = recieved.substring(1);
					if (recieved == "0") {
						System.out.println("Server recieved message");
					}
				} else if (recieved.charAt(0) == '/') {
					
				} else if (recieved.charAt(0) == '>') {
					System.out.println(recieved.substring(1));
				} 
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

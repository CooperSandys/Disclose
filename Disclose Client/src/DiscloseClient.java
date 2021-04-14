import java.net.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.io.*;
import java.math.BigInteger;

public class DiscloseClient {
	private static int defaultPort = 41337;
	private static int maxUsernameLength = 30;
	private Socket socket;
	private OutputStream outStream;
	private DataOutputStream dataOutStream;
	private DiscloseClientServerResponseHandler clientHandler;
	private File usrInfo;
	private BigInteger[] privateKey;
	private BigInteger[] publicKey;
	
	public DiscloseClient() throws IOException {
		System.out.print("Welcome! Please enter the server you would like to connect to: ");
		Scanner scan = new Scanner(System.in);
		this.socket = new Socket(scan.nextLine(), defaultPort);
		scan.close();
		this.outStream = this.socket.getOutputStream();
		this.dataOutStream = new DataOutputStream(this.outStream);
		this.clientHandler = new DiscloseClientServerResponseHandler(this.socket);
		if (!checkIfCredentialsSaved()) {
			genCredentials();
		}
		genKeys();
	}
	
	public boolean checkIfCredentialsSaved() throws FileNotFoundException {
		usrInfo = new File("usrinfo/usrcred.txt");
		Scanner usrInfoScan = new Scanner(usrInfo);
		if (!usrInfoScan.hasNextLine()) {
			usrInfoScan.close();
			return false;
		}
		usrInfoScan.close();
		return true;
	}
	
	public void genCredentials() throws IOException {
		Scanner userIn = new Scanner(System.in);
		FileWriter credWriter = new FileWriter(usrInfo);
		System.out.print("This seems to be your first time using this program, please enter a username: ");
		credWriter.write("uname:" + limitStringLength(userIn.nextLine(), maxUsernameLength));
		System.out.print("\n"); 
		userIn.close();
		credWriter.close();
	}
	
	public String limitStringLength(String str, int len) {
		if (str.length() >= len)
			return str.substring(0, len);
		return str;
	}
	
	public void genKeys() {
		int numOfBits = 4096;
		BigInteger p;
		BigInteger q;
		while (true) {
			p = BigInteger.probablePrime(numOfBits / 2, ThreadLocalRandom.current());
			if (!p.isProbablePrime(100)) {
				continue;
			} else {
				break;
			}
		}
		while (true) {
			q = BigInteger.probablePrime(numOfBits / 2, ThreadLocalRandom.current());
			if (!q.isProbablePrime(100)) {
				continue;
			} else {
				break;
			}
		}
		BigInteger n = p.multiply(q);
		BigInteger e = new BigInteger("65537");
		BigInteger d = e.modInverse(p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE)));
		publicKey = new BigInteger[2];
		publicKey[0] = n;
		publicKey[1] = e;
		privateKey = new BigInteger[2];
		privateKey[0] = d;
		privateKey[1] = e;
	}
	
	public void connect(String server, int port) throws IOException {
		socket = new Socket(server, port);
		outStream = socket.getOutputStream();
		dataOutStream = new DataOutputStream(outStream);
	}
	
	public void close() throws IOException {
		sendCommandAsString("stop");
		dataOutStream.close();
		outStream.close();
		clientHandler.close();
		socket.close();
	}
	
	public void sendMessageAsString(String data) throws IOException {
		dataOutStream.writeUTF(">" + data);
		dataOutStream.flush();
	}
	
	public void sendCommandAsString(String data) throws IOException {
		dataOutStream.writeUTF("/" + data);
		dataOutStream.flush();
	}
	
	public void sendResponseCode(int responseCode) throws IOException {
		dataOutStream.writeUTF("!" + responseCode);
		dataOutStream.flush();
	}
	
	public void run() throws IOException {
		clientHandler.start();
		Scanner scan = new Scanner(System.in);
		while (true) {
			String in = scan.nextLine();
			if (in.equals("/stop")) {
				System.out.println("Shutting down client.");
				scan.close();
				close();
				System.exit(0);
			} else {
				sendMessageAsString(in);
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		DiscloseClient client = new DiscloseClient();
		client.run();
		// nothing after this will run :/ OH CAUSE IS A FUCNTIOn hahaha
	}
}

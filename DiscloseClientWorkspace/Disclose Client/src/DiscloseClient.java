import java.net.*;
import java.util.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import java.io.*;
import javax.swing.*;

public class DiscloseClient implements ActionListener {
	private static int defaultPort = 41337;
	private static int maxUsernameLength = 30;
	private String serverIp;
	private Socket socket;
	private OutputStream outStream;
	private DataOutputStream dataOutStream;
	private DiscloseClientServerResponseHandler clientHandler;
	private Scanner sc;
	private static String username;
	private JFrame window;
	private JTextArea consoleOutput;
	private String consoleText = "";
	private JTextArea usersList;
	private JTextField userInput;
	private JButton sendButton;
	private boolean isConnectedToServer = false;
	private boolean isUsernameSet = false;
	private boolean isDirectMessaging = false;
	private boolean shouldWriteToFile = true;
	private File usrCredFile;
	private BufferedReader usrCredReader;
	private FileWriter usrCredWriter;
	
	public DiscloseClient() throws IOException {
        window = new JFrame("Disclose");
        window.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                try {
                	if (isConnectedToServer) {
                		close();
                	}
					System.exit(0);
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        });
        window.setResizable(true);
        window.setSize(1024, 768);
        JPanel panel = new JPanel();
        JLabel sendLabel = new JLabel("Enter Text");
        userInput = new JTextField(50);
        sendButton = new JButton("Send");
        sendButton.setActionCommand("send");
        sendButton.addActionListener(this);
        panel.add(sendLabel);
        panel.add(userInput);
        panel.add(sendButton);
        consoleOutput = new JTextArea("This is where output stuffs goes");
        consoleOutput.setEditable(false);
        JScrollPane scroll = new JScrollPane(consoleOutput, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(500, 500));
        usersList = new JTextArea("this is where ppl go");
        usersList.setEditable(false);
        JScrollPane userScroll = new JScrollPane(usersList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        userScroll.setPreferredSize(new Dimension(200, 10));
        consoleOutput.setEditable(false);
        window.getContentPane().add(BorderLayout.LINE_START, userScroll);
        window.getContentPane().add(BorderLayout.CENTER, scroll);
        window.getContentPane().add(BorderLayout.SOUTH, panel);
        window.setVisible(true);
        
        usrCredFile = new File("usrcred.txt");
        usrCredReader = new BufferedReader(new FileReader(usrCredFile));
        usrCredWriter = new FileWriter(usrCredFile, true);
        
		this.sc = new Scanner(System.in);
		updateUsernameList(new String[] {});
		
		writeToOutput("Welcome to Disclose! Please enter the server you would like to connect to: ");
	}
	
	public boolean getShouldWrite() {
		return shouldWriteToFile;
	}
	
	public void writeUsernameToFile(String toWrite) throws IOException {
		usrCredWriter.write(serverIp + ":" + toWrite + "\n");
		usrCredWriter.flush();
	}
	
	public void updateUsernameList(String[] users) {
		String userText = "";
		for (String i : users) {
			userText += i + "\n";
		}
		usersList.setText(userText);
	}
	
	public void setIsDming(boolean isDming) {
		isDirectMessaging = isDming;
	}
	
	public void actionPerformed(ActionEvent e) {
		try {
			if ("send".equals(e.getActionCommand())) {
				String in = userInput.getText();
				if (isConnectedToServer == false) {
					String lineIn;
					while ((lineIn = usrCredReader.readLine()) != null) {
						if (lineIn.split(":")[0].equals(in)) {
							try {
								connect(in);
							} catch (IOException i) {
								writeToOutput("Error uhoh #1");
								userInput.setText("");
								return;
							}
							writeToOutput("Connected!");
							shouldWriteToFile = false;
							genResponseHandler();
							try {
								username = lineIn.split(":")[1];
								writeToOutput("Welcome " + username + "!");
								sendCommandAsString("uname:" + username);
							} catch (IOException i) {
								writeToOutput("Error uhoh #2");
								userInput.setText("");
								return;
							}
							isConnectedToServer = true;
							isUsernameSet = true;
							userInput.setText("");
							return;
						}
					}
					try {
						connect(in);
					} catch (IOException i) {
						writeToOutput("That server could not be found!");
						userInput.setText("");
						return;
					}
					writeToOutput("Connected!");
					genResponseHandler();
					isConnectedToServer = true;
					writeToOutput("Please enter a username: ");
				} else if (isUsernameSet == false) {
					username = limitStringLength(in, maxUsernameLength);
					sendCommandAsString("uname:" + username);
					isUsernameSet = true;
					writeToOutput("Welcome " + username + "!");
				} else if (in.equals("/stop")) {
					writeToOutput("Shutting down client.");
					close();
					System.exit(0);
				} else if (in.split(" ")[0].equals("/dm") && !isDirectMessaging) {
					writeToOutput("Attempting to connect to: " + in.split(" ")[1]);
					sendCommandAsString("dm:" + in.split(" ")[1]);
				} else if (in.split(" ")[0].equals("/dm") && isDirectMessaging) {
					writeToOutput("Disconnecting from direct communication");
					sendCommandAsString("dm/");
				} else {
					writeToOutput("(You): " + in);
					sendMessageAsString(in);
				}
				userInput.setText("");
			}
		} catch (IOException i) {
			i.printStackTrace();
		}
	}
	
	public void writeToOutput(String toWrite) {
		consoleText = consoleText + toWrite + "\n";
		consoleOutput.setText(consoleText);
	}
	
	public String limitStringLength(String str, int len) {
		if (str.length() >= len)
			return str.substring(0, len);
		return str;
	}
	
	public void connect(String server) throws IOException {
		System.out.println(server);
		socket = new Socket(server, defaultPort);
		serverIp = server;
		outStream = socket.getOutputStream();
		dataOutStream = new DataOutputStream(outStream);
		clientHandler = new DiscloseClientServerResponseHandler(this, this.socket);
		clientHandler.start();
	}
	
	public void close() throws IOException {
		sendCommandAsString("stop");
		dataOutStream.close();
		outStream.close();
		clientHandler.close();
		sc.close();
		socket.close();
		usrCredWriter.close();
		usrCredReader.close();
	}
	
	public void genResponseHandler() throws IOException {
		this.clientHandler = new DiscloseClientServerResponseHandler(this, this.socket);
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
	
	public static void main(String[] args) throws IOException {
		@SuppressWarnings("unused")
		DiscloseClient client = new DiscloseClient();
	}
}

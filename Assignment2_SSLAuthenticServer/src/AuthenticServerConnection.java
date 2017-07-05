
/**
 * Author: Hao Yu Yeh
 * Date: 2016¦~10¤ë12¤é
 * Project: Assignment2 of Distributed System
 * Comment: this class is used to process the communications between servers
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import javax.net.ssl.SSLSocket;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class AuthenticServerConnection extends Thread {

	private SSLSocket clientSocket;
	private BufferedReader reader;
	private BufferedWriter writer;
	private int clientNum;

	public AuthenticServerConnection(SSLSocket clientSocket, int clientNum) {
		try {
			this.clientSocket = clientSocket;
			reader = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream(), "UTF-8"));
			writer = new BufferedWriter(new OutputStreamWriter(
					clientSocket.getOutputStream(), "UTF-8"));
			this.clientNum = clientNum;
			/* Specify the keystore details (this can be specified as VM arguments as well)
			   the keystore file contains an application's own certificate and private key
			   keytool -genkey -keystore <keystorename> -keyalg RSA 
			*/
			// for jar file
			String path = AuthenticServerConnection.class.getResource("").getPath().replaceAll("%20", " ").replaceAll("/bin", "")+"mykeystore";
			System.setProperty("javax.net.ssl.keyStore", path);
			// for eclipse run
//			System.setProperty("javax.net.ssl.keyStore", "lib/mykeystore");
			// Password to access the private key from the keystore file
			System.setProperty("javax.net.ssl.keyStorePassword", "19831010");

			// Enable debugging to view the handshake and communication which
			// happens between the SSLClient and the SSLServer
			System.setProperty("javax.net.debug", "all");
			//Location of the Java keystore file containing the collection of 
			//certificates trusted by this application (trust store).
			// for jar file
			System.setProperty("javax.net.ssl.trustStore", path);
			// for eclipse run
//			System.setProperty("javax.net.ssl.trustStore", "lib/mykeystore");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {

		try {

			// System.out.println(Thread.currentThread().getName()
			// + " - Reading messages from client's " + clientNum + "
			// connection");

			String clientMsg = null;
			while ((clientMsg = reader.readLine()) != null) {
				// System.out.println(Thread.currentThread().getName()
				// + " - Message from client " + clientNum + " received: " +
				// clientMsg);

				// process msg from chat server
				processMessage(clientMsg);

			}

			clientSocket.close();
			// System.out.println(Thread.currentThread().getName()
			// + " - Client " + clientNum + " disconnected");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Needs to be synchronized because multiple threads can be invoking this
	// method at the same
	// time
	public synchronized void write(String msg) {
		try {
			writer.write(msg + "\n");
			writer.flush();
			// System.out.println(Thread.currentThread().getName() + " - Message
			// sent to client " + clientNum);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void processMessage(String msg) {
		// msg is in JSON format
		JSONParser parser = new JSONParser();
		try {
			JSONObject jObj = (JSONObject) parser.parse(msg);
			switch ((String) jObj.get("type")) {
			case "authentication":
				String uName = (String) jObj.get("username");
				String pw = (String) jObj.get("password");
				if (AuthenticServerState.getInstance().checkLogin(uName, pw)) {
					// log in successfully
					JSONObject uMessage = new JSONObject();
					uMessage.put("type", "authentication");
					uMessage.put("approved", "true");
					this.write(uMessage.toJSONString());
					System.out.println("log in successfully");
					// direct the client to the chat server
					directClientToChatServer();
				} else {
					JSONObject uMessage = new JSONObject();
					uMessage.put("type", "authentication");
					uMessage.put("approved", "false");
					this.write(uMessage.toJSONString());
					System.out.println("log in failed");
				}
				break;
			case "addserver":
				String sID = (String) jObj.get("serverid");
				String sAddr = (String) jObj.get("serveraddress");
				int cPort = Integer.parseInt((String)jObj.get("clientport"));
				int sPort = Integer.parseInt((String) jObj.get("serverPort"));
				AuthenticServerState.getInstance().addChatServer(new ChatServer(sID,sAddr,cPort,sPort));
				break;
			default:
				System.out.println("unknown contents");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * distributing clients equally to chat servers
	 */
	@SuppressWarnings("unchecked")
	private void directClientToChatServer(){
		ArrayList<ChatServer> serverList = new ArrayList<ChatServer>();
		serverList = AuthenticServerState.getInstance().getServerList();
		if(AuthenticServerState.count < serverList.size()){
			JSONObject uMessage = new JSONObject();
			uMessage.put("type", "tochatserver");
			uMessage.put("host", serverList.get(AuthenticServerState.count).getServerAddr());
			uMessage.put("port", Integer.toString(serverList.get(AuthenticServerState.count).getClientPort()));
			this.write(uMessage.toJSONString());
			AuthenticServerState.count += 1; 
		}else{
			AuthenticServerState.count = 0;
			JSONObject uMessage = new JSONObject();
			uMessage.put("type", "tochatserver");
			uMessage.put("host", serverList.get(AuthenticServerState.count).getServerAddr());
			uMessage.put("port", Integer.toString(serverList.get(AuthenticServerState.count).getClientPort()));
			this.write(uMessage.toJSONString());
		}
	}
}


/**
 * Author: Hao Yu Yeh
 * Date: 2016¦~10¤ë12¤é
 * Project: Assignment2 of Distributed System
 * Comment: this class is used to validate the client's log in
 */

import java.io.IOException;
import java.util.Scanner;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public class RunAuthenticServer {

	public static void main(String[] args) {

		/*
		 * Specify the keystore details (this can be specified as VM arguments
		 * as well) the keystore file contains an application's own certificate
		 * and private key keytool -genkey -keystore <keystorename> -keyalg RSA
		 */
		// for jar file
		String path = RunAuthenticServer.class.getResource("").getPath()
				.replaceAll("%20", " ").replaceAll("/bin", "") + "mykeystore";
		System.setProperty("javax.net.ssl.keyStore", path);
		// for eclipse run
		// System.setProperty("javax.net.ssl.keyStore", "lib/mykeystore");
		// Password to access the private key from the keystore file
		System.setProperty("javax.net.ssl.keyStorePassword", "19831010");

		// Enable debugging to view the handshake and communication which
		// happens between the SSLClient and the SSLServer
		System.setProperty("javax.net.debug", "all");
		// Location of the Java keystore file containing the collection of
		// certificates trusted by this application (trust store).
		// for jar file
		System.setProperty("javax.net.ssl.trustStore", path);
		// for eclipse run
		// System.setProperty("javax.net.ssl.trustStore", "lib/mykeystore");

		// add users into system
		User u = new User("admin", "0000");
		AuthenticServerState.getInstance().addUser(u);
		User u1 = new User("admin1", "0000");
		AuthenticServerState.getInstance().addUser(u1);
		User u2 = new User("admin2", "0000");
		AuthenticServerState.getInstance().addUser(u2);

		String serverID = "", serverAddr = "", serverConfigPath = "";
		int port = 4444;
		// get arguments from command line
		// Object that will store the parsed command line arguments
		CmdLineArgs argsBean = new CmdLineArgs();
		// Parser provided by args4j
		CmdLineParser parser = new CmdLineParser(argsBean);
		try {
			// Parse the arguments
			parser.parseArgument(args);
			// After parsing, the fields in argsBean have been updated with the
			// given
			// command line arguments
			serverID = argsBean.getHost();
			serverConfigPath = argsBean.getServerConfigPath();
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			// Print the usage to help the user understand the arguments
			// expected
			// by the program
			parser.printUsage(System.err);
		}
		// load server's configuration
		try {
			Scanner read;
			read = new Scanner(new FileInputStream(serverConfigPath));
			String str = "";
			// read all the data in the file
			while (read.hasNextLine()) {
				str = read.nextLine();
				String[] content;
				content = str.split("\t");
				if (content.length == 3) {
					// authentic server
					if (str.startsWith(serverID)) {
						serverAddr = content[1];
						port = Integer.parseInt(content[2]);
					}
				} else if (content.length == 4) {
					// maintain the chat servers' list
					AuthenticServerState.getInstance()
							.addChatServer(new ChatServer(content[0],
									content[1], Integer.parseInt(content[2]),
									Integer.parseInt(content[3])));
				}
			}
			read.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// listen for port
		SSLServerSocket sslserversocket = null;
		try {
			// create a thread to check the condition of each other chat server
			// regularly
			// first arg is the time for set up all servers
			// second arg is the interval for check server's existence
			// the unit of two args are milliseconds
			CheckChatServerCondition ccsc = new CheckChatServerCondition(60000,
					10000);
			ccsc.setName("CheckChatServersAlive");
			ccsc.start();

			// Create a SSL server socket listening on port for chat clients
			SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory
					.getDefault();
			sslserversocket = (SSLServerSocket) sslserversocketfactory
					.createServerSocket();
			sslserversocket.bind(new InetSocketAddress(serverAddr, port));
			System.out.println(
					Thread.currentThread().getName() + " - Server listening on "
							+ port + " for chat clients' connection");

			int clientNum = 0;

			// Listen for incoming connections for ever
			while (true) {

				// Accept an incoming client connection request
				SSLSocket clientSocket = (SSLSocket) sslserversocket.accept();
				System.out.println(Thread.currentThread().getName()
						+ " - Client conection accepted");
				clientNum++;

				// Create a client connection to listen for and process all the
				// messages
				// sent by the client
				AuthenticServerConnection clientConnection = new AuthenticServerConnection(
						clientSocket, clientNum);
				clientConnection.setName("Thread" + clientNum);
				clientConnection.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (sslserversocket != null) {
				try {
					sslserversocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}

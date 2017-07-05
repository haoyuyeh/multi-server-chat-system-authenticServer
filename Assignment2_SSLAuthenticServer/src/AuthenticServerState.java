
/**
 * Author: Hao Yu Yeh
 * Date: 2016¦~10¤ë12¤é
 * Project: Assignment2 of Distributed System
 * Comment: this class store the lists of usernames and corresponding passwords
 * 			as well as all the functions that a server needs 
 * 			when server processes the messages from clients
 */

import java.util.ArrayList;

//Singleton object that manages the server state
public class AuthenticServerState {

	private static AuthenticServerState instance;
	public static int count;
	private ArrayList<ChatServer> serverList;
	private ArrayList<User> userList;

	private AuthenticServerState() {
		count = 0;
		serverList = new ArrayList<ChatServer>();
		userList = new ArrayList<User>();
	}

	public static synchronized AuthenticServerState getInstance() {
		if (instance == null) {
			instance = new AuthenticServerState();
		}
		return instance;
	}
	
	public synchronized void addChatServer(ChatServer s) {
		serverList.add(s);
	}

	public synchronized void deleteChatServer(ChatServer s) {
		serverList.remove(s);
	}
	
	public synchronized ArrayList<ChatServer> getServerList() {
		return serverList;
	}

	/**
	 * add user into system
	 * @param u
	 */
	public synchronized void addUser(User u) {
		userList.add(u);
	}
	
	/**
	 * return true means log in successfully
	 * @param uName
	 * @param pw
	 * @return
	 */
	public synchronized boolean checkLogin(String uName, String pw) {
		// check validation of log in
		System.out.println(uName+ " " + pw);
		for (User u : userList) {
			System.out.println(uName+ " " + pw);
			System.out.println(u.getUserName()+ " " + u.getPassword());
			if (u.getUserName().equals(uName) && u.getPassword().equals(pw)) {
				return true;
			}
		}
		// log in failed
		return false;
	}
}

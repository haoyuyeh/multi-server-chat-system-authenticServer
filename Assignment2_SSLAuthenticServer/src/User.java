/**
 * Author: Hao Yu Yeh 
 * Date: 2016¦~10¤ë12¤é 
 * Project: Assignment2 of Distributed System 
 * Comment: this class store the configuration of user
 */

public class User {
	private String userName, password;


	public User(String uName, String pw) {
		userName = uName;
		password = pw;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}
}

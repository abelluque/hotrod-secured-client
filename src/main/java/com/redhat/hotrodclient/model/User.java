package com.redhat.hotrodclient.model;

import java.io.Serializable;

public class User implements Serializable {

	private static final long serialVersionUID = -6965601136887211042L;

	private String userName;
	
	private String password;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}

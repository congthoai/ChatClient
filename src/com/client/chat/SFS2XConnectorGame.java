package com.client.chat;

import java.util.Collection;
import java.util.Scanner;

import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSException;

import sfs2x.client.SmartFox;
import sfs2x.client.core.BaseEvent;
import sfs2x.client.core.SFSEvent;
import sfs2x.client.requests.ExtensionRequest;
import sfs2x.client.requests.LoginRequest;
import sfs2x.client.util.ConfigData;

public class SFS2XConnectorGame {
	SmartFox sfs;
	ConfigData cfg;
	String username = "";

	public SFS2XConnectorGame(String username) {
		this.username = username;
		// Configure client connection settings
		cfg = new ConfigData();
		cfg.setPort(Constant.SERVER.PORT);
		cfg.setZone(Constant.SERVER.ZONE);
		cfg.setDebug(false);

		// Set up event handlers
		sfs = new SmartFox();
		sfs.addEventListener(SFSEvent.CONNECTION, this::onConnection);
		sfs.addEventListener(SFSEvent.CONNECTION_LOST, this::onConnectionLost);
		sfs.addEventListener(SFSEvent.LOGIN, this::onLogin);
		sfs.addEventListener(SFSEvent.LOGIN_ERROR, this::onLoginError);
		sfs.addEventListener(SFSEvent.EXTENSION_RESPONSE, this::onExtensionResponse);

		// Connect to server
		sfs.connect(cfg);
	}

	private void onConnection(BaseEvent evt) {
		boolean success = (boolean) evt.getArguments().get("success");

		if (success) {
			System.out.println("Connection success");
			sfs.send(new LoginRequest(username));
		} else
			System.out.println("Connection Failed. Is the server running?");

	}

	private void onConnectionLost(BaseEvent evt) {
		System.out.println("-- Connection lost --");
	}

	private void onLogin(BaseEvent evt) {
		System.out.println("Logged in as: " + sfs.getMySelf().getName());
		actionSend("join", -1);
	}

	private void onLoginError(BaseEvent evt) {
		String message = (String) evt.getArguments().get("errorMessage");
		System.out.println("Login failed. Cause: " + message);
	}

	private void onExtensionResponse(BaseEvent evt) throws SFSException {
		String cmd = (String) evt.getArguments().get("cmd");
		System.out.println("CMD: " + cmd);
		ISFSObject responseParams = (SFSObject) evt.getArguments().get("params");

		switch (cmd) {

		case "match":
			actionReceive(responseParams.getInt("serverRandomNumber"));
			break;
			
		case "notMatch":
			actionReceive(responseParams.getInt("serverRandomNumber"));
			break;

		case "win":
			showWinner(responseParams.getIntArray("winnerList"));
			break;

		case "logout":
			logOut();
			break;
		}
	}

	private void actionSend(String type, int numberServer) {
		SFSObject obj = new SFSObject();
		obj.putUtfString("actionType", type);	
		obj.putInt("playerChoice", numberServer);		
		sfs.send(new ExtensionRequest("actionSend", obj));	
	}

	private void actionReceive(Integer serverRandomNumber) {	
		System.out.println("New random from server: " + serverRandomNumber);
		actionSend("send" , serverRandomNumber);
	}

	private void showWinner(Collection<Integer> collection) {
		collection.forEach(item -> {
			System.out.printf("Winner:  %s\n", item);		
		});
		
		System.out.println("Logout: " + sfs.getMySelf().getName());
		logOut();
	}
	
	private void logOut() {
		sfs.removeAllEventListeners();
		sfs.disconnect();
	}
	
	public int getRandomNumber(int min, int max) {
	    return (int) ((Math.random() * (max - min)) + min);
	}

	public static void main(String[] args) throws InterruptedException {
		System.out.println("Enter username: \t");
		Scanner scan = new Scanner(System.in);
		String username = scan.nextLine();
		SFS2XConnectorGame sfsConn = new SFS2XConnectorGame(username);	
	}
}

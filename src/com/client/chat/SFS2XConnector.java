package com.client.chat;

import java.util.Scanner;

import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSException;

import sfs2x.client.SmartFox;
import sfs2x.client.core.BaseEvent;
import sfs2x.client.core.SFSEvent;
import sfs2x.client.entities.Room;
import sfs2x.client.entities.User;
import sfs2x.client.requests.ExtensionRequest;
import sfs2x.client.requests.JoinRoomRequest;
import sfs2x.client.requests.LoginRequest;
import sfs2x.client.requests.PublicMessageRequest;
import sfs2x.client.util.ConfigData;

public class SFS2XConnector
{
    SmartFox sfs;
    ConfigData cfg;
    String username = "";
 
    public SFS2XConnector(String username)
    {
    	this.username = username;
        // Configure client connection settings
        cfg = new ConfigData();
        cfg.setHost(Constant.SERVER.HOST);
        cfg.setPort(Constant.SERVER.PORT);
        cfg.setZone(Constant.SERVER.ZONE);
        cfg.setDebug(false);
 
        // Set up event handlers
        sfs = new SmartFox();
        sfs.addEventListener(SFSEvent.CONNECTION, this::onConnection);
        sfs.addEventListener(SFSEvent.CONNECTION_LOST, this::onConnectionLost);
        sfs.addEventListener(SFSEvent.LOGIN, this::onLogin);
        sfs.addEventListener(SFSEvent.LOGIN_ERROR, this::onLoginError);
        sfs.addEventListener(SFSEvent.ROOM_JOIN, this::onRoomJoin);
        sfs.addEventListener(SFSEvent.ROOM_JOIN_ERROR, this::onRoomJoinErr);
        sfs.addEventListener(SFSEvent.EXTENSION_RESPONSE, this::onExtensionResponse);
        sfs.addEventListener(SFSEvent.PUBLIC_MESSAGE, this::onShowMessage);
 
        // Connect to server
        sfs.connect(cfg);
    }
    
    private void onConnection(BaseEvent evt)
    {
        boolean success = (boolean) evt.getArguments().get("success");
     
        if (success)
        {
            System.out.println("Connection success");
            sfs.send(new LoginRequest(username));
        }
        else
            System.out.println("Connection Failed. Is the server running?");
     
    }
     
    private void onConnectionLost(BaseEvent evt)
    {
        System.out.println("-- Connection lost --");
    }
     
    private void onLogin(BaseEvent evt)
    {
        System.out.println("Logged in as: " + sfs.getMySelf().getName());
        sfs.send(new JoinRoomRequest("RoomA"));
        
        ISFSObject params = new SFSObject();
        params.putUtfString("username", sfs.getMySelf().getName());
        params.putUtfString("message", "New Member... ");
        sfs.send(new ExtensionRequest("chatting", params));
    }
     
    private void onLoginError(BaseEvent evt)
    {
        String message = (String) evt.getArguments().get("errorMessage");
        System.out.println("Login failed. Cause: " + message);
    }
    
    private void onRoomJoin(BaseEvent evt) {
    	Room room = (Room) evt.getArguments().get("room");
        System.out.println("Joined Room: " + room.getName());
    }
    
    private void onRoomJoinErr(BaseEvent evt) {
    	 System.out.println("Room creation failed: " + evt.getArguments().get("errorMessage"));
    }
    
    private void onSendMessage() {
    	System.out.println("Enter message: \t");
    	Scanner scan = new Scanner(System.in);
        String message = scan.nextLine();
        
        sfs.send(new PublicMessageRequest(message));       
    }
    
    private void onShowMessage(BaseEvent evt) 
    {
    	 User sender = (User)evt.getArguments().get("sender");
         
         if (sender == sfs.getMySelf()) {
        	 System.out.println("Me: " + evt.getArguments().get("message"));
         } else {
        	 System.out.printf("%s: %s\n", sender.getName(), evt.getArguments().get("message"));
         }
    }
    
    private void onExtensionResponse(BaseEvent evt) throws SFSException
    {
		if (evt.getArguments().get("cmd").equals("chatting")) {
			ISFSObject responseParams = (SFSObject) evt.getArguments().get("params");			
			ISFSObject chatModel = responseParams.getSFSObject("ChatModel");
			
			if (sfs.getMySelf().getName().equals(chatModel.getUtfString("username"))) {
				System.out.println("Me: " + chatModel.getUtfString("message"));
			} else {
				System.out.printf("%s: %s\n", chatModel.getUtfString("username"), chatModel.getUtfString("message")); 
			}			
		}
    }
    
    private void onSendMessage2() {
    	//System.out.println("Enter message: \t");
    	Scanner scan = new Scanner(System.in);
        ISFSObject params = new SFSObject();
        params.putUtfString("username", sfs.getMySelf().getName());
        params.putUtfString("message", scan.nextLine());
        sfs.send(new ExtensionRequest("chatting", params));
    }
     
    public static void main(String[] args) throws InterruptedException
    {
    	System.out.println("Enter username: \t");
    	Scanner scan = new Scanner(System.in);
        String username = scan.nextLine();
        SFS2XConnector sfsConn = new SFS2XConnector(username);
        Thread.sleep(1000);
        
    	while(true) {
            sfsConn.onSendMessage2();
            Thread.sleep(1000);
    	}
    }
}

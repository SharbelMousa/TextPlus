package com.win7.example.textplus.interfaces;

import java.io.UnsupportedEncodingException;
//an interface that conntain all the methods that thee manager r the mservice must have so we dont forget any method

 public interface Manager {
	
	public String getUsername();//getting theuser name for the account we already singed in
	public String sendMessage(String username,String tousername, String message) throws UnsupportedEncodingException;//sending the message that the user tybed by making all the varibel we want to send in one string and send it to the socket so the socket send it to the php file (the server )
	public String authenticateUser(String usernameText, String passwordText) throws UnsupportedEncodingException; //this we be acyive when we do sign in successfuly upating the username and passs for the account we singed in and getting his friends list and updating all the frineds lis and messages that this user have
	public void messageReceived(String username, String message);//this method is the method that will be acyive when we rcive a message from another user
	public boolean isNetworkConnected();//cheking if we are connected to a network or not
	public boolean isUserAuthenticated();///if we are loged in to a user or not
	public void exit();//exiting and shutting down the service and the connection
	public String signUpUser(String usernameText, String passwordText, String email);//signing up the user by making all the varibels in one string and send it to the server
	public String addNewFriendRequest(String friendUsername);//sending a friend request by getting the user name we want to add and all the varibel we need and send them to the sever
	public String sendFriendsReqsResponse(String approvedFriendNames,String discardedFriendNames);//responding to all the friends request by sending al the frineds we want to approve and the ones we dont want to aprove to the server so we can start chatting or delete there request

	
}

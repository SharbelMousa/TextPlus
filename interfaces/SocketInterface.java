package com.win7.example.textplus.interfaces;
///an interface that holdes the methods that the socket must have so we can do the connectoin and have a well socket that is match with our app
public  interface SocketInterface {

	public String sendHttpRequest(String params);//send http request by sending a string to the server by the socket in another meaing send the string that have our data we want to send to the server wiht this method
	public int startListening(int port); //start to listing to the port and starting the socker connection between the app an the server
	public void stopListening();//stoping the connection and stoping to listen to the port
	public void exit();///exiting the socket and deleting and shuutting all the sockets down
	public int getListeningPort();//getting the port we are listining to

}

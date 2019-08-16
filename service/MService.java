package com.win7.example.textplus.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.win7.example.textplus.MainLogin;
import com.win7.example.textplus.R;
import com.win7.example.textplus.WritingMessage;
import com.win7.example.textplus.communacation.Socketing;
import com.win7.example.textplus.interfaces.Manager;
import com.win7.example.textplus.interfaces.SocketInterface;
import com.win7.example.textplus.interfaces.Updater;
import com.win7.example.textplus.toolBox.FriendController;
import com.win7.example.textplus.toolBox.DataStorage;
import com.win7.example.textplus.toolBox.MessageController;
import com.win7.example.textplus.toolBox.XmlHandler;
import com.win7.example.textplus.Data_type.FriendsInformation;
import com.win7.example.textplus.Data_type.MessagesInformation;

//it must have the updata data methid and the methods int he manager interface
public class MService extends Service implements Manager, Updater {

	///the user name
	public static String USERNAME;
	public static final String TAKE_MESSAGE = "Take_Message";
	public static final String FRIEND_LIST_UPDATED = "Take Friend List";
	public static final String MESSAGE_LIST_UPDATED = "Take Message List";
	public ConnectivityManager conManager = null; 
	private final int UPDATE_TIME = 10000;//the time that is to update the data (the time we send request to update or send request to see if the is a new thing in the server or to get a thing from the server )
//its like a timer to every 12000 he do a sync
	private String rawFriendList = new String();//row to hold the frind from the frinedlist in a string
	private String rawMessageList = new String();//row to hold the message romt he message list in a string

	SocketInterface socketOperator = new Socketing(this);///new socket from the sockting class

	private final IBinder mBinder = new IMBinder();
	private String username;//string to hold the user name
	private String password;//string to hold the usr password
	private boolean authenticatedUser = false;//if the user is loged invor not
	 // timer to take the updated data from server
	private Timer timer;//a timer
	
// sqllite data base from the calss we made
	private DataStorage localstoragehandler; 
	//a notfacation opeject
	private NotificationManager mNM;
///
	public class IMBinder extends Binder {
	//get this service (mservice )from the  ibindr
		public Manager getService() {
			return MService.this;
		}
		
	}
	   
    @Override
    public void onCreate() 
    {   	
         mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

         localstoragehandler = new DataStorage(this);
        // showing  notification about  starting.  We put an icon in the status bar.
    	conManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    	new DataStorage(this);
    	
    	// Timer is used to take the friend List information every UPDATE_TIME;
		timer = new Timer();   
		
		Thread thread = new Thread()
		{
			@Override
			public void run() {			
				
				/*
				the while run when we are cant listen  to a port 10 times if we cuoldnt listen to any random port
				if this happe  10 times we break
				*/
				Random random = new Random();
				int tryCount = 0;
				while (socketOperator.startListening(10000 + random.nextInt(20000))  == 0 )
				{		
					tryCount++; 
					if (tryCount > 10)
					{
						// if it can't listen a port after trying 10 times, give up the connection 
						break;
					}
					
				}
			}
		};		
		thread.start();
    
    }



	@Override
	public IBinder onBind(Intent intent) 
	{
		return mBinder;
	}


    private void showNotification(String username, String msg) 
	{       
      //set titel
    	String title = "Textplus You got a new Message From ~>(" + username + ")";
 				//set the text
    	String text = username + ": " + ((msg.length() < 5) ? msg : msg.substring(0, 5)+ "...");
		// Set the icon
    	NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.notification).setContentTitle(title).setContentText(text);

        Intent i = new Intent(this, WritingMessage.class);
        i.putExtra(FriendsInformation.USERNAME, username);
        i.putExtra(MessagesInformation.MESSAGETEXT, msg);	
        // The Pending Intent to launch our  activity if we select this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);
        // Set the infomation  for the views that show in the notification 
        mBuilder.setContentIntent(contentIntent); 
        mBuilder.setContentText("You Have a New Message Amigo From -> " + username + ": " + msg);    
        // Send the notification.
        // We use a layout id because it is a unaique number.  We use it later to cancel.
        mNM.notify((username+msg).hashCode(), mBuilder.build());
    }
	 

	public String getUsername() {
		///returns the logged in username or null if he is not logged in
		return this.username;
	}

	///making all the requested params to send for the server a one string so we can send this params in a http request \
    //this method is called when we try to ssend a message for another user
	public String sendMessage(String  username, String  tousername, String message) throws UnsupportedEncodingException 
	{///encoding the username ,password ,the message ,the reciver username ,and action to send them to the server
		String params = "username="+ URLEncoder.encode(this.username,"UTF-8") +"&password="+ URLEncoder.encode(this.password,"UTF-8") +
						"&to=" + URLEncoder.encode(tousername,"UTF-8") +"&message="+ URLEncoder.encode(message,"UTF-8") +
					"&action="  + URLEncoder.encode("sendMessage","UTF-8")+"&";
					Log.println(Log.ERROR,"not error its debugging  PARAMS ",params);
	
		return socketOperator.sendHttpRequest(params);		
	}

	
	private String getFriendList() throws UnsupportedEncodingException 	{		
		// after authentication, server replie with a friendList xml
		 rawFriendList = socketOperator.sendHttpRequest(getAuthenticateUserParams(username, password));
		 if (rawFriendList != null) 
		 {///parsing the xml replay from the server and updating the friendslist and messagelist by activating the xmlhandler constractor
			this.parseFriendInfo(rawFriendList);
		 }
		 return rawFriendList;
	}
	
	private String getMessageList() throws UnsupportedEncodingException 	{
        // after authentication, server replie with a friendList xml
        rawMessageList = socketOperator.sendHttpRequest(getAuthenticateUserParams(username, password));
		 if (rawMessageList != null) {
             ///parsing the xml replay from the server and updating the friendslist and messagelist by activating the xmlhandler constractor

             this.parseMessageInfo(rawMessageList);
		 }
		 return rawMessageList;
	}
	
	

	public String authenticateUser(String usernameText, String passwordText) throws UnsupportedEncodingException 
	{
		this.username = usernameText;
		this.password = passwordText;
		this.authenticatedUser = false;
		String result = this.getFriendList();
		if (result != null && !result.equals(MainLogin.AUTHENTICATION_FAILED)) 
		{			
			// if user is authenticated then return string from sever is not eqal to AUTHENTICATIO_FAILED
            //and if the user have frineds
			this.authenticatedUser = true;
			rawFriendList = result;
			USERNAME = this.username;
            ///setting the action meaning that the action for this intent is to update friends list
			Intent i = new Intent(FRIEND_LIST_UPDATED);
            //putting the friends list as extras in the intent to update to the extras we send
			i.putExtra(FriendsInformation.FRIEND_LIST, rawFriendList);
           //sending brodcast to the app MessageReceiver class that is in Lisoffriends and in writing message
            //to update there data to the data we send in this intent we send
			sendBroadcast(i);
			timer.schedule(new TimerTask()
			{			
				public void run() 
				{
					try {					
						
						// sending the friend list and doing Brodcast to za hdaka
                        // saving into the intents data and send it to the broadcast reciver that update the data
						Intent i = new Intent(FRIEND_LIST_UPDATED);
						Intent i2 = new Intent(MESSAGE_LIST_UPDATED);
						String tmp = MService.this.getFriendList();
						String tmp2 = MService.this.getMessageList();
						if (tmp != null)
						{
							i.putExtra(FriendsInformation.FRIEND_LIST, tmp);
							sendBroadcast(i);	
							Log.println(Log.ERROR,"not error its debugging ","friend list broadcast sent Amigo   (-_*) ");
						
							if (tmp2 != null)
							{
							i2.putExtra(MessagesInformation.MESSAGE_LIST, tmp2);
							sendBroadcast(i2);	
							Log.println(Log.ERROR,"not error its debugging ","friend list broadcast sent Amigo  (*_*) ");
	
							}
						}
						else {
							Log.println(Log.ERROR,"not error its debugging ","friend list returned null you have no friends  Amigo We Can BE Your Friend ADD me khalil123er and add  sharbelmousa");

						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}					
				}			
			}, UPDATE_TIME, UPDATE_TIME);// every
		}
		
		return result;		
	}

	public void messageReceived(String username, String message) 
	{				
		
	MessagesInformation msg = MessageController.checkMessage(username);
		if ( msg != null)
		{			
			Intent i = new Intent(TAKE_MESSAGE);
			i.putExtra(MessagesInformation.USERID, msg.userid);			
			i.putExtra(MessagesInformation.MESSAGETEXT, msg.messagetext);			
			sendBroadcast(i);
			String activeFriend = FriendController.getActiveFriend();
			//if the user is gets a meesage from a friend that the reciver is not in the sender chat activity it will show a notafcation and save the
			//message in the sqlite
			if (activeFriend == null || activeFriend.equals(username) == false) 
			{
				// saving in the sqlite
				localstoragehandler.insert(username,this.getUsername(), message.toString());
				//showing the notafcation
				showNotification(username, message);
			}
			Log.println(Log.ERROR,"not error its debugging ","TAKE_MESSAGE broadcast sent by im service");
	
		}	
		
	}  
	//getting messages and friends list from the server by making a string that contaies all the username and pass
	private String getAuthenticateUserParams(String usernameText, String passwordText) throws UnsupportedEncodingException 
	{			
		String params = "username=" + URLEncoder.encode(usernameText,"UTF-8") +"&password="+ URLEncoder.encode(passwordText,"UTF-8") +
				"&action="  + URLEncoder.encode("authenticateUser","UTF-8")+"&port="    + 
				
				URLEncoder.encode(Integer.toString(socketOperator.getListeningPort()),"UTF-8") +
						"&";		
		
		return params;		
	}

	// if the device is connected to a network
	public boolean isNetworkConnected() {
		return conManager.getActiveNetworkInfo().isConnected();
	}

	// if the user has loggen in or not
	public boolean isUserAuthenticated(){
		return authenticatedUser;
	}
	/////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onDestroy() {
			Log.println(Log.ERROR,"not error its debugging ","IMService is being destroyed");
		super.onDestroy();
	}
	
	public void exit() 
	{//shutting down the timer tasks and the secket proccses
		timer.cancel();
		socketOperator.exit(); 
		socketOperator = null;
		//exiting the app
		this.stopSelf();
	}

    ///making all the requested params to send for the server a one string so we can send this params in a http request
	public String signUpUser(String usernameText, String passwordText,
			String emailText) 
	{
		String params = "username=" + usernameText +	"&password=" + passwordText +
					"&action=" + "signUpUser"+"&email=" + emailText+"&";
		String result = socketOperator.sendHttpRequest(params);		
		return result;
	}

    ///making all the requested params to send for the server a one string so we can send this params in a http request
	public String addNewFriendRequest(String friendUsername) 
	{
		String params = "username=" + this.username +"&password=" + this.password +"&action=" + "addNewFriend" +
		"&friendUserName=" + friendUsername +"&";
		String result = socketOperator.sendHttpRequest(params);		
		
		return result;
	}

    ///making all the requested params to send for the server a one string so we can send this params in a http request
	public String sendFriendsReqsResponse(String approvedFriendNames,
			String discardedFriendNames) 
	{
		String params = "username=" + this.username +
		"&password=" + this.password +"&action=" + "responseOfFriendReqs"+
		"&approvedFriends=" + approvedFriendNames +"&discardedFriends=" +discardedFriendNames +
		"&";

		String result = socketOperator.sendHttpRequest(params);		
		
		return result;
		
	} 
	
	private void parseFriendInfo(String xml)
	{			
		try 
		{///making a new sax parser and parsing the data we get fro the server becouse we get them as a xml file and decoding it by saxparser
            //then we active the xmlhandler class constarctor that updates the friends list and message and the friends request
			SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
			sp.parse(new ByteArrayInputStream(xml.getBytes()), new XmlHandler(MService.this));
		} 
		catch (ParserConfigurationException e) {			
			Log.println(Log.ERROR,"Erroor in sax parser",e.getMessage());
		}
		catch (SAXException e) {			
					Log.println(Log.ERROR,"Erroor in sax parser",e.getMessage());
	
		} 
		catch (IOException e) {			
					Log.println(Log.ERROR,"Erroor in sax parser",e.getMessage());
	
		}	
	}
	private void parseMessageInfo(String xml)
	{			
		try 
		{///making a new sax parser and parsing the data we get fro the server becouse we get them as a xml file and decoding it by saxparser
            //then we active the xmlhandler class constarctor that updates the friends list and message and the friends request

            SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
			sp.parse(new ByteArrayInputStream(xml.getBytes()), new XmlHandler(MService.this));
		} 
		catch (ParserConfigurationException e) {			
					Log.println(Log.ERROR,"Erroor in sax parser",e.getMessage());
	
		}
		catch (SAXException e) {			
					Log.println(Log.ERROR,"Erroor in sax parser",e.getMessage());
	
		} 
		catch (IOException e) {			
					Log.println(Log.ERROR,"Erroor in sax parser",e.getMessage());
	
		}	
	}

	public void updateData(MessagesInformation[] messages,FriendsInformation[] friends, FriendsInformation[] unApprovedFriends )//String //userKey
	{
		//updating the messages
		MessageController.setMessagesInfo(messages);
			Log.println(Log.ERROR,"not error its debugging SERVICE" ,"messages.length="+messages.length);
		
		int i = 0;
		while (i < messages.length){
			messageReceived(messages[i].userid,messages[i].messagetext);
			i++;
		}
		
		// updating the friends
		FriendController.setFriendsInfo(friends);
		FriendController.setUnapprovedFriendsInfo(unApprovedFriends);
		
	}


	
	
	
	
}
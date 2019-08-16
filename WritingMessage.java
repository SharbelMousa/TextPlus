package com.win7.example.textplus;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.win7.example.textplus.Data_type.FriendsInformation;
import com.win7.example.textplus.Data_type.MessagesInformation;
import com.win7.example.textplus.interfaces.Manager;
import com.win7.example.textplus.service.MService;
import com.win7.example.textplus.toolBox.DataStorage;
import com.win7.example.textplus.toolBox.FriendController;

import java.io.UnsupportedEncodingException;


public class WritingMessage extends Activity {


	public String username;
	private EditText messageText;
	private EditText messageHistoryText;
	private Button sendMessageButton;
	private Manager imService;
	// FriendsInformation friend so we can take the username of the user and the friend list name
	// that in the message box appear beside the message
	private FriendsInformation friend = new FriendsInformation();
	private DataStorage localstoragehandler; // DataStorage we used it so we can save the chat inside the database
	private Cursor dbCursor; // by this cursor we will be able accessing the database and handel data

	/*
	this method is called when we get a service connection so we can get the service opject to access it directly
	we  use ibinder to get a spisific service so we can access  it directly
	the service that is already running
	* */
	private ServiceConnection mConnection = new ServiceConnection() {
		//this method is called when there is a connecteion with the service

		public void onServiceConnected(ComponentName className, IBinder service) {
			//making the imservice a service that is the MService class
            imService = ((MService.IMBinder)service).getService();
        }
        public void onServiceDisconnected(ComponentName className) {
			//this methd is actived when the service is stoped and disconnected
			//we make the imservice a null to cancel the service and make a toast that the app stoped
        	imService = null;
            Toast.makeText(WritingMessage.this, R.string.local_service_stopped,
                    Toast.LENGTH_SHORT).show();
        }
    };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		
		super.onCreate(savedInstanceState);	   
		
		setContentView(R.layout.message);		
		messageHistoryText = (EditText) findViewById(R.id.messageHistory);
		messageText = (EditText) findViewById(R.id.message);
		messageText.requestFocus();

		sendMessageButton = (Button) findViewById(R.id.sendMessageButton);
		Bundle extras = this.getIntent().getExtras();
		//getting the friend we have selected info
		friend.userName = extras.getString(FriendsInformation.USERNAME);
		friend.ip = extras.getString(FriendsInformation.IP);
		friend.port = extras.getString(FriendsInformation.PORT);
		String msg = extras.getString(MessagesInformation.MESSAGETEXT);
		setTitle("Chatting with -> " + friend.userName);
		localstoragehandler = new DataStorage(this);
		//getting the old messges with this friend from the sqlite database
		dbCursor = localstoragehandler.get(friend.userName, MService.USERNAME );
		if (dbCursor.getCount() > 0){
		int noOfScorer = 0;
		dbCursor.moveToFirst();
			//putting all the old messages to the messgae box
		    while ((!dbCursor.isAfterLast())&& noOfScorer<dbCursor.getCount())
		    {
		        noOfScorer++;
				this.appendToMessageHistory(dbCursor.getString(2) , dbCursor.getString(3));
		        dbCursor.moveToNext();
		    }
		}
		localstoragehandler.close();
		
		if (msg != null) 
		{
			//putting the recived messeges in the message box and canceling the notfcation
			this.appendToMessageHistory(friend.userName , msg);
			((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel((friend.userName+msg).hashCode());
		}
		
		sendMessageButton.setOnClickListener(new OnClickListener(){
			// CharSequence represents an ordered set of characters, and defines methods for examining this character set.
			// It is ineterface, and one implementation of this interface is String class.
			CharSequence message;
			// Handler to handle the threads because sending the message and clicking the button at the same time doing 2 processes at the moment
			Handler handler = new Handler();

			public void onClick(View arg0) {
				// saving the message content in the message variable
				message = messageText.getText();

				if (message.length()>0) // if the user typed something
				{		     //getting the username of the user who is sending the message and converted to string
					appendToMessageHistory(imService.getUsername(), message.toString());
					// inserting the username and the friend username and store it inside the database
					localstoragehandler.insert(imService.getUsername(), friend.userName, message.toString());
					// blanking the edit text of the message
					messageText.setText("");
					// so we can handle this activity we hav to use a thread because we are doing 2 processes at the same time
					// 1) sending the message
					// 2) pressing the send button
					Thread thread = new Thread(){					
						public void run() {
							try {  // if the user didn't typed a message in the edit text and it cann't converted to string  we have to handle this thread by prompt the user Toast or dialog
								if (imService.sendMessage(imService.getUsername(), friend.userName, message.toString()) == null)
								{
									
									handler.post(new Runnable(){	

										public void run() {
											
									        Toast.makeText(getApplicationContext(),R.string.message_cannot_be_sent, Toast.LENGTH_LONG).show();								
										}
										
									});
								}
							} catch (UnsupportedEncodingException e) {// if the message is not encoded
								// like if the message is not encoded in utf8 and the
								// comunecation service required other encoding ways
								Toast.makeText(getApplicationContext(),R.string.message_cannot_be_sent, Toast.LENGTH_LONG).show();

							Log.println(Log.ERROR,"error",e.getMessage());
							}
						}						
					};
					thread.start(); // stating the thread
										
				}
				
			}});
		
		messageText.setOnKeyListener(new OnKeyListener(){
			public boolean onKey(View v, int keyCode, KeyEvent event) 
			{
				if (keyCode == 66){// ENTER CODE
					sendMessageButton.performClick();
					return true;
				}
				return false;
			}
			
			
		});
				
	}


	@Override
	protected void onPause() {
		//shutting down the message reciver and the service and butting the friend controller to null
		super.onPause();
		unregisterReceiver(messageReceiver);
		unbindService(mConnection);
		FriendController.setActiveFriend(null);
		
	}

	@Override
	protected void onResume() 
	{		///resart the service
		super.onResume();
		bindService(new Intent(WritingMessage.this, MService.class), mConnection , Context.BIND_AUTO_CREATE);
				//restart the message reciver
		IntentFilter i = new IntentFilter();
		i.addAction(MService.TAKE_MESSAGE);
		
		registerReceiver(messageReceiver, i);
		///re setting the message controller tovthe friend
		FriendController.setActiveFriend(friend.userName);		
		
		
	}
	
	
	public class  MessageReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) 
		{		//get the data from the sened intent from the brodcast
			Bundle extra = intent.getExtras();
			String username = extra.getString(MessagesInformation.USERID);			
			String message = extra.getString(MessagesInformation.MESSAGETEXT);

			if (username != null && message != null)
			{
				if (friend.userName.equals(username)) {
					appendToMessageHistory(username, message);//if we get a message from the friend we ae chatting with (on his chat activity )
					localstoragehandler.insert(username,imService.getUsername(), message);
					
				}
				else {
					if (message.length() > 15) {//if the friend message is more than 15 chars to to but every 15 chars in a line
						message = message.substring(0, 15);
					}
					Toast.makeText(WritingMessage.this,  username + " said'"+message + "'",Toast.LENGTH_SHORT).show();
				}
			}			
		}
		
	};
	private MessageReceiver messageReceiver = new MessageReceiver();
	
	public  void appendToMessageHistory(String username, String message) {
		if (username != null && message != null) {
			//putting the user name and his message in the message box
			messageHistoryText.append(username + ":\n");								
			messageHistoryText.append(message + "\n");
		}
	}
	
	
	@Override
	protected void onDestroy() {
	    super.onDestroy();
		//closing the sqlite  proccess
	    if (localstoragehandler != null) {
	    	localstoragehandler.close();
	    }
	    if (dbCursor != null) {
	    	dbCursor.close();
	    }
	}
	

}

package com.win7.example.textplus;

import android.app.ListActivity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.win7.example.textplus.Data_type.FriendsInformation;
import com.win7.example.textplus.interfaces.Manager;
import com.win7.example.textplus.service.MService;


public class WritingListFriends extends ListActivity {
	
	private static final int APPROVE_SELECTED_FRIENDS_ID = 0;
	private String[] friendUsernames;
	private Manager imService;
	String approvedFriendNames = new String();
	String discardedFriendNames = new String();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		String names = extras.getString(FriendsInformation.FRIEND_LIST);
		//spliting the users names after evrey "," we save everry username in a new array
		friendUsernames = names.split(",");
		//setting the list adapter to be a multi choice list that containes the friends usernames
		setListAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice, friendUsernames));
		//setting the listview choise mode
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		//canceling the new friend request
		NotificationManager NM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		NM.cancel(R.string.new_friend_request_exist);
	}
	
	@Override//adding menu options
	public boolean onCreateOptionsMenu(Menu menu) {		
		boolean result = super.onCreateOptionsMenu(menu);		
		menu.add(0, APPROVE_SELECTED_FRIENDS_ID, 0, R.string.approve_selected_friends);
		return result;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) 
	{		
		switch(item.getItemId()) 
		{
			//on selectiog menu
			case APPROVE_SELECTED_FRIENDS_ID:
			{
				int reqlength = getListAdapter().getCount();
				
				for (int i = 0; i < reqlength ; i++) 
				{
					if (getListView().isItemChecked(i)) {
						//if the user selected friends usernames from the friends requests they will be added to approvedFriendNames
						approvedFriendNames = approvedFriendNames.concat(friendUsernames[i]).concat(",");
					}
					else {
						//if the user didnt select friends usernames from the friends requests they will be added to discardedFriendNames

						discardedFriendNames = discardedFriendNames.concat(friendUsernames[i]).concat(",");						
					}					
				} 
				Thread thread = new Thread(){
					@Override
					public void run() {
						if ( approvedFriendNames.length() > 0 || 
							 discardedFriendNames.length() > 0 
							) 
						{//sending friend request responed to the all friends request
							imService.sendFriendsReqsResponse(approvedFriendNames, discardedFriendNames);
							
						}											
					}
				};
				thread.start();

				Toast.makeText(WritingListFriends.this, "Friend Request Accepted Succfully ^_^ ", Toast.LENGTH_SHORT).show();
			
				finish();				
				return true;
			}			
			
		}

		return super.onMenuItemSelected(featureId, item);		
	}

	@Override
	protected void onPause() 
	{
		//shutting down the service
		unbindService(mConnection);
		super.onPause();
	}

	@Override
	protected void onResume() 
	{
		//reactive the service
		super.onResume();
		bindService(new Intent(WritingListFriends.this, MService.class), mConnection , Context.BIND_AUTO_CREATE);
	}

	/*
	this method is called when we get a service connection so we can get the service opject to access it directly
	we  use ibinder to get a spisific service so we can access  it directly
	the service that is already running
	* */
	private ServiceConnection mConnection = new ServiceConnection() {
		//this method is called when there is a connection with the service
		public void onServiceConnected(ComponentName className, IBinder service) {
			//making the imservice a service that is the MService class
			imService = ((MService.IMBinder)service).getService();
		}
		//this methd is actived when the service is stoped and disconnected
		//we make the imservice a null to cancel the service and make a toast that the app stopped
		public void onServiceDisconnected(ComponentName className) {          
			imService = null;
			Toast.makeText(WritingListFriends.this, R.string.local_service_stopped,
					Toast.LENGTH_SHORT).show();
		}
	};
}

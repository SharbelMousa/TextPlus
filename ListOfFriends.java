package com.win7.example.textplus;

import android.app.ListActivity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.win7.example.textplus.interfaces.Manager;
import com.win7.example.textplus.service.MService;
import com.win7.example.textplus.toolBox.FriendController;
import com.win7.example.textplus.Data_type.FriendsInformation;
import com.win7.example.textplus.Data_type.StatusInformation;


public class ListOfFriends extends ListActivity 
{
	private static final int ADD_NEW_FRIEND_ID = Menu.FIRST;
	private static final int EXIT_APP_ID = Menu.FIRST + 1;
	private Manager  imService = null;
	private FriendListAdapter friendAdapter;
	
	public String ownusername = new String();

	private class FriendListAdapter extends BaseAdapter 
	{		
		class ViewHolder {  // when the user added a friend at the left position will be the icon and on the right position will be the name of the friend

			TextView text;
			ImageView icon;
		}
		/*
      *  LayoutInflater (which coverts an XML layout file into corresponding ViewGroups and Widgets)
       *  and the way it inflates Views inside Fragment’s onCreateView() method.
       *  it used for calling a view from another view
       *  so if a person is online we will inflating one view on another view and we will placing the online icon by bitmap
       *  and if offline we will placing the offline icon by the bitmap */
		private LayoutInflater mInflater;
		private Bitmap mOnlineIcon;
		private Bitmap mOfflineIcon;		

		private FriendsInformation[] friends = null;


		public FriendListAdapter(Context context) {
			super();
			// getting out the LayoutInflater from this list of friends class by the context
			mInflater = LayoutInflater.from(context);
			// adding the online and offline icons
			mOnlineIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.online_icon);
			mOfflineIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.offline_icon);

		}

		public void setFriendList(FriendsInformation[] friends)
		{
			this.friends = friends;
		}


		public int getCount() {
			// returning how many friends we hav in the list
			return friends.length;
		}
		

		public FriendsInformation getItem(int position) {
			// this will tell us the exact position of a specific friend
			return friends[position];
		}

		public long getItemId(int position) {

			return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder;

			// When convertView is not null, we can reuse it directly, there is no need
			// to reinflate it. We only inflate a new View when the convertView supplied
			// by ListView is null
			if (convertView == null) 
			{
				// inflate the list_friends_screen
				convertView = mInflater.inflate(R.layout.friend_list_screen, null);
				// Creates a ViewHolder and store references to the two children views
				// we want to bind data to
				holder = new ViewHolder();
				holder.text = (TextView) convertView.findViewById(R.id.text);
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);
				// placing the textView and the icon in the friends_list_screen xml file
				convertView.setTag(holder);
			}
			// if the list of friends is not empty we hav to get the tag by getTag()
			else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView
				holder = (ViewHolder) convertView.getTag();
			}

			// Bind the data efficiently with the holder.
			holder.text.setText(friends[position].userName);
			// putting the username in the textView in the list_friends_screen
			//also setting the imageView by the online icon or the offline
			holder.icon.setImageBitmap(friends[position].status == StatusInformation.ONLINE ? mOnlineIcon : mOfflineIcon);

			return convertView;
		}

	}

	public class MessageReceiver extends  BroadcastReceiver  {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			Log.println(Log.ERROR,"not error ","Broadcast receiver received a message");
			Bundle extra = intent.getExtras();
			if (extra != null)
			{
				String action = intent.getAction();
				if (action.equals(MService.FRIEND_LIST_UPDATED))
				{//updating friends data
					ListOfFriends.this.updateData(FriendController.getFriendsInfo(), 
												FriendController.getUnapprovedFriendsInfo());
					
				}
			}
		}

	};
	public MessageReceiver messageReceiver = new MessageReceiver();
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
			
			FriendsInformation[] friends = FriendController.getFriendsInfo();
			if (friends != null) {
				//setting the friends
				ListOfFriends.this.updateData(friends, null); 
			}    
			
			setTitle(imService.getUsername() + "'s friend list");
			ownusername = imService.getUsername();
		}
		public void onServiceDisconnected(ComponentName className) {
			//this methd is actived when the service is stoped and disconnected
			//we make the imservice a null to cancel the service and make a toast that the app stoped
			imService = null;
			Toast.makeText(ListOfFriends.this, R.string.local_service_stopped,
					Toast.LENGTH_SHORT).show();
		}
	};
	


	protected void onCreate(Bundle savedInstanceState) 
	{		
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listoffriends);
		friendAdapter = new FriendListAdapter(this);
	}
	public void updateData(FriendsInformation[] friends, FriendsInformation[] unApprovedFriends)
	{//updating friends
		if (friends != null) {
			friendAdapter.setFriendList(friends);
			//setting the list adapter
			setListAdapter(friendAdapter);				
		}				
		
		if (unApprovedFriends != null) 
		{
			//setting the notafcation for friends request
			NotificationManager NM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			
			if (unApprovedFriends.length > 0)
			{					
				String tmp = new String();
				for (int j = 0; j < unApprovedFriends.length; j++) {
					tmp = tmp.concat(unApprovedFriends[j].userName).concat(",");			
				}
				NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.notification)
		    	.setContentTitle(getText(R.string.new_friend_request_exist));
				Intent i = new Intent(this, WritingListFriends.class);
				i.putExtra(FriendsInformation.FRIEND_LIST, tmp);				

				PendingIntent contentIntent = PendingIntent.getActivity(this, 0,i, 0);

				mBuilder.setContentText("You have new friend request(s) Amigooo");
				mBuilder.setContentIntent(contentIntent);

				
				NM.notify(R.string.new_friend_request_exist, mBuilder.build());			
			}
			else
			{
				NM.cancel(R.string.new_friend_request_exist);			
			}
		}

	}


	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		super.onListItemClick(l, v, position, id);

		Intent i = new Intent(this, WritingMessage.class);
		FriendsInformation friend = friendAdapter.getItem(position);
		i.putExtra(FriendsInformation.USERNAME, friend.userName);
		i.putExtra(FriendsInformation.PORT, friend.port);
		i.putExtra(FriendsInformation.IP, friend.ip);

		startActivity(i);
		//oppening a chat activity with the selected friend
	}

	@Override
	protected void onPause() 
	{
		//shuting down the service and the message reciver
		unregisterReceiver(messageReceiver);		
		unbindService(mConnection);
		super.onPause();
	}

	@Override
	protected void onResume() 
	{
			
		super.onResume();
		//reactive the service
		bindService(new Intent(ListOfFriends.this, MService.class), mConnection , Context.BIND_AUTO_CREATE);
		IntentFilter i = new IntentFilter();
		//activating the message reciver and friendlist updater
		i.addAction(MService.FRIEND_LIST_UPDATED);
		registerReceiver(messageReceiver, i);			
		

	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		boolean result = super.onCreateOptionsMenu(menu);
		//adding menu options
		menu.add(0, ADD_NEW_FRIEND_ID, 0, R.string.add_new_friend);
		menu.add(0, EXIT_APP_ID, 0, R.string.exit_application);
		return result;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) 
	{		

		switch(item.getItemId()) 
		{	  
			case ADD_NEW_FRIEND_ID:
			{
				//if the user select add New friend from menu
				Intent i = new Intent(ListOfFriends.this, AddFriend.class);
				startActivity(i);
				return true;
			}		
			case EXIT_APP_ID:
			{
				//if the user select logout from menu
				imService.exit();
				finish();
				return true;
			}			
		}

		return super.onMenuItemSelected(featureId, item);		
	}
	
	@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {

			super.onActivityResult(requestCode, resultCode, data);

		}


}

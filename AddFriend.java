package com.win7.example.textplus;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.win7.example.textplus.service.MService;


public class AddFriend extends Activity implements OnClickListener {

    private static Button mAddFriendButton;
    private static Button mCancelButton;
    private static EditText mFriendUserNameText;
    public static  String res ;
    private static MService mImService = new MService();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_addfriend);
        setTitle(getString(R.string.add_new_friend));

        mAddFriendButton = (Button)findViewById(R.id.addFriend);
        mCancelButton = (Button)findViewById(R.id.cancel);
        mFriendUserNameText = (EditText)findViewById(R.id.newFriendUsername);

        //adding onclicklistner to buttons
        if (mAddFriendButton != null) {
            mAddFriendButton.setOnClickListener(this);
        } else {
            Log.println(Log.ERROR,"error", " AddButton is null");
        }

        if (mCancelButton != null) {
            mCancelButton.setOnClickListener(this);
        } else {
            Log.println(Log.ERROR,"error", " CancelButton is null");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = new Intent(this, MService.class);
        if (mConnection != null) {
            //reactivate the Mservice
            bindService(intent, mConnection , Context.BIND_AUTO_CREATE);
        } else {
        Log.println(Log.ERROR,"error","mConnection is null");
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        //shutting down the Mservice
        if (mConnection != null) {
            unbindService(mConnection);
        } else {
          Log.println(Log.ERROR,"error","onResume: mConnection is null");
        }
    }

    @Override
    public void onClick(View view) {
        if (view == mCancelButton) {
            finish();
        } else if (view == mAddFriendButton) {
            addNewFriend();//activ addnewfriend method
        } else {
      Log.println(Log.ERROR,"error", "onClick: view clicked is unknown");
        }
    }

    /*
    this method is called when we get a service connection so we can get the service opject to access it directly
    we  use ibinder to get a spisific service so we can access  it directly
    the service that is already running
    * */
    private final ServiceConnection mConnection = new ServiceConnection() {
        //this method is called when there is a connecteion with the service
        public void onServiceConnected(ComponentName className, IBinder service) {
            //making the imservice a service that is the MService class
            mImService = (MService) ((MService.IMBinder)service).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            //this methd is actived when the service is stoped and disconnected
            //we make the imservice a null to cancel the service and make a toast that the app stoped
                mImService = null;
            Toast.makeText(AddFriend.this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
        }
    };



// addNewFriend method
    //activated when we press the add button
    private void addNewFriend() {
        if (mFriendUserNameText.length() > 0) {//if the user entered a friend usernam

            Thread thread = new Thread() {
                @Override
                public void run() {
                    // sending friend request to the friend
                    res= mImService.addNewFriendRequest(mFriendUserNameText.getText().toString());
                }
            };
            thread.start();
            Toast.makeText(AddFriend.this,res, Toast.LENGTH_SHORT).show();
            finish();
        } else {
             Log.println(Log.ERROR,"not error ", "addNewFriend: username length (" + mFriendUserNameText.length() + ") is < 0");
            Toast.makeText(AddFriend.this, R.string.type_friend_username, Toast.LENGTH_LONG).show();
        }
    }
}
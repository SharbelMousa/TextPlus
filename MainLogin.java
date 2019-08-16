package  com.win7.example.textplus;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.win7.example.textplus.interfaces.Manager;
import com.win7.example.textplus.service.MService;

import java.io.UnsupportedEncodingException;


public class MainLogin extends Activity {

	public static final String AUTHENTICATION_FAILED = "0";// if the authentication is failed
	private EditText usernameText;
    private EditText passwordText;
    
    private Manager imService;
    public static final int SIGN_UP_ID = Menu.FIRST;
    public static final int EXIT_APP_ID = Menu.FIRST + 1;
   
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
            
            if (imService.isUserAuthenticated() == true)
            {//if the user had allready used the app and singed in to an account and didnt log out
            	Intent i = new Intent(MainLogin.this, ListOfFriends.class);																
				startActivity(i);
				MainLogin.this.finish();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
			//this methd is actived when the service is stoped and disconnected
			//we make the imservice a null to cancel the service and make a toast that the app stoped
        	imService = null;
            Toast.makeText(MainLogin.this, R.string.local_service_stopped,
                    Toast.LENGTH_SHORT).show();
        }
    };
		
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		//strarting the Mservice as service
    	startService(new Intent(MainLogin.this,  MService.class));
        setContentView(R.layout.activity_mainlogin);
        setTitle("Login");
        ImageButton loginButton = (ImageButton) findViewById(R.id.button1);
        usernameText = (EditText) findViewById(R.id.username);
        passwordText = (EditText) findViewById(R.id.password);
        loginButton.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) 
			{					
				if (imService == null) {
					//if the imservice is nul if the service connection is disconnected
					Toast.makeText(getApplicationContext(),R.string.not_connected_to_service, Toast.LENGTH_LONG).show();
					return;
				}
				else if (imService.isNetworkConnected() == false)
					//if the device is not conected to network
				{
					Toast.makeText(getApplicationContext(),R.string.not_connected_to_network, Toast.LENGTH_LONG).show();
					
					
				}

				else if (usernameText.length() > 0 && passwordText.length() > 0)
				{
					// we used a thread because handle more than one process in the same time also by using tha Handler class
					// 1) authenticateUser
					// 2) authentication failed
					Thread loginThread = new Thread(){
						private Handler handler = new Handler(); // Handler class will allows us to handle the 2 threads at the same time
						@Override
						public void run() {
							String result = null;
							try {
								// we activated the authenticateUser method from the manager interface and saving the result in res
								result = imService.authenticateUser(usernameText.getText().toString(), passwordText.getText().toString());
							} catch (UnsupportedEncodingException e) {
								
								Log.println(Log.ERROR,"error",e.getMessage());
							}
							// if the res is null or the username and the password not present at the database
							// we will use the handler so we can mange the thread
							if (result == null || result.equals(AUTHENTICATION_FAILED)) 
							{
								
								handler.post(new Runnable(){
									public void run() {	
										Toast.makeText(getApplicationContext(),R.string.make_sure_username_and_password_correct, Toast.LENGTH_LONG).show();

									}									
								});
								// this else will be activated if the res is not equal to AUTHENTICATION_FAILED
								// and it wiill put the res equal to friend list.
								// wich means will take the user to the main page of the app
							}
							else {
							
								
								handler.post(new Runnable(){
									public void run() {										
										Intent i = new Intent(MainLogin.this, ListOfFriends.class);
										startActivity(i);	
										MainLogin.this.finish();
									}									
								});
								
							}
							
						}
					};
					// starting the thread
					loginThread.start();
					
				}
				else {
					// this else will activated if the user didn't filled the the username and the password
					Toast.makeText(getApplicationContext(),R.string.fill_both_username_and_password, Toast.LENGTH_LONG).show();
					
				}				
			}       	
        });
        
       
        
    }
    
   
	@Override
	protected void onPause() 
	{
		//shutting down the service coonn
		unbindService(mConnection);
		super.onPause();
	}

	@Override
	protected void onResume() 
	{		//reactive thr service connectaion
		bindService(new Intent(MainLogin.this, MService.class), mConnection , Context.BIND_AUTO_CREATE);
	    		
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		boolean result = super.onCreateOptionsMenu(menu);
		// adding the menu options
		 menu.add(0, SIGN_UP_ID, 0, R.string.sign_up);
		 menu.add(0, EXIT_APP_ID, 0, R.string.exit_application);


		return result;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
	    
		switch(item.getItemId()) 
	    {
	    	case SIGN_UP_ID:
				//onclick on the option mnue sign up button
	    		Intent i = new Intent(MainLogin.this, SignUp.class);
	    		startActivity(i);
	    		return true;
			//onclick on the option mnue exit button
	    	case EXIT_APP_ID:
	    	
	    		return true;
	    }
	       
	    return super.onMenuItemSelected(featureId, item);
	}

	

    
    
    
}
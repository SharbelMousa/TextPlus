package com.win7.example.textplus.toolBox;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * this DataStorage it will take care of database that we are using handeling all the information of friends ,
 * messages and if the user if online or offline , how many messages is being passed ,
 * how many friends in the friends list online
 * so the main job for this DataStorage is to take care of all the information inside the database.
 */

public class DataStorage extends SQLiteOpenHelper {

	private static final String TAG = DataStorage.class.getSimpleName(); // returns the name of the class as given
	// returns an empty string if this class is anonymous


	private static final String DATABASE_NAME = "TextPlus.db";
	private static final int DATABASE_VERSION = 1;
	private static final String _ID = "_id";  // index of the table in the  database every database have an id
	private static final String TABLE_NAME_MESSAGES = "textplus_messages";//message table name
	public static final String MESSAGE_RECEIVER = "receiver";// who is the message receiver
	public static final String MESSAGE_SENDER = "sender";// who is the message sender
	private static final String MESSAGE_MESSAGE = "message";//

	/* create a new table inside the sqlite database */
	private static final String TABLE_MESSAGE_CREATE = "CREATE TABLE " + TABLE_NAME_MESSAGES
	+ " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+ MESSAGE_RECEIVER + " VARCHAR(25), "+ MESSAGE_SENDER + " VARCHAR(25), "
	+MESSAGE_MESSAGE + " VARCHAR(255));";
	/* dropping the table : in case the user deleted the friends list or the messages */
// delete from tbl by the user nane
	private static final String TABLE_MESSAGE_DROP = "DROP TABLE IF EXISTS "+ TABLE_NAME_MESSAGES;

	//constructor
	public DataStorage(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override/* executing the database */
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_MESSAGE_CREATE);
		
	}

	@Override/*unimplemented method means that if your database version is old than you should upgrade
        * and if the database is not supportable than you should drop it  */
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrade the  DB from Verion : "+ oldVersion + " to new  Version :" + newVersion + "; all the data will be delted ");
		db.execSQL(TABLE_MESSAGE_DROP);
		onCreate(db);
		
	}
	/* saving the data when the users start to chat with each other :
                    * 1) the sender
                     * 2) the receiver
                      * 3) the messages */
	public void insert(String sender, String receiver, String message){
		long rowId = -1;// this will speciphied on witch row we will insert the sender , receiver , message area section
		try{// dealing with database input and output
			
			SQLiteDatabase db = getWritableDatabase();// this wil allows us to open the database and insert into...
			ContentValues values = new ContentValues(); // allows us to put somethings into the database like manipulating the elements o the database .

			values.put(MESSAGE_RECEIVER, receiver);// inserting
			values.put(MESSAGE_SENDER, sender);// inserting
			values.put(MESSAGE_MESSAGE, message);// inserting
			rowId = db.insert(TABLE_NAME_MESSAGES, null, values);  // inserting all the elements into the database
			
		} catch (SQLiteException e){
			Log.e(TAG, "insert()", e);
		} finally {
			Log.d(TAG, "insert(): rowId=" + rowId);
		}
		
	}
	/* so we can pull all the strings from the database like
       * the sender , the receiver ,the message to matched with the queries outside of the area
        * by running a selective query so we can select from the table so we have to use the concept of cursor
          * the cursor allows us to select the queries from the database  */
	public Cursor get(String sender, String receiver) {
					
			SQLiteDatabase db = getWritableDatabase();// in order to pull something from the database or inserting we have to get a primession
			String SELECT_QUERY = "SELECT * FROM " + TABLE_NAME_MESSAGES + " WHERE " + MESSAGE_SENDER + " LIKE '" + sender + "' AND " + MESSAGE_RECEIVER + " LIKE '" + receiver + "' OR " + MESSAGE_SENDER + " LIKE '" + receiver + "' AND " + MESSAGE_RECEIVER + " LIKE '" + sender + "' ORDER BY " + _ID + " ASC";
			
			return db.rawQuery(SELECT_QUERY,null);
		
	}
	
	

}

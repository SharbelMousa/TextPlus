package com.win7.example.textplus.toolBox;

import android.util.Log;

import com.win7.example.textplus.Data_type.FriendsInformation;
import com.win7.example.textplus.Data_type.MessagesInformation;
import com.win7.example.textplus.Data_type.StatusInformation;
import com.win7.example.textplus.interfaces.Updater;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Vector;

/*
 *  XmlHandler provides way how to access or modify and change data presents in an XML document
 *  it will parse the xml data into the FriendsInformation array.
 */
public class XmlHandler extends DefaultHandler
{
		private Updater updater;
		
		public XmlHandler(Updater updater) {
			super();
			this.updater = updater;
		}

	/* we have used the vector class because by the vector we can add
                   * and remove the data that's means that we can make the vetor bigger or smaller
                   * as much as we can*/
		private Vector<FriendsInformation> mFriends = new Vector<FriendsInformation>();
		private Vector<FriendsInformation> mOnlineFriends = new Vector<FriendsInformation>();
		private Vector<FriendsInformation> mUnapprovedFriends = new Vector<FriendsInformation>();
		private Vector<MessagesInformation> mUnreadMessages = new Vector<MessagesInformation>();

	/*endDocument holds an a friend array that we will use it for store inside of it Friends Informations like online offline
    *and other and we used the The SAXException class, is the generic exception class for almost anything other
     *  than an I/O problem that can go wrong while processing an XML document with SAX. Not only the parse() method
      *  but most of the callback methods in the various SAX interfaces are declared to throw this exception.
       *  If we detect a problem while processing an XML document, the code can throw its own SAXException.*/
		public void endDocument() throws SAXException 
		{
			FriendsInformation[] friends = new FriendsInformation[mFriends.size() + mOnlineFriends.size()];// (array.size) containes count of all online friends and friends who are not online
			MessagesInformation[] messages = new MessagesInformation[mUnreadMessages.size()];// (array.size) containes the count of the unread messages


                    /* onlineFriendsCount holds how much online friends we have
                     * and by the loop we have putted every online friend in the array of friends[i] */
			int onlineFriendCount = mOnlineFriends.size();			
			for (int i = 0; i < onlineFriendCount; i++) 
			{				
				friends[i] = mOnlineFriends.get(i);
			}
			  /* offlineFriendsCount holds how much offline  friends we have
                     * and by the loop we have added every offline friend in the array of friends[i] */


			int offlineFriendCount = mFriends.size();			
			for (int i = 0; i < offlineFriendCount; i++) 
			{
				friends[i + onlineFriendCount] = mFriends.get(i);
			}
			/* unApprovedFriendsCount holds how much unApproved  friends we have
                     * and by the loop we have added every unApproved friend in the array of unApprovedFriends[i] */

			int unApprovedFriendCount = mUnapprovedFriends.size();
			FriendsInformation[] unApprovedFriends = new FriendsInformation[unApprovedFriendCount];
			
			for (int i = 0; i < unApprovedFriends.length; i++) {
				unApprovedFriends[i] = mUnapprovedFriends.get(i);
			}

			   /* unreadMessageCount holds how much unread Messages we have
                     * and by the loop we have added every unread Message in the array of messages[i]
                      * and we have used the Log because if any error happen  we will kown*/
			int unreadMessagecount = mUnreadMessages.size();
			Log.println(Log.ERROR,"unreadMessagecount","mUnreadMessages="+unreadMessagecount);
			for (int i = 0; i < unreadMessagecount; i++) 
			{
				messages[i] = mUnreadMessages.get(i);
				Log.println(Log.ERROR,"message log","i="+i);
			}

        /*through the updater we have provided with the Message info and the friends status and the userKey
        * that will keep the messages updated and the friends status if it (online or offline) by matched with the userKye
         *and it also will helps us handeling the data of the online and the offline messages .... */
			this.updater.updateData(messages, friends, unApprovedFriends);//,userKey
			super.endDocument();
		}
	/* the startElement method it's used for matching a strings that we have,
		* and inside this method we will add to the friend array the statuses */

	public void startElement(String uri, String localName, String name,Attributes attributes) throws SAXException
		{				
			if (localName == "friend")// localName its an any name for any user and
			// if it's inside the friends array it will do whats inside th if statemenet
			{
				FriendsInformation friend = new FriendsInformation();
				friend.userName = attributes.getValue(FriendsInformation.USERNAME);// accessing the friends array userName and puting inside of him
				// the UserName that we have created in the FriendsInformation class by attributes.getValue() method
				String status = attributes.getValue(FriendsInformation.STATUS);// putting iside the status the Status the we have been created in the FriendsInformation class
				friend.ip = attributes.getValue(FriendsInformation.IP);// the ip of the user...
				friend.port = attributes.getValue(FriendsInformation.PORT);// the port number that the user is connected (online)

				// if the status of the user is online do the if
				if (status != null && status.equals("online"))
				{					
					friend.status = StatusInformation.ONLINE; // putting inside of the status of friends array " ONLINE " the ONLINE that we declared in the StatusInformation enum
					mOnlineFriends.add(friend);// adding the frined aray inside the mOnlineFriends
				}
				else if (status.equals("unApproved"))// this means that if tje user is still waiting on a friend rquest it will counted as unApproved
				{
					friend.status = StatusInformation.UNAPPROVED;// saving the status as unAproved in the friend array
					mUnapprovedFriends.add(friend);//adding to the unApproved
				}	
				else
				{
					friend.status = StatusInformation.OFFLINE;// this means if the user is online then it will be added to the friend array as Offline
					mFriends.add(friend);	// addig to the offline
				}											
			}
 /* We Are Here Now To Be Continued Hello Moran !! */
			else if (localName == "message") {
				MessagesInformation message = new MessagesInformation();
				message.userid = attributes.getValue(MessagesInformation.USERID);
				message.sendt = attributes.getValue(MessagesInformation.SENDT);
				message.messagetext = attributes.getValue(MessagesInformation.MESSAGETEXT);
				Log.println(Log.ERROR,"MessageLOG", message.userid + message.sendt + message.messagetext);
				mUnreadMessages.add(message);//adding unreadmessage
			}
			super.startElement(uri, localName, name, attributes);
		}

		@Override  /*this start document will clear whta's inside all offline friends and the online friends and the unread messages */
		public void startDocument() throws SAXException {			
			this.mFriends.clear();
			this.mOnlineFriends.clear();
			this.mUnreadMessages.clear();
			super.startDocument();
		}
		
		
}


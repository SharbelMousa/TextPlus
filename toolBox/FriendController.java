package com.win7.example.textplus.toolBox;

import com.win7.example.textplus.Data_type.FriendsInformation;

/**
 * this class will allow us to store the user , the userKey and the friends list information ,
 * it will check the userKey according to the stored combination.
 */
public class FriendController 
{
	
	private static FriendsInformation[] friendsInfo = null;// this represent the friends that were added , null because we haven't any data for 'friendsInfo'
	private static FriendsInformation[] unapprovedFriendsInfo = null;// this represent the friends that unapproved by other users , null because we haven't any data for 'unapprovedFriends'
	private static String activeFriend;// this will represent the friends currently online .


	/* setter and getter to provide and get the information of friends ,
                     and working through the loops that will check every single friend ,
                      and to get the right information . */
	public static void setFriendsInfo(FriendsInformation[] friendInfo)
	{
		FriendController.friendsInfo = friendInfo;
	}

	
	public static void setActiveFriend(String friendName){
		activeFriend = friendName;
	}
	
	public static String getActiveFriend()
	{
		return activeFriend;
	}


	public static void setUnapprovedFriendsInfo(FriendsInformation[] unapprovedFriends) {
		unapprovedFriendsInfo = unapprovedFriends;		
	}



	public static FriendsInformation[] getFriendsInfo() {
		return friendsInfo;
	}



	public static FriendsInformation[] getUnapprovedFriendsInfo() {
		return unapprovedFriendsInfo;
	}
	
	
	

}

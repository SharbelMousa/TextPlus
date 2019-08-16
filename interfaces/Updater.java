package com.win7.example.textplus.interfaces;
import com.win7.example.textplus.Data_type.FriendsInformation;
import com.win7.example.textplus.Data_type.MessagesInformation;

//an interface the holdes the update method
public interface Updater {
	//updating the user mesages frinedslist and the user key )
	public void updateData(MessagesInformation[] messages, FriendsInformation[] friends, FriendsInformation[] unApprovedFriends);//,/ String userKey
///************ps we may never user the user key its just a thing we make to add a secure userkey that is always changed *************
}

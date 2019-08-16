package com.win7.example.textplus.toolBox;

import com.win7.example.textplus.Data_type.MessagesInformation;


/*
 * this class (MessageController) will take care of the messages that is passed between the users
 * and it will store friend information and check userkey and username combination according to its stored data.
 */
public class MessageController 
{
	
	private static MessagesInformation[] messagesInfo = null;// null because at the start it has nothing to do ...
	
	public static void setMessagesInfo(MessagesInformation[] messageInfo)
	{
		MessageController.messagesInfo = messageInfo;
	}


	// checking the username of the message
	public static MessagesInformation checkMessage(String username)
	{
		MessagesInformation result = null;
		if (messagesInfo != null) 
		{
			for (int i = 0; i < messagesInfo.length;) 
			{
				
					result = messagesInfo[i];
					break;
								
			}			
		}		
		return result;
	}
	

}

package client;

import java.io.Serializable;
import java.util.Vector;

public class UserIfo implements Serializable
{
	private String name;
	private String password;
	private String signature =" ";
	private Vector<String> waitSolvedMsg=new Vector<String>();
	private Vector<String> friends = new Vector<String>();

	public UserIfo()
	{
		
	}
	public UserIfo(String name,String password)
	{
		this.name = name;
		this.password=password;
	}
	public void addFriend(String name)
	{
		friends.addElement(name);
	}
	public Vector<String> getAllFriends()
	{
		return friends;
	}
	public synchronized void addWaitSolvedMsg(String msg)
	{
		waitSolvedMsg.addElement(msg);
	}
	public Vector<String> get_clearAllWaitSolvedMsg()
	{
		Vector<String> wsm = new Vector<String>(waitSolvedMsg);
		waitSolvedMsg.clear();
		return wsm;
	}
	public void clearWaitSolvedMsg()
	{
		waitSolvedMsg.clear();
	}
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public String getPassword()
	{
		return password;
	}
	public void setPassword(String password)
	{
		this.password = password;
	}
	public String getSignature()
	{
		return signature;
	}
	public void setSignature(String signature)
	{
		this.signature = signature;
	}
	
}

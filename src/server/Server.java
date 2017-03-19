package server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Vector;

import client.UserIfo;

public class Server
{
	private static ServerWin winFrame;

	private static ServerSocket ss;
	private static Socket clientS;

	private static HashMap<String, UserIfo> allUsers = new HashMap<String, UserIfo>();

	private static Vector<String> onlineUsers = new Vector<String>();
	private static HashMap<String, ServerThread> onlineUsersMap = new HashMap<String, ServerThread>();

	private static File ifoFile = new File("src/ifo.date");

	private Server() // 不许别处实例化服务器
	{
		winFrame = new ServerWin();
		try
		{
			loadIfo();
			ss = new ServerSocket(40000);
		} catch (IOException e)
		{
			e.printStackTrace();
		} catch (ClassNotFoundException e)
		{

			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private void loadIfo() throws FileNotFoundException, IOException, ClassNotFoundException
	{
		if (ifoFile.length() != 0)
		{
			FileInputStream fis = new FileInputStream(ifoFile);
			ObjectInputStream ois = new ObjectInputStream(fis);
			allUsers = (HashMap<String, UserIfo>) ois.readObject();
			fis.close();
			ois.close();
		}
	}

	private static void saveIfo() throws FileNotFoundException, IOException
	{
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ifoFile));
		oos.writeObject(allUsers);
	}

	public static int isLoginCheck(String name, String password)
	{
		if (allUsers.containsKey(name))
		{
			if (allUsers.get(name).getPassword().equals(password))
			{
				if (onlineUsers.contains(name))
				{
					return -1;
				}
				return 1;
			}
		}
		return 0;
	}

	
	
	public static boolean isContain(String target)
	{
		if(allUsers.containsKey(target))
			return true;
		return false;
	}
	
	public static boolean isOnline(String name)
	{
		if (onlineUsersMap.containsKey(name))
			return true;
		return false;
	}

	public static Vector<String> getFriends(String name)
	{
		return allUsers.get(name).getAllFriends();
	}

	public static Vector<String> getOnlinUsers()
	{
		return onlineUsers;
	}

	public static void addWaitSolvedMsg(String target, String msg)
	{
		allUsers.get(target).addWaitSolvedMsg(msg);
	}
	public static Vector<String> get_clearWaitSolvedMsg(String target)
	{
		return allUsers.get(target).get_clearAllWaitSolvedMsg();
	}
	
	public static void addFriend(String target, String name)
	{
		allUsers.get(target).addFriend(name);
	}

	// 返回用户名为name的服务器端线程
	public static ServerThread getUserThread(String name)
	{
		synchronized (onlineUsersMap)
		{
			return onlineUsersMap.get(name);
		}
	}

	// 服务器关闭 通知在线用户服务器已关闭
	public static void closeServer() throws IOException
	{
		saveIfo();

		Iterator<Entry<String, ServerThread>> iter = onlineUsersMap.entrySet().iterator();
		while (iter.hasNext())
		{
			HashMap.Entry<String, ServerThread> entry = (Entry<String, ServerThread>) iter.next();
			ServerThread t = onlineUsersMap.get(entry.getKey());
			if (t != null)
			{
				t.sendMsg("ServerClosed");
			} else
			{
				System.out.println("Server closedServer()  null");
			}
		}
	}

	public static int getUserCount()
	{
		return onlineUsers.size();
	}

	// 用户登录
	public static void addUser(String name, String password)
	{
		UserIfo ifo = new UserIfo(name, password);
		synchronized (allUsers)
		{
			allUsers.put(name, ifo);
		}
	}

	public static void addOnlineUser(String name, ServerThread thread)
	{
		winFrame.updateUserArea(name, true);
		synchronized (onlineUsers)
		{
			onlineUsers.addElement(name);
		}
		synchronized (onlineUsersMap)
		{
			onlineUsersMap.put(name, thread);
		}
	}

	// 用户登出
	public static void reduceUser(String name, ServerThread thread)
	{
		winFrame.updateUserArea(name, false);
		synchronized (onlineUsers)
		{
			onlineUsers.remove(name);
		}
		synchronized (onlineUsersMap)
		{
			onlineUsersMap.remove(name);
		}
	}

	public static void updateMsgArea(String msg)
	{
		winFrame.addMsg(msg);
	}

	public static void main(String[] args)
	{
		new Server();

		while (true)
		{
			try
			{
				clientS = ss.accept();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			ServerThread newUserThread = new ServerThread(clientS);
			newUserThread.start();
		}
	}

	

}

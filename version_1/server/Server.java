package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Vector;

public class Server
{
	private static ServerWin winFrame;

	private static ServerSocket ss;
	private static Socket clientS;

	private static Vector<String> onlinUsers = new Vector<String>();
	private static HashMap<String, ServerThread> usersMap = new HashMap<String, ServerThread>();

	private static Object onlinUsersLock = new Object();// ͬ������
	private static Object usersMapLock = new Object();

	private Server() // �����ʵ����������
	{
		winFrame = new ServerWin();
		try
		{
			ss = new ServerSocket(40000);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static Vector<String> getOnlinUsers()
	{
		return onlinUsers;
	}

	//�����û���Ϊname�ķ��������߳�
	public static ServerThread getUserThread(String name)
	{
		synchronized (usersMapLock)
		{
			return usersMap.get(name);
		}
	}

	// �������ر� ֪ͨ�����û��������ѹر�
	public static void closeServer() throws IOException
	{
		Iterator<Entry<String, ServerThread>> iter = usersMap.entrySet().iterator();
		while (iter.hasNext())
		{
			HashMap.Entry<String, ServerThread> entry = (Entry<String, ServerThread>) iter.next();
			ServerThread t = usersMap.get(entry.getKey());
			t.sendMsg("ServerClosed");
		}
	}

	public static int getUserCount()
	{
		return onlinUsers.size();
	}

	//�û���¼
	public static void addUser(String name, ServerThread thread)
	{
		winFrame.updateUserArea(name, true);
		synchronized (onlinUsersLock)
		{
			onlinUsers.addElement(name);
		}
		synchronized (usersMapLock)
		{
			usersMap.put(name, thread);
		}
	}

	//�û��ǳ�
	public static void reduceUser(String name, ServerThread thread)
	{
		winFrame.updateUserArea(name, false);
		synchronized (onlinUsersLock)
		{
			onlinUsers.remove(name);
		}
		synchronized (usersMapLock)
		{
			usersMap.remove(name);
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

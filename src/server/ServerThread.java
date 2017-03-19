package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.annotation.Target;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Vector;

/*
 * 在创建ObjectInputStream对象时会检查ObjectOutputStream所传过来了头信息，如果没有信息将一直会阻塞
 */

public class ServerThread extends Thread
{

	private Socket clientS;
	private InputStream is;
	private OutputStream os;
	private ObjectOutputStream oos;
	private Object osLock = new Object();

	private int count = 0;
	private String name = " name";
	private boolean is_logout = false;

	Vector<String> friendsname;

	public ServerThread(Socket s)
	{
		setName("ServerThread Thread : " + name);
		this.clientS = s;
	}

	@Override
	public void run()
	{
		try
		{
			connet();
			while (!is_logout)
			{
				String msg = readString();
				String[] splitMsg = msg.split(" ");

				if (splitMsg[0].equals("register"))
				{
					if (!Server.isContain(splitMsg[1]))
					{
						sendMsg("registerPass");
						addUser(splitMsg[1], splitMsg[2]);
						addOnlineUser(splitMsg[1]);//
						// sendOnlineUsers(); // 再输出已有用户的用户名
						sendFriends();
					} else
					{
						sendMsg("registerNoPass");
					}
				} else if (splitMsg[0].equals("login"))
				{
					int checkresult = Server.isLoginCheck(splitMsg[1], splitMsg[2]);
					if (checkresult == 1)
					{
						sendMsg("pass");
						addOnlineUser(splitMsg[1]);
						// sendOnlineUsers();
						sendFriends();
						sendWaitSolvedMsg();
						notifyFriends("friendLogin");

					} else if (checkresult == -1)
					{
						sendMsg("onlined");
					} else if (checkresult == 0)
					{
						sendMsg("nopass");
					}
				} else if (splitMsg[0].equals("closed"))
				{
					notifyFriends("friendLogout");
					logout();
					// break;
				} else if (splitMsg[0].equals("chatMsg"))
				{
					transmitMsg(msg);
				} else if (splitMsg[0].equals("apply"))
				{
					transmitApply(msg);
				} else if (splitMsg[0].equals("addFriend"))
				{
					Server.addFriend(name, splitMsg[1]);
				} else if (splitMsg[0].equals("responseApply"))
				{
					responseApply(msg);
				}
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		} catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	private void sendWaitSolvedMsg() throws IOException
	{
		Vector<String> msgs = Server.get_clearWaitSolvedMsg(name);
		for (int i = 0; i < msgs.size(); i++)
		{
			sendMsg(msgs.get(i));
		}
	}

	private void sendFriends() throws IOException
	{
		oos = new ObjectOutputStream(os);
		Map<String, String> friends = new HashMap<String, String>();
		friendsname = Server.getFriends(name);
		for (int i = 0; i < friendsname.size(); i++)
		{
			String name = friendsname.get(i);
			if (Server.isOnline(name))
			{
				friends.put(name, "true");
			} else
			{
				friends.put(name, "false");
			}
		}
		oos.writeObject(friends);
	}

	private void notifyFriends(String state) throws IOException
	{ // state = friendLogin 或 friendLogout
		for (int i = 0; i < friendsname.size(); i++)
		{
			String name = friendsname.elementAt(i);
			if (Server.isOnline(name))
			{
				ServerThread st = Server.getUserThread(name);
				st.sendMsg(state + " " + this.name);
			}
		}
	}

	private void responseApply(String msg) throws IOException
	{
		String[] splitMsg = msg.split(" ");
		String target = splitMsg[1];
		String result = splitMsg[2];

		ServerThread st = Server.getUserThread(target);
		String Msg = "addResponse" + " " + name + " " + result;
		if (st != null)
		{
			st.sendMsg(Msg);
		} else
		{
			Server.addWaitSolvedMsg(target, Msg);
		}
	}

	private void connet() throws IOException
	{
		is = clientS.getInputStream();
		os = clientS.getOutputStream();
		// clientS.setSoTimeout(arg0);
	}

	private void transmitApply(String msg) throws IOException
	{
		String[] splitMsg = msg.split(" ");
		String type = splitMsg[1];
		String target = splitMsg[2];
		if (type.equals("friend"))
		{
			if (Server.isContain(target))
			{
				ServerThread st = Server.getUserThread(target);
				String applyMsg = "addApply" + " " + name;
				if (st != null)
				{
					st.sendMsg(applyMsg);
				} else
				{
					Server.addWaitSolvedMsg(target, applyMsg);
				}
			} else
			{
				sendMsg("noFind");
			}
		} else if (type.equals("group"))
		{

		}
	}

	private void addUser(String name, String password) throws ClassNotFoundException, IOException
	{
		Server.addUser(name, password);
	}

	private void addOnlineUser(String name) throws ClassNotFoundException, IOException
	{
		this.name = name;
		Server.addOnlineUser(name, this);
		count = Server.getUserCount();
	}

	private void logout() throws IOException
	{
		Server.reduceUser(name, this);
		is_logout = true;
	}

	private void sendOnlineUsers() throws IOException
	{
		oos = new ObjectOutputStream(os);
		oos.writeObject(Server.getOnlinUsers());
	}

	public void sendMsg(String msg) throws IOException
	{
		synchronized (osLock)
		{
			os.write(msg.getBytes());
		}
	}

	private String readString() throws IOException
	{
		int length = 2048;
		byte[] bs = new byte[length];
		int len = is.read(bs);
		if (len == -1)
		{
	//		System.out.println("长度为 -1");

			try
			{
				Thread.sleep(100);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			return "noMag";
		} else
		{
			String s = new String(bs, 0, len);
			return s;
		}

	}

	// 转发聊天消息给目标用户
	private void transmitMsg(String msg) throws IOException
	{
		String[] splitMsg = msg.split(" ");
		String contextMsg = new String();

		ServerThread to = Server.getUserThread(splitMsg[1]);
		int headLenght = splitMsg[0].length(); // 消息类型 此时为chatMsg
		int nameLenght = splitMsg[1].length(); // 消息目标对象名

		StringBuffer msgBuffer = new StringBuffer(msg);
		contextMsg = msgBuffer.substring(headLenght + nameLenght + 2); // 获取消息内容

		String newMsg = new String();
		newMsg = "chatMsg" + " " + this.name + " " + contextMsg;
		if (to != null)
		{
			to.sendMsg(newMsg); // 调用目标线程发送信息
		} else
		{
			Server.addWaitSolvedMsg(splitMsg[1], newMsg);
		}
		String msg2 = "from" + " " + this.name + " " + "to" + " " + splitMsg[1] + ": " + contextMsg;
		Server.updateMsgArea(msg2);
	}
}

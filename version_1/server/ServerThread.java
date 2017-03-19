package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

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
	private UpdateUsers updateUsersThread = new UpdateUsers();

	public ServerThread(Socket s)
	{
		this.clientS = s;
	}

	@Override
	public void run()
	{
		try
		{
			connet();
			addUser();// 首先读取新用户的name 添加到。。
			sendOnlineUsers(); // 再输出已有用户的用户名

			updateUsersThread.start();
			while (!is_logout)
			{
				String msg = readString();
				String[] splitMsg = msg.split(" ");
				if (splitMsg[0].equals("closed"))
				{
					logout();
					break;
				} else if (splitMsg[0].equals("chatMsg"))
				{
					transmitMsg(msg);
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

	private void connet() throws IOException
	{
		is = clientS.getInputStream();
		os = clientS.getOutputStream();
	}

	private void addUser() throws ClassNotFoundException, IOException
	{
		name = readString();
		Server.addUser(name, this);
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
		byte[] bs = new byte[1024];
		int len = is.read(bs);
		String s = new String(bs, 0, len);
		return s;
	}

	//转发聊天消息给目标用户
	private void transmitMsg(String msg) throws IOException
	{
		String[] splitMsg = msg.split(" ");
		String contextMsg = new String();

		ServerThread to = Server.getUserThread(splitMsg[1]);
		if (to != null)
		{
			int headLenght = splitMsg[0].length();  //消息类型  此时为chatMsg
			int nameLenght = splitMsg[1].length();  //消息目标对象名

			StringBuffer msgBuffer = new StringBuffer(msg);
			contextMsg = msgBuffer.substring(headLenght + nameLenght + 2); //获取消息内容

			String newMsg = new String();
			newMsg = "chatMsg" + " " + this.name + " " + contextMsg;
			if (splitMsg[1].equals(this.name))  //是发送给自己的信息
			{
				String msg1 = "from" + " " + this.name + " " + "to" + " " + this.name + ": " + contextMsg;
				Server.updateMsgArea(msg1);
			} else
			{
				to.sendMsg(newMsg);   // 不是发送给自己的信息 则调用目标线程发送信息
				String msg2 = "from" + " " + this.name + " " + "to" + " " + splitMsg[1] + ": " + contextMsg;
				Server.updateMsgArea(msg2);
			}
		}
	}

	class UpdateUsers extends Thread
	{
		@Override
		public void run()
		{
			try
			{
				while (!is_logout)
				{
					Thread.sleep(100);  //每100毫秒更新一次在线用户列表
					int onlinUserCount = Server.getUserCount();

					if (count != onlinUserCount) //在线用户发生变化
					{
						count = onlinUserCount;
						sendMsg("updateOnlineUser");
						sendOnlineUsers();
					}
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}

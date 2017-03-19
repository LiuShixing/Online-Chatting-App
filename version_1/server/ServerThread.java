package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

/*
 * �ڴ���ObjectInputStream����ʱ����ObjectOutputStream����������ͷ��Ϣ�����û����Ϣ��һֱ������
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
			addUser();// ���ȶ�ȡ���û���name ��ӵ�����
			sendOnlineUsers(); // ����������û����û���

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

	//ת��������Ϣ��Ŀ���û�
	private void transmitMsg(String msg) throws IOException
	{
		String[] splitMsg = msg.split(" ");
		String contextMsg = new String();

		ServerThread to = Server.getUserThread(splitMsg[1]);
		if (to != null)
		{
			int headLenght = splitMsg[0].length();  //��Ϣ����  ��ʱΪchatMsg
			int nameLenght = splitMsg[1].length();  //��ϢĿ�������

			StringBuffer msgBuffer = new StringBuffer(msg);
			contextMsg = msgBuffer.substring(headLenght + nameLenght + 2); //��ȡ��Ϣ����

			String newMsg = new String();
			newMsg = "chatMsg" + " " + this.name + " " + contextMsg;
			if (splitMsg[1].equals(this.name))  //�Ƿ��͸��Լ�����Ϣ
			{
				String msg1 = "from" + " " + this.name + " " + "to" + " " + this.name + ": " + contextMsg;
				Server.updateMsgArea(msg1);
			} else
			{
				to.sendMsg(newMsg);   // ���Ƿ��͸��Լ�����Ϣ �����Ŀ���̷߳�����Ϣ
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
					Thread.sleep(100);  //ÿ100�������һ�������û��б�
					int onlinUserCount = Server.getUserCount();

					if (count != onlinUserCount) //�����û������仯
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

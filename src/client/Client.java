package client;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import server.ServerThread;

/*
 * 在创建ObjectInputStream对象时会检查ObjectOutputStream所传过来了头信息，如果没有信息将一直会阻塞
 */
public class Client extends Thread
{
	public ClientWin clientWin;

	private Socket socket;
	private InputStream is;
	private OutputStream os;
	private ObjectInputStream ois;

	public HashMap<String, ChatWin> chattingUsers = new HashMap<String, ChatWin>();

	public String name = "name";
	public boolean is_logout = false;

	public Map<String, String> friends = new HashMap<String, String>();

	public Client() throws UnknownHostException, IOException
	{
		setName("Client Thread : " + name);
		new LogonWin(this);
	}

	@Override
	public void run()
	{
		while (!is_logout)
		{
			try
			{
				String msg = readString();
				String[] splitMsg = msg.split(" ");

				if (splitMsg[0].equals("chatMsg")) // 是聊天信息
				{
					clientWin.analyzeMsg(msg);
				} else if (splitMsg[0].equals("ServerClosed")) // 服务器关闭
				{
					JOptionPane.showMessageDialog(null, "服务器已关闭！", "警告", JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				} else if (splitMsg[0].equals("addResponse"))
				{
					String name = splitMsg[1];
					String result = splitMsg[2];
					String message = name + result + "你的好友请求";
					JOptionPane.showMessageDialog(null, message, "好友申请结果", JOptionPane.YES_OPTION);

					if (result.equals("agree"))
					{
						addFriend(name);
					}
				} else if (splitMsg[0].equals("noFind"))
				{
					JOptionPane.showMessageDialog(null, "没有该用户", "好友申请结果", JOptionPane.YES_OPTION);
				} else if (splitMsg[0].equals("addApply"))
				{
					String message = splitMsg[1] + "请求添加你为好友。同意？";
					int r = JOptionPane.showConfirmDialog(clientWin, message, "好友请求", JOptionPane.YES_NO_OPTION);
					if (r == JOptionPane.YES_OPTION)
					{
						addFriend(splitMsg[1]);
						responseApply(splitMsg[1], "agree");
					} else
					{
						responseApply(splitMsg[1], "reject");
					}
				} else if (splitMsg[0].equals("friendLogin"))
				{
					clientWin.updateFriendsList("friendLogin", splitMsg[1]);
				} else if (splitMsg[0].equals("friendLogout"))
				{
					clientWin.updateFriendsList("friendLogout", splitMsg[1]);
				}else if(splitMsg[0].equals("friendOff_line"))
				{
					
				}
				

			} catch (IOException e)
			{
				System.out.println(name + "error");
				e.printStackTrace();
			}

		}
	}

	private void responseApply(String name, String result) throws IOException
	{

		String response = "responseApply" + " " + name + " " + result;
		sendMsg(response);
	}

	private void addFriend(String name) throws IOException
	{
		String msg = "addFriend" + " " + name;
		sendMsg(msg);

		friends.put(name, "true"); // ???????????????????????????????

		if (friends.get(name).equals("true"))
		{
			clientWin.addOnlineFriendsList(name);
		} else
		{
			clientWin.addOff_lineFriendsList(name);
		}
	}

	// 连接服务器
	public void connet() throws IOException
	{
		socket = new Socket(InetAddress.getLocalHost(), 40000);
		is = socket.getInputStream();
		os = socket.getOutputStream();
	}

	public void shutdown()
	{
		try
		{
			socket.shutdownOutput();
			socket.shutdownInput();
		} catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	public void readFriends() throws ClassNotFoundException, IOException
	{
		ois = new ObjectInputStream(is); // 每次读取都新创建一个流对象
		friends = (HashMap<String, String>) ois.readObject();

		Iterator<Entry<String, String>> it = friends.entrySet().iterator();

		while (it.hasNext())
		{
			Entry<String, String> k_v = it.next();
			String k = k_v.getKey();
			String v = k_v.getValue();

			if (v.equals("true"))
			{
				clientWin.addOnlineFriendsList(k);
			} else
			{
				clientWin.addOff_lineFriendsList(k);
			}

		}
	}

	// 发送信息 包括聊天信息和登出信息
	public synchronized void sendMsg(String msg) throws IOException
	{
		os.write(msg.getBytes());
	}

	// 读入字符串
	public String readString() throws IOException
	{
		byte[] bs = new byte[1024];
		int len = is.read(bs);
		if (len == -1)
		{
			try
			{
				Thread.sleep(100);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			return "noMsg";
		} else
		{
			String s = new String(bs, 0, len);
			return s;
		}
	}

	public static void main(String[] args)
	{
		try
		{
			Client client = new Client();

		} catch (UnknownHostException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}

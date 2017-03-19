package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.JOptionPane;

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

	public Vector<String> onlineUsers = new Vector<String>();
	public HashMap<String, ChatWin> chattingUsers = new HashMap<String, ChatWin>();

	public String name = "name";
	public boolean is_logout = false;

	public Client() throws UnknownHostException, IOException
	{
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
				} else if (splitMsg[0].equals("updateOnlineUser")) // 是用户登录或登出
				{
					readOnlineUsers();
					clientWin.updateUserList();
				} else if (splitMsg[0].equals("ServerClosed")) // 服务器关闭
				{
					JOptionPane.showMessageDialog(null, "服务器已关闭！", "警告", JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}

			} catch (IOException e)
			{
				e.printStackTrace();
			} catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}
		}
	}

	// 连接服务器
	public void connet() throws IOException
	{
		socket = new Socket(InetAddress.getLocalHost(), 40000);
		is = socket.getInputStream();
		os = socket.getOutputStream();
	}

	// 发送用户名
	public void sendName() throws IOException
	{
		os.write(name.getBytes());
	}

	// 读取在线用户名
	public void readOnlineUsers() throws ClassNotFoundException, IOException
	{
		ois = new ObjectInputStream(is); // 每次读取都新创建一个流对象
		onlineUsers = (Vector<String>) ois.readObject();
	}

	// 发送信息 包括聊天信息和登出信息
	public synchronized void sendMsg(String msg) throws IOException
	{
		os.write(msg.getBytes());
	}

	// 读入字符串
	private String readString() throws IOException
	{
		byte[] bs = new byte[1024];
		int len = is.read(bs);
		String s = new String(bs, 0, len);
		return s;
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

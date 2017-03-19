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
 * �ڴ���ObjectInputStream����ʱ����ObjectOutputStream����������ͷ��Ϣ�����û����Ϣ��һֱ������
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

				if (splitMsg[0].equals("chatMsg")) // ��������Ϣ
				{
					clientWin.analyzeMsg(msg);
				} else if (splitMsg[0].equals("updateOnlineUser")) // ���û���¼��ǳ�
				{
					readOnlineUsers();
					clientWin.updateUserList();
				} else if (splitMsg[0].equals("ServerClosed")) // �������ر�
				{
					JOptionPane.showMessageDialog(null, "�������ѹرգ�", "����", JOptionPane.ERROR_MESSAGE);
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

	// ���ӷ�����
	public void connet() throws IOException
	{
		socket = new Socket(InetAddress.getLocalHost(), 40000);
		is = socket.getInputStream();
		os = socket.getOutputStream();
	}

	// �����û���
	public void sendName() throws IOException
	{
		os.write(name.getBytes());
	}

	// ��ȡ�����û���
	public void readOnlineUsers() throws ClassNotFoundException, IOException
	{
		ois = new ObjectInputStream(is); // ÿ�ζ�ȡ���´���һ��������
		onlineUsers = (Vector<String>) ois.readObject();
	}

	// ������Ϣ ����������Ϣ�͵ǳ���Ϣ
	public synchronized void sendMsg(String msg) throws IOException
	{
		os.write(msg.getBytes());
	}

	// �����ַ���
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

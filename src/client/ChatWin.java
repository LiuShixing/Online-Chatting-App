package client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ChatWin extends JFrame implements Runnable
{
	private String name = "name";
	private Client user;

	private JPanel jPanelBlow = new JPanel(new FlowLayout());
	private JPanel jPanelUp = new JPanel(new FlowLayout());
	private JScrollPane jScrollChatMsg = new JScrollPane();
	private JTextArea jtChatMsg = new JTextArea(20, 30);
	private JTextField jtEditMsg = new JTextField(20);
	private JButton jbSend = new JButton("发送");

	SimpleDateFormat df = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");// 日期格式

	public ChatWin(String name, Client user)
	{
		super(name);

		setName("ChatWin Thread: "+ "base:"+user.name+"chatwith: "+name);
		
		this.name = name;
		this.user = user;

		jtChatMsg.setLineWrap(true);// 激活自动换行功能

		jtChatMsg.setEditable(false);

		jScrollChatMsg.add(jtChatMsg);
		jScrollChatMsg.setViewportView(jtChatMsg);
		jScrollChatMsg.setVisible(true);

		jPanelUp.add(jScrollChatMsg);

		jPanelBlow.add(jtEditMsg);
		jPanelBlow.add(jbSend);

		Container con = getContentPane();
		con.setLayout(new BorderLayout());
		con.add(jPanelBlow, BorderLayout.SOUTH);
		con.add(jPanelUp, BorderLayout.NORTH);

		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				user.chattingUsers.remove(name);
				setVisible(false);
			}
		});
		jtEditMsg.addKeyListener(new KeyListener()
		{
			@Override
			public void keyTyped(KeyEvent e)
			{
				if (e.getKeyChar() == KeyEvent.VK_ENTER)
				{
					if (jtEditMsg.getText() != "")
						sendMsg();
					jtEditMsg.setText("");
				}
			}

			@Override
			public void keyReleased(KeyEvent e)
			{

			}

			@Override
			public void keyPressed(KeyEvent e)
			{

			}
		});
		jbSend.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (jtEditMsg.getText() != "")
					sendMsg();
				jtEditMsg.setText("");
			}
		});
		setLocation(200, 200);
		setSize(400, 500);
		setResizable(false);
		setVisible(true);
	}

	private void sendMsg()
	{
		// 消息格式为 “chatMsg 名字 内容
		String msg = new String("chatMsg" + " " + name + " " + jtEditMsg.getText());
		try
		{
				user.sendMsg(msg);
				setMsgArea(jtEditMsg.getText());
		} catch (IOException e)
		{
			e.printStackTrace();
		}	
	}

	public void receiveMsg(String msg)
	{
		setMsgArea(msg);
	}

	private void setMsgArea(String s)
	{
		String date = df.format(new java.util.Date());
		jtChatMsg.append(date + '\n');
		jtChatMsg.append(s +'\n' );
		jtChatMsg.append("\n");

		jtChatMsg.setCaretPosition(jtChatMsg.getDocument().getLength()); // 保持显示最新
	}

	@Override
	public void run()
	{

	}
}

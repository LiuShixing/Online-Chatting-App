package server;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ServerWin extends JFrame
{
	public JPanel jpMsg = new JPanel(new BorderLayout());
	public JPanel jpUser = new JPanel(new BorderLayout());

	JLabel jlMsg = new JLabel("聊天信息");
	JLabel jlUser = new JLabel("在线用户");

	private final int TEXT_ROWS = 20;
	private final int TEXT_MSG_COLS = 30;
	private final int TEXT_USER_COLS = 12;
	JTextArea jtMsg = new JTextArea(TEXT_ROWS, TEXT_MSG_COLS);
	JTextArea jtUser = new JTextArea(TEXT_ROWS, TEXT_USER_COLS);

	JScrollPane jspMsg = new JScrollPane();
	JScrollPane jspUser = new JScrollPane();

	SimpleDateFormat df = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");// 日期格式

	// 创建锁
	private Object jtMsgLock = new Object();
	private Object jtUserLock = new Object();

	public ServerWin()
	{
		super("服务器");

		jtMsg.setLineWrap(true);// 激活自动换行功能
		jtUser.setLineWrap(true);

		jtMsg.setEditable(false);
		jtUser.setEditable(false);

		jpMsg.add(jlMsg, BorderLayout.NORTH);
		jspMsg.add(jtMsg);
		jspMsg.setViewportView(jtMsg);
		jspMsg.setVisible(true);
		jpMsg.add(jspMsg, BorderLayout.SOUTH);

		jpUser.add(jlUser, BorderLayout.NORTH);
		jspUser.add(jtUser);
		jspUser.setViewportView(jtUser);
		jspUser.setVisible(true);
		jpUser.add(jspUser, BorderLayout.SOUTH);

		Container con = getContentPane();
		con.setLayout(new FlowLayout());

		con.add(jpMsg);
		con.add(jpUser);

		addWindowListener(new WindowAdapter()
		{

			@Override
			public void windowClosing(WindowEvent e)
			{
				try
				{
					Server.closeServer();
				} catch (IOException e1)
				{

					e1.printStackTrace();
				}
				setVisible(false);
			}

		});

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocation(100, 200);
		setSize(500, 450);
		setResizable(false);
		setVisible(true);

	}

	void addMsg(String msg)
	{
		synchronized (jtMsgLock)
		{
			String date = df.format(new java.util.Date());
			jtMsg.append(date + '\n');
			jtMsg.append(msg + '\n');
			jtMsg.setCaretPosition(jtMsg.getDocument().getLength()); // 保持显示最新
		}
	}

	void updateUserArea(String name, boolean is_in)
	{
		synchronized (jtUserLock)
		{
			String date = df.format(new java.util.Date());
			jtUser.append(date + '\n');
			if (is_in)
				jtUser.append(name + " login " + '\n');
			else
				jtUser.append(name + " logout " + '\n');
		}
		jtUser.setCaretPosition(jtUser.getDocument().getLength()); // 保持显示最新
	}
}

package client;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public class ClientWin extends JFrame
{
	private Client user;

	JScrollPane jspUsers = new JScrollPane();
	JPanel jPanel = new JPanel(new BorderLayout());

	JList userList;

	Vector<ChatWin> chatWins = new Vector<ChatWin>();

	public ClientWin(Client user)
	{
		super(user.name);
		this.user = user;

		userList = new JList(user.onlineUsers);
		updateUserList();

		jspUsers.setBounds(0, 0, 450, 400);
		jspUsers.add(userList);
		jspUsers.setViewportView(userList);

		jspUsers.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		JLabel jl = new JLabel("ÔÚÏß", JLabel.CENTER);
		jPanel.add(jl, BorderLayout.NORTH);
		jPanel.add(jspUsers, BorderLayout.CENTER);

		getContentPane().add(jPanel);

		userList.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (userList.getSelectedIndex() != -1)
				{
					if (e.getClickCount() == 2)
					{
						String select = (String) userList.getSelectedValue();

						ChatWin chatWin = new ChatWin(select, user);
						Thread chatThread = new Thread(chatWin);
						chatThread.start();
						user.chattingUsers.put(select, chatWin);
					}
				}

			}

		});

		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				try
				{
					user.sendMsg("closed");
					user.is_logout = true;
				} catch (IOException e1)
				{
					e1.printStackTrace();
				}
				super.windowClosing(e);
			}
		});

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocation(100, 200);
		setSize(300, 450);
		setResizable(false);
		setVisible(true);
	}

	public void updateUserList()
	{
		userList.removeAll();
		userList.setListData(user.onlineUsers);
	}

	public void analyzeMsg(String msg)
	{
		String[] splitMsg = msg.split(" ");
		StringBuffer msgBuffer = new StringBuffer(msg);

		int lenght = splitMsg[0].length();
		int nameLenght = splitMsg[1].length();

		String msgContent = new String();
		msgContent = msgBuffer.substring(lenght + nameLenght + 2);

		String newMsg = splitMsg[1] + ": " + msgContent;

		if (user.chattingUsers.containsKey(splitMsg[1]))
		{
			user.chattingUsers.get(splitMsg[1]).receiveMsg(newMsg);
		} else
		{
			ChatWin chatWin = new ChatWin(splitMsg[1], user);
			Thread chatThread = new Thread(chatWin);
			chatThread.start();
			user.chattingUsers.put(splitMsg[1], chatWin);

			chatWin.receiveMsg(newMsg);
		}
	}
}

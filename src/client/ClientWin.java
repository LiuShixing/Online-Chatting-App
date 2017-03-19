package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;

public class ClientWin extends JFrame
{
	private Client user;

	JLabel jLabel_friends = new JLabel("好友", JLabel.CENTER);
	JButton jButton_addfriend = new JButton("+");
	JLabel jLabel_groups = new JLabel("群", JLabel.CENTER);
	JButton jButton_addgroup = new JButton("+");

	JScrollPane jspFriends = new JScrollPane();
	JScrollPane jspGroups = new JScrollPane();

	JPanel jPanel_Friends = new JPanel(new BorderLayout());
	JPanel jPanel_Groups = new JPanel(new BorderLayout());

	DefaultListModel dfriendslistModel = new DefaultListModel<String>();
	JList friendsList = new JList(dfriendslistModel);

	DefaultListModel dgroupslistModel = new DefaultListModel<String>();
	JList groupsList = new JList(dgroupslistModel);

	private int off_lineindex = 1;
	private int onlineCount = 0;
	private int off_lineCount = 0;

	public ClientWin(Client user)
	{
		super(user.name);
		this.user = user;

		updateFriendsListTitle();

		jspFriends.add(friendsList);
		jspFriends.setViewportView(friendsList);
		jspFriends.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		jPanel_Friends.add(jLabel_friends, BorderLayout.NORTH);
		jPanel_Friends.add(jspFriends, BorderLayout.CENTER);
		jPanel_Friends.add(jButton_addfriend, BorderLayout.SOUTH);

		jPanel_Groups.add(jLabel_groups, BorderLayout.NORTH);
		jPanel_Groups.add(jspGroups, BorderLayout.CENTER);
		jPanel_Groups.add(jButton_addgroup, BorderLayout.SOUTH);

		jspGroups.add(groupsList);
		jspGroups.setViewportView(groupsList);
		jspGroups.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		jPanel_Groups.add(jLabel_groups, BorderLayout.NORTH);
		jPanel_Groups.add(jspGroups, BorderLayout.CENTER);

		Container con = getContentPane();
		con.setLayout(new GridLayout(1, 2));
		con.add(jPanel_Friends);
		con.add(jPanel_Groups);

		jButton_addfriend.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				AddWin.getAddWin(user, "friend");
			}
		});
		jButton_addgroup.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				AddWin.getAddWin(user, "group");

			}
		});
		groupsList.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (groupsList.getSelectedIndex() != -1)
				{
					if (e.getClickCount() == 2)
					{
						String select = (String) groupsList.getSelectedValue();

					}
				}

			}

		});
		friendsList.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				int index = friendsList.getSelectedIndex();
				if ((index != -1) && (index != 0) && (index != off_lineindex))
				{
					if (e.getClickCount() == 2)
					{
						String select = (String) friendsList.getSelectedValue();

						if (!user.chattingUsers.containsKey(select))
						{
							ChatWin chatWin = new ChatWin(select, user);
							Thread chatThread = new Thread(chatWin);
							chatThread.start();
							user.chattingUsers.put(select, chatWin);
						} else
						{
							user.chattingUsers.get(select).setAlwaysOnTop(true);
							user.chattingUsers.get(select).setAlwaysOnTop(false);
						}
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
					user.shutdown();
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
		// setResizable(false);
		setVisible(true);
	}

	private void updateFriendsListTitle()
	{
		if (!dfriendslistModel.isEmpty())
		{
			dfriendslistModel.remove(0);
			dfriendslistModel.remove(off_lineindex-1);
		}
		dfriendslistModel.insertElementAt("                  在线 (" + onlineCount + ")", 0);
		dfriendslistModel.insertElementAt("                  离线 (" + off_lineCount + ")", off_lineindex);
	}

	public void updateFriendsList(String type, String name)
	{
		dfriendslistModel.removeElement(name);
		if (type.equals("friendLogin"))
		{
			off_lineCount--;
			addOnlineFriendsList(name);
		} else if (type.equals("friendLogout"))
		{
			onlineCount--;
			off_lineindex--;  //之前删除的是在线的  ，记得 离线 小标题已前移
			addOff_lineFriendsList(name);
		}
	}

	public void addOnlineFriendsList(String name)
	{
		dfriendslistModel.insertElementAt(name, 1);
		onlineCount++;
		
		off_lineindex++;
		updateFriendsListTitle();
	}

	public void addOff_lineFriendsList(String name)
	{
		dfriendslistModel.insertElementAt(name, off_lineindex + 1);
		off_lineCount++;
		updateFriendsListTitle();
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

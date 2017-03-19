package client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.Closeable;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class AddWin extends JFrame implements ActionListener
{
	private Client user;
	private String type = "type";
	private JTextField jtfname;
	private JButton jbAdd;

	private static AddWin addWin = null;

	public static AddWin getAddWin(Client user, String type)
	{
		if (addWin == null)
		{
			addWin = new AddWin(user, type);
			return addWin;
		} else
		{
			return addWin;
		}
	}

	private AddWin(Client user, String type)
	{
		super("add" + type);
		this.user = user;
		this.type = type;

		jtfname = new JTextField("输入" + type + "的名字", 15);
		jbAdd = new JButton("添加");

		jtfname.addActionListener(this);
		jbAdd.addActionListener(this);

		JPanel jPanel = new JPanel();
		jPanel.add(jtfname);
		jPanel.add(jbAdd);
		getContentPane().add(jPanel);

		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setLocation(200, 200);
		setSize(250, 80);
		setResizable(false);
		setVisible(true);

		addWindowListener(new WindowAdapter()
		{

			@Override
			public void windowClosing(WindowEvent e)
			{
				addWin = null;
			}

		});
	}

	public void close()
	{
		setVisible(false);
		addWin = null;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		String name = jtfname.getText();
		if (!user.friends.containsKey(name)) // 不是好友
		{
			String msg = "apply" + " " + type + " " + name;
			try
			{
				user.sendMsg(msg);
				JOptionPane.showMessageDialog(null, "请求已发出，请等待回应");
				close();
			} catch (IOException e1)
			{
				e1.printStackTrace();
			}
		} else
		{
			JOptionPane.showMessageDialog(null, name + "已经是你的好友！");
		}

	}

}

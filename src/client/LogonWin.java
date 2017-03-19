package client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.omg.CORBA.PRIVATE_MEMBER;

public class LogonWin extends JFrame
{
	private JLabel jlUserName = new JLabel("用户名");
	private JTextField jTextFieldUserNmae = new JTextField(10);

	private JLabel jlPassword = new JLabel("密码    ");
	private JPasswordField jTextFieldPassword = new JPasswordField(10);

	private JButton jbLogon = new JButton("登录");
	private JButton jbRegister = new JButton("注册");

	Client user;

	private LogonWin logonWin;

	public LogonWin(Client user)
	{
		super("登录");
		this.user = user;
		logonWin = this;

		Container con = getContentPane();
		con.setLayout(new FlowLayout());

		JPanel jp1 = new JPanel(new FlowLayout());
		jp1.add(jlUserName);
		jp1.add(jTextFieldUserNmae);
		JPanel jp2 = new JPanel(new FlowLayout());
		jp2.add(jlPassword);
		jp2.add(jTextFieldPassword);

		JPanel jpSouth = new JPanel(new FlowLayout());
		jpSouth.add(jbLogon);
		jpSouth.add(jbRegister);

		con.add(jp1);
		con.add(jp2);
		con.add(jpSouth);

		jTextFieldUserNmae.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				login();
			}
		});
		jTextFieldPassword.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				login();
			}
		});
		jbLogon.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				login();
			}
		});
		jbRegister.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				new Register(user, logonWin);
			}
		});

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocation(100, 200);
		setSize(250, 150);
		setResizable(false);
		setVisible(true);
	}

	private void login()
	{
		String name = jTextFieldUserNmae.getText();
		String[] nameSplit = name.split(" ");

		char[] passWord = jTextFieldPassword.getPassword();
		String pws = new String(passWord);

		if (name.equals("") || nameSplit.length <= 0)
			JOptionPane.showMessageDialog(null, "用户名不能为空！", "警告", JOptionPane.ERROR_MESSAGE);
		else if (nameSplit.length > 1)
		{
			JOptionPane.showMessageDialog(null, "用户名不能包含空格！", "警告", JOptionPane.ERROR_MESSAGE);
		} else if (pws.length() < 1 || pws.contains(" "))
		{
			JOptionPane.showMessageDialog(null, "密码不能为空或包含空格！", "警告", JOptionPane.ERROR_MESSAGE);
		} else 
		{
			try
			{
				user.connet();
				user.sendMsg("login" + " " + name + " " + pws);
				String chectIfo = user.readString(); // 获取检查结果
				if (chectIfo.equals("pass"))
				{
					user.name = new String(name);
					setVisible(false);
					user.clientWin = new ClientWin(user);
					user.readFriends();
					user.start();
				}else if(chectIfo.equals("onlined"))
				{
					JOptionPane.showMessageDialog(null, "用户已经登录，不可重复登陆！", "警告", JOptionPane.ERROR_MESSAGE);
				}else if(chectIfo.equals("nopass"))
				{
					JOptionPane.showMessageDialog(null, "用户名或密码错误！", "警告", JOptionPane.ERROR_MESSAGE);
				}

			} catch (IOException e1)
			{
				JOptionPane.showMessageDialog(null, "未开启服务器", "警告", JOptionPane.ERROR_MESSAGE);
				System.exit(0);

			} catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}
		}
	}

}

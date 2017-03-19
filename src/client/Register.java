package client;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class Register extends JFrame
{
	private JLabel jlUserName = new JLabel("用户名     ");
	private JTextField jTextFieldUserNmae = new JTextField(10);

	private JLabel jlPassword = new JLabel("密码         ");
	private JPasswordField jFieldPassword = new JPasswordField(10);

	private JLabel jlAgain = new JLabel("确认密码");
	private JPasswordField jTextFieldAgain = new JPasswordField(10);

	private JButton jbRegister = new JButton("注册");

	private Client user;
	private LogonWin logonWin;

	public Register(Client user, LogonWin logonWin)
	{
		super("注册");
		this.user = user;
		this.logonWin = logonWin;

		Container con = getContentPane();
		con.setLayout(new FlowLayout());

		JPanel jpName = new JPanel(new FlowLayout());
		jpName.add(jlUserName);
		jpName.add(jTextFieldUserNmae);

		JPanel jpPassWord = new JPanel(new FlowLayout());
		jpPassWord.add(jlPassword);
		jpPassWord.add(jFieldPassword);

		JPanel jpPassWordAgain = new JPanel(new FlowLayout());
		jpPassWordAgain.add(jlAgain);
		jpPassWordAgain.add(jTextFieldAgain);

		con.add(jpName);
		con.add(jpPassWord);
		con.add(jpPassWordAgain);
		con.add(jbRegister);

		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				setVisible(false);
			}
		});
		jTextFieldAgain.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				register();
			}
		});
		jbRegister.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				register();
			}
		});

		setLocation(100, 200);
		setSize(250, 200);
		setResizable(false);
		setVisible(true);

	}

	private void register()
	{
		String name = jTextFieldUserNmae.getText();
		String[] nameSplit = name.split(" ");

		char[] passWord = jFieldPassword.getPassword();
		char[] passWordAgain = jTextFieldAgain.getPassword();

		String pws = new String(passWord);
		String pwas = new String(passWordAgain);

		if (name.equals("") || nameSplit.length <= 0)
			JOptionPane.showMessageDialog(null, "用户名不能为空！", "警告", JOptionPane.ERROR_MESSAGE);
		else if (nameSplit.length > 1)
		{
			JOptionPane.showMessageDialog(null, "用户名不能包含空格！", "警告", JOptionPane.ERROR_MESSAGE);
		} else if (pws.length() == 0)
		{
			JOptionPane.showMessageDialog(null, "密码不能为空！", "警告", JOptionPane.ERROR_MESSAGE);
		} else if (pws.contains(" "))
		{
			JOptionPane.showMessageDialog(null, "密码不能包含空格！", "警告", JOptionPane.ERROR_MESSAGE);
		}
		
		String userIfo = new String("register" + " " + name + " " + pws);

		try
		{
			user.connet();
			user.sendMsg(userIfo);
			String registerResult = user.readString();

			if (registerResult.equals("registerPass"))
			{
				user.name = new String(name);
				logonWin.setVisible(false);
				setVisible(false);
				user.clientWin = new ClientWin(user);
				user.start();
			}
			else if(registerResult.equals("registerNoPass"))
			{
				JOptionPane.showMessageDialog(null, "该用户名已被占用！", "警告", JOptionPane.ERROR_MESSAGE);
			}
		} catch (IOException e1)
		{
			JOptionPane.showMessageDialog(null, "未开启服务器", "警告", JOptionPane.ERROR_MESSAGE);
			System.exit(0);

		}

	}

}

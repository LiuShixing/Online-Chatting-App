package client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.omg.CORBA.PRIVATE_MEMBER;

public class LogonWin extends JFrame
{
	private JLabel jlUserName = new JLabel("用户名");
	private JTextField jTextFieldUserNmae = new JTextField(10);
	private JButton jbLogon = new JButton("登录");

	Client user;

	public LogonWin(Client user)
	{
		super("登录");
		this.user=user;

		Container con = getContentPane();
		con.setLayout(new BorderLayout());

		JPanel jp = new JPanel(new FlowLayout());
		jp.add(jlUserName);
		jp.add(jTextFieldUserNmae);

		con.add(jp, BorderLayout.CENTER);
		con.add(jbLogon, BorderLayout.SOUTH);

		jTextFieldUserNmae.addActionListener(new ActionListener()
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

		
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocation(100, 200);
		setSize(300, 200);
		setResizable(false);
		setVisible(true);
	}
	private void login()
	{
		String s = jTextFieldUserNmae.getText();
		String[] split = s.split(" ");
		if (s.equals("") || split.length <=0 )
			JOptionPane.showMessageDialog(null, "用户名不能为空！", "警告", JOptionPane.ERROR_MESSAGE);
		else if(split.length>1)
		{
			JOptionPane.showMessageDialog(null, "用户名不能包含空格！", "警告", JOptionPane.ERROR_MESSAGE);
		}
		else
		{
			user.name = new String(s);
			setVisible(false);
			try
			{
				user.connet();
				user.sendName();
				user.readOnlineUsers();  
			} catch (IOException e1)
			{
				JOptionPane.showMessageDialog(null, "未开启服务器", "警告", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			
			} catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}
			user.clientWin = new ClientWin(user);

			user.start();
		}
	}

}

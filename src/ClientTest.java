/*********************************************
 * 此為聊天室的Client主程式                                          *
 * 姓名:李士暄                                                                 *                                          
 * 學號:994003063                             *
 * 程式限制: 1.client登入後 ，暱稱預設為ClientID  *
 *            可自行修改暱稱                                          *       
 *          2.目前當Server剔除使用者後會變成關閉所有使用者連線
 *            這個bug尚未解決= =+              * 
 *                                           * 
 *                                           */ 
import javax.swing.*;

public class ClientTest
{	
	private static Client c;//宣告client端
	
	public static void main(String[] args)
	{
		String nick = "ClientID";//預設為 clientID
		c = new Client(nick, "127.0.0.1");
		c.setVisible(true);
		c.setSize(400, 300);
		c.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		c.runClient();
	}
	
	/*****************改變client的title***************************/
	public static void changeNick(String nick)
	{
		c.setTitle(nick);
	}
}

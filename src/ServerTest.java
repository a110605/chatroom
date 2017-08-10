/**********************************************
 * 此為聊天室的Server主程式                                           *
 * 姓名:李士暄                                                                   *                                          
 * 學號:994003063                              *
 * 程式限制: Server做到可「剔除使用者」、「廣播訊息」功能 * 
 *          並管理使用者的連線與訊息傳輸                    *       
 *                                            * 
 *                                            * 
 *                                            */ 
import javax.swing.JFrame;

public class ServerTest
{
	public static void main(String[] args)
	{			
		Server server = new Server();
		server.setVisible(true);
		server.setSize(600,400);
		server.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		server.runServer();
	}
	
}

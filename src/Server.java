import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Server extends JFrame
{
  
  private static JTextArea textarea=new JTextArea();//宣告主畫布為static
  private static JList list;//儲存線上名單的list
  private JLabel label1=new JLabel("線上人數:");
  private JLabel label2=new JLabel("系統廣播 :");
  private JLabel label3=new JLabel("線上名單");
  private static JTextField count=new JTextField();
  private static JTextField broadcast=new JTextField();
  private JPanel northpanel=new JPanel();
  private JPanel southpanel=new JPanel();
  private JPanel panel=new JPanel();
  private JPanel eastpanel=new JPanel();
  private JButton button=new JButton("剔除連線");
  private ServerSocket server; // server socket
  private final int SERVER_PORT = 12345;
  private Socket connection; // connection to client
  private int clientnumber;//線上人數
  static protected Set<ClientHandler> ClientHandlerSet = new HashSet<ClientHandler>();
  static DefaultListModel nickListModel;//儲存列表的類別
  static ClientHandler clientHandler;
  
  public Server()
  {
	  super("聊天室伺服器");
	  
	  this.setLayout(new BorderLayout());
	  
	  add(northpanel,BorderLayout.NORTH);
	  northpanel.setLayout(new GridLayout(1,4));
	  northpanel.add(label1);
	  northpanel.add(count); 
	  //northpanel.add(label2);
	  //northpanel.add(broadcast);
     
	  add(panel,BorderLayout.CENTER);
	  panel.setLayout(new BorderLayout());
      panel.add(new JScrollPane(textarea,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),BorderLayout.CENTER);
      
      add(southpanel,BorderLayout.SOUTH);
      southpanel.setLayout(new GridLayout(1,2));
      southpanel.add(label2);
	  southpanel.add(broadcast);
	
	  nickListModel = new DefaultListModel();
	  list=new JList(nickListModel);
	  
	  add(eastpanel,BorderLayout.EAST);
	  eastpanel.setLayout(new BorderLayout());
	  eastpanel.add(label3,BorderLayout.NORTH);
	  eastpanel.add(list,BorderLayout.CENTER);
	  eastpanel.add(button,BorderLayout.SOUTH);
      textarea.setEditable(false);//主畫布不可修改
	  count.setEditable(false);//人數不可被修改
	
	  button.addActionListener(
			  new ActionListener()
		{ 
				  public void actionPerformed(ActionEvent event)
				  { 
					  kickNick();//剔除使用者
				  }
	    }
			  
			  );
	
	  broadcast.addActionListener
	  (
	    new ActionListener()
	    {
			public void actionPerformed( ActionEvent event )
	    	{ 
				sendBroadcastData( broadcast.getText());//廣播
	            broadcast.setText("");
	        }
	    }
	 );

	  
	  
	  
  }//end Server()
  
  public void runServer()
  {
     try // 建立 server socket 開始監聽連線
     {
        server = new ServerSocket( 12345, 100 ); // 建立 socket
        clientnumber = 0;
        int i = 1;//使用者的ID
        
        displayMessage("阿包的聊天室...\n");
        displayMessage(getDateTime()+"<系統公告> 聊天室已上線，等待使用者的連線\n");
        displayMessage("等待連線中...\n\n");

        boolean shutdown = false;
        
        while ( !shutdown )
        {
       	 connection = server.accept(); // server持續監聽連線直到有client再次連上線
       	 
       	 clientHandler = new ClientHandler(connection, i);
         ClientHandlerSet.add(clientHandler);
       	 clientHandler.start(); //利用多執行緒啟動
       	 clientnumber++;
       	 i++;
       	 
       	 String number = Integer.toString(clientnumber);  //將clientnumber轉成String
       	 count.setText(number);
        } // end while
     }
     catch ( IOException ioException ) 
     {
        displayMessage("Socket連線失敗");
     }
  } // end method runServer
  
  /************************顯示訊息**************************************/
  public static void displayMessage(final String messageToDisplay)
  {
	  SwingUtilities.invokeLater(new Runnable()
	  {
		public void run()
		{
			textarea.append(messageToDisplay);
		}
		  
	  });
  }
  
  /************************取得時間**************************************/
  public static String getDateTime()
  {
      DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
      Date date = new Date();
      return dateFormat.format(date);
  }
  
  /************************系統傳輸廣播資料***********************************/
  private static void sendBroadcastData( String message )
  {
	    String temp = getDateTime()+ " <系統公告> ";
		temp += message;
		temp += "\n";
		displayMessage(temp);
		
		Iterator iter = ClientHandlerSet.iterator();
		while (iter.hasNext())
		{
			ClientHandler t = (ClientHandler) iter.next();
			t.sendData(temp);
		}
  } // end method sendData
  
  /************************系統傳輸廣播命令************************************/
  private static void sendBroadcastCommand( String message )
  {
	Iterator iter = ClientHandlerSet.iterator();
	
	while (iter.hasNext()) {
		ClientHandler t = (ClientHandler) iter.next();
		t.sendCommand(message);
	}
  } // end method sendBroadcastCommand
  
  
  /*************************設定廣播***********************************/
  public static void setTextFieldEditable( final boolean editable )
  {
     SwingUtilities.invokeLater(new Runnable()
     {
   	  public void run()
   	  {
   		  broadcast.setEditable( editable );
   	  }
   } 
     );
  } // end method setTextFieldEditable
  
  /***********************新增暱稱***************************************/
  public static void addNick(String nick) 
  {
		nickListModel.addElement(nick);
		String allNickTemp = "UPDATE_NICK ";
		allNickTemp += getAllNick();
		sendBroadcastCommand(allNickTemp);
  }
  
  /*********************取得所有暱稱**************************************/
  public static String getAllNick()
  {
		String output = "";
		for ( int i=0; i<nickListModel.getSize();i++ )
		{
			output += (String) nickListModel.elementAt(i);
			output += " ";
		}
		return output;
  }
  
  /*********************改暱稱*****************************************/
	public static void changeNick(String oldNick, String newNick)
   {
		nickListModel.removeElement(oldNick);
		nickListModel.addElement(newNick);
		
		String allNick = "UPDATE_NICK ";
		allNick += Server.getAllNick();
		sendBroadcastCommand(allNick);
		sendBroadcastData(oldNick+" 已經更換名字為 "+newNick);
	}//end changeNick
	
  /***********************剔除使用者**************************************/
	private void kickNick()
	{
		if (list.getSelectedValue() == null)
		{
			JOptionPane.showMessageDialog(this, "沒有選擇使用者", "錯誤" ,JOptionPane.ERROR_MESSAGE );
		}
		else 
		{
			String nick = (String) list.getSelectedValue();			
			String kickCommand = "KICK ";
			kickCommand += nick;
			//System.out.println(kickCommand);
			sendBroadcastCommand(kickCommand);
			sendBroadcastData("使用者 "+ nick + " 被系統管理員踢除！");
			clientnumber--;
	       	count.setText(Integer.toString(clientnumber));
			nickListModel.removeElement(nick);
			ClientHandlerSet.remove(clientHandler);
		}
	}//end kickNick()


}//end Server
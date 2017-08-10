import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.io.*;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Client extends JFrame
{
	private JPanel north=new JPanel();
	private JPanel south=new JPanel();
	private JPanel east=new JPanel();
	private JList clientlist;//存放線上名單
	private JLabel nicknamelabel=new JLabel("我的暱稱 :");
	private JLabel send=new JLabel("傳送訊息 :");
	private JLabel label3=new JLabel("線上名單");
	private static JTextArea clienttextarea=new JTextArea();
	private JTextField insertnickname=new JTextField();
	private JTextField sendmessage=new JTextField();
	@SuppressWarnings("rawtypes")
	private DefaultListModel nickListModel;	
	private String message="";
	private String nick;
	private String chatServer; // host server for this application
	private ObjectOutputStream output; // output stream to server
	private ObjectInputStream input; // input stream from server
	private boolean isKicked = false;//是否被t除
	private Socket client; // socket to communicate with server
	
	public Client(String nick , String hostIP)
	{
		super("Client");
		this.setLayout(new BorderLayout());
	
		this.nick = nick;
		this.chatServer=hostIP;
		
		north.add(nicknamelabel);
		north.add(insertnickname);
		north.setLayout(new GridLayout(1,2));
		add(north,BorderLayout.NORTH);
		
		add(new JScrollPane(clienttextarea,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),BorderLayout.CENTER);
		clienttextarea.setEditable(false);//主畫布不可修改
		
		add(south,BorderLayout.SOUTH);
		south.setLayout(new GridLayout(1,2));
		south.add(send);
		south.add(sendmessage);
		
		insertnickname.addActionListener(
				new ActionListener()
	            {
					public void actionPerformed(ActionEvent event)
					{
						changeNick(); //改變暱稱
					}
				}
				
			);
		
		sendmessage.addActionListener(
				new ActionListener()
				{
			     public void actionPerformed( ActionEvent event )
	    	     { 
			    	sendData(sendmessage.getText());//傳送使用者方的訊息 
	    	        sendmessage.setText("");
	    	     }
	            }
	 			);
		
	
		
		nickListModel = new DefaultListModel();
		clientlist=new JList(nickListModel);
		
		add(east,BorderLayout.EAST);
		east.setLayout(new BorderLayout());
		east.setFocusable(false);//主畫布不可修改
		east.add(label3,BorderLayout.NORTH);
		east.add(clientlist,BorderLayout.CENTER);
	}//end Client
	
	 public void runClient() 
	   {
	      try
	      {
	         connectToServer(); // create a Socket to make connection
	         getStreams(); // get the input and output streams
	         processConnection(); // process connection
	      }
	      catch ( EOFException eofException ) 
	      {
	         displayMessage( "客戶端關閉連線\n" );
	      }
	      catch ( IOException ioException ) 
	      {
	    	  displayMessage( "連線失敗\n" );
	      }
	      finally 
	      {
	         closeConnection();
	      }
	   } // end method runClient

	 /***************************連線到Server*********************************/
	   private void connectToServer() throws IOException, ConnectException
	   {      
	      displayMessage( "嘗試連線中...\n" );
	      client = new Socket( InetAddress.getByName( chatServer ), 12345 );
	      displayMessage( "連線到： "+client.getInetAddress().getHostName()+"\n" );
	   } // end method connectToServer

	   /************************ 建立串流***********************************/
	   private void getStreams() throws IOException
	   {
	      output = new ObjectOutputStream( client.getOutputStream() );      
	      output.flush();
	      input = new ObjectInputStream( client.getInputStream() );

	      displayMessage( "串流已經建立\n\n" );
	      sendCommand( "NEW_NICK "+nick );// 向server傳送新使用者的command
	   } // end method getStreams

	   /************************ 處理連線交談***********************************/
	   private void processConnection() throws IOException
	   {
	      setTextFieldEditable( true );//打開使用者輸入方格
	      do 
	      { 
	         try 
	         {
	            message = ( String ) input.readObject(); // read new message
	            /***** 分析收到的訊息中是否有命令 ****/
	            Scanner scanner = new Scanner(message);
	            String command = scanner.next();
	          
	            if (command.equals("UPDATE_NICK")) //若收到更新暱稱清單的訊息
	            {
	            	nickListModel.removeAllElements();//將所有使用者清單清空，再一一加入新的資料
	            	while(scanner.hasNext())
	            	{
	            		String newnick = scanner.next();
	            		nickListModel.addElement(newnick);
	            	}
	            }
	            else if (command.equals("KICK"))//若被T了
	            {
	            	while(scanner.hasNext())
	            	{
	            		String n = scanner.next();     
	    		    	nickListModel.removeElement(n);
	    		    	System.out.println("kick n="+n+", nick="+nick);
	    		    	if(n.equals(nick)){
	    		    		displayMessage(getDateTime()+" <系統公告> 您已被系統管理員踢除\n");
	    		    		isKicked= true;
	    		    	}
	            	}
	            	
	            	
	            }
	            else
	            {
	            	displayMessage( message ); // 顯示訊息
	            }
	         }
	         catch ( ClassNotFoundException classNotFoundException ) 
	         {
	            displayMessage( "收到錯誤的封包\n" );
	         }
	      } while ( !isKicked ); // kick user message
	   } // end method processConnection

	   /**********************關閉連線***************************************/
	   private void closeConnection() 
	   {
	      displayMessage("關閉連線...\n" );
	      setTextFieldEditable( false ); // disable enterField
	      System.out.println("client closeConnection()");
	      try 
	      {
	         output.close();
	         input.close();
	         client.close();
	         System.exit(0);
	      }
	      catch ( IOException ioException ) 
	      {
	    	  displayMessage(nick+"已中止連線");
	      }
	   } // end method closeConnection
	   
	   /*************************傳送Server端傳來的資料*********************************************/
	   private void sendData( String message )
	   {
	      try 
	      {
	         output.writeObject( getDateTime()+" <"+nick+"> 說： " + message+"\n" );
	         output.flush(); // flush data to output
	         displayMessage( getDateTime()+" <"+nick+"> 說： " + message + "\n" ); //在本地端顯示訊息
	      }
	      catch ( IOException ioException )
	      { 
	    	  clienttextarea.append( "傳送錯誤\n" ); 
	      }
	   } // end method sendData
	   
	/********************在Client端顯示訊息************************************/
	private void displayMessage(final String messageToDisplay)
	{
		SwingUtilities.invokeLater(
				new Runnable()
				{
					public void run()
					{
						clienttextarea.append(messageToDisplay);
					}
				}
				);
	}
	
	/********************傳送Server端的命令***************************************/
	 private void sendCommand( String message )
	   {
	      try
	      {
	         output.writeObject( message );
	         output.flush();
	      } // end try
	      catch ( IOException ioException )
	      {
	    	  clienttextarea.append( "傳送錯誤\n" ); 
	      }
	   } // end method sendData
	 
	 /*********************改變client端的暱稱**************************************/
	 private void changeNick()
	   {
		   String oldNick = this.nick; //舊暱稱
		   String newNick = insertnickname.getText(); //新暱稱
		  
		   if (newNick.equals("")){
			   JOptionPane.showMessageDialog(this, "請輸入暱稱", "錯誤" ,JOptionPane.ERROR_MESSAGE );
		   }else{
			   displayMessage("您已經更換暱稱為" + newNick + "\n" );
			   ClientTest.changeNick(newNick);
			   
			   //將更新過後的暱稱資訊傳送給所有clients
			   String allNick = "UPDATE_NICK "+oldNick+" "+newNick;
			   sendCommand(allNick);
			   this.nick = newNick;
			   System.out.println("changeNick() this.nick="+nick);
		   }  
	   }
	       
/**************************設定輸入方格*********************************************/
	 private void setTextFieldEditable( final boolean editable )
	   {
	      SwingUtilities.invokeLater(new Runnable()
	      {
	            public void run() // sets enterField's editability
	            {
	            	sendmessage.setEditable( editable );
	            }
	       }
	      );
	   } // end method setTextFieldEditable
	 
	 /*********************取得目前時間**********************************/
	 public static String getDateTime()
	    {
	        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	        Date date = new Date();
	        return dateFormat.format(date);
	    }
 
}//end Client

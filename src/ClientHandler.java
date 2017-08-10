/**********************************************
 * 此為處理client的副程式，  一旦server接收到socket *
 * 連線時，即產生此process利用multithreading的方  *
 * 式獨立處理client之連線。                       *
 *                                            *
 * 姓名:李士暄                                  *                                          
 * 學號:994003063                              *
 * ********************************************/ 
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.Scanner;

public class ClientHandler extends Thread
{
 protected int ID;//使用者ID
 protected Socket connection;//Socket 連線
 private ObjectOutputStream output;
 private ObjectInputStream input;
 
 public ClientHandler (Socket connection,int ID)
 {
	 this.ID=ID;
	 this.connection=connection;
 }//end ClientHandler()
  
 public void run()
 {
	 try
	 {
		getStreams();
		processConnection();
	 }
	 catch (IOException e) 
	 {
		//Server.displayMessage("使用者已離開聊天室\n");
	 }
	 finally 
	 {
	    closeConnection();
	 }
 }
 /*****************建立串流************************/
 private void getStreams() throws IOException
 {
	    output = new ObjectOutputStream( connection.getOutputStream() );
	    output.flush();
	    input = new ObjectInputStream( connection.getInputStream() );
	
	    Server.displayMessage( Server.getDateTime() + " <系統公告> 連線 "+ID+" 已建立\n" );
 } // end method getStreams
 
 /*******************處理連線***************************************/
 private void processConnection() throws IOException
 {
	 String message = "";
     String nick = "";
	  try {
		  message = (String) input.readObject();
		  Scanner scanner = new Scanner(message);
		  while (scanner.hasNext())
		  {
			  String temp = scanner.next();
			  //如果命令不等於NEW_NICK
			  if ( !temp.equals("NEW_NICK") )
			  {
				  nick = temp;
				  Server.addNick(nick);
			  }
		  }
		  
	  } 
	  catch (ClassNotFoundException e)
	  {
		Server.displayMessage("取得暱稱錯誤\n");
	  }
	  
	  String allNick = "UPDATE_NICK ";
	  allNick += Server.getAllNick();
	  sendBroadcastCommand(allNick);
	  
	  message = nick + " 加入聊天室\n";
      sendBroadcastData( message );
      Server.displayMessage(Server.getDateTime()+" <系統公告> "+message);
      Server.setTextFieldEditable( true );//啟動輸入格
      
      boolean shutdown = false;
      
      /**********開始接收client端傳送之訊息**********/
      while ( !shutdown )
      { 
         try
         {
            message = ( String ) input.readObject();
            Scanner scanner = new Scanner(message);
            String command = scanner.next();
            //如果是更換暱稱的command
            if ( command.equals("UPDATE_NICK") ) 
            {
            	String oldNick = scanner.next();
            	String newNick = scanner.next();        	
            	Server.changeNick(oldNick, newNick);
            }
            //若不是，就顯示出來
            else
            {
				
				Iterator iter = Server.ClientHandlerSet.iterator();
				while (iter.hasNext())
				{
					ClientHandler t = (ClientHandler) iter.next();
					if (t != this)
					t.sendData(message);
				}
				Server.displayMessage(message);
            }//end if
         }//end try
         catch ( ClassNotFoundException classNotFoundException ) 
         {
            Server.displayMessage( "收到錯誤的封包\n" );
         }
      }
	  
 }//end processConnection()
 
 
 /************************關閉連線****************************/
 private void closeConnection() 
 {
    //Server.displayMessage( "關閉連線中\n" );
    try 
    {
       output.close();
       input.close();
       connection.close();
       //System.out.println("handler closeConnection()");
    }
    catch ( IOException ioException ) 
    { 
    	Server.displayMessage( "連線錯誤!!" ); 
    }
 } // end method closeConnection
 
 /************************傳送命令***************************/
 public synchronized void sendCommand( String message )
 {
    try
    {
       output.writeObject( message );
       output.flush(); // flush output to client
    } // end try
    catch ( IOException ioException ) 
    { 
    	Server.displayMessage( "傳送錯誤!sendcommand\n" ); 
    }
 } // end method sendData
 
 /************************傳送訊息資料***************************/
 public synchronized void sendData(String message)
 {
     try
     {
        output.writeObject( message );
        output.flush();
     }
     catch ( IOException ioException ) 
     { 
    	 Server.displayMessage( "傳送錯誤!sendData\n" ); 
     }   
 }//end sendData()
 
 /*************************傳送廣播訊息資料************************/
	public synchronized void sendBroadcastData(String message)
	{
		String outputText = Server.getDateTime() + " <系統廣播> ";
		outputText += message;
	
		Iterator iter = Server.ClientHandlerSet.iterator();
		while (iter.hasNext())
		{
			ClientHandler t = (ClientHandler) iter.next();
			t.sendData(outputText);
		}
	}
	
	/***********************傳送廣播命令********************************/
	public synchronized void sendBroadcastCommand(String message)
	{
		@SuppressWarnings("rawtypes")
		
		Iterator iter = Server.ClientHandlerSet.iterator();
		while (iter.hasNext())
		{
			ClientHandler t = (ClientHandler) iter.next();
			t.sendCommand(message);
		}
	}
	
}//end ClientHandler

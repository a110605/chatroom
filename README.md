# Chatroom（聊天室)
此程式實做server端和client端聊天室的介面，當server和Client程式開始執行，便會使用Java socket進行連接，以進行資料的傳輸，而且server端有multithread的功能，以支援多個client端進行聊天。


## 功能
### Server 
- 利用HashSet資料結構管理client連線
- 顯示總線上人數 		
	- 當新的client加入時即時更新
- 廣播訊息
	- 在所有的client的顯示區顯示廣播內容
- 剔除client
	- Client被踢除後自動關閉視窗，並且server通知所有其餘clients
	
### Client
- 可更改暱稱 (預設：ClientID)
	- 更改後，及時更改server端的client暱稱清單
	- 更改後，在Client顯示線上清單(動態更新)
- 傳送訊息
	- Server以及所有client都可以看到

## 聯絡方式
若有任何問題或疑問，請不吝用 [a1106052000@gmail.com](a1106052000@gmail.com) 聯絡我


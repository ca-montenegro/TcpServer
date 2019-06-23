# TCP File Transfer Application made for CS Lab communication infrastructure. Universidad de los Andes. Bogotá, Colombia

This is a Java application that :

- uses TCP for file transfer.
	
- has a user interface buit using Java AWT and Java Swing.
	
- is built using Java Socket Programming.
	
- allows multiple users to connect to a server at once. It accomplishes this through multithreading.
	
- mandates the server to specify working directory as a command line argument with the freedom to specify a custom port number as the second argument. The default port number has been set to 3333.
	
- server assigns connection IDs to the clients connected.
	
- mandates client to specify working directory as a command line argument. It allows user to specify host address and if not specified, defaults it to Localhost. The user can also specify the port number as the third argument.
	
- Displays files and directories present in the server working directory and allows the client to select files and download them onto the client system.
	
- Allows the client to upload files to the server working directory. This allows 2 clients to transfer files through the server.

	

## How to use
```
git clone https://github.com/ca-montenegro/TcpServer.git
cd TcpTransferapp
```
For *server* :
```
javac TCPServer.java
java TCPServer ./DataServer/ [port. Default:3333]
```

For *client* :
```
javac TCPClient.java
java TCPClient ./DataClient/ [ip of TCP Server. Default: Localhost] [port. Default:3333]
```
Choose file to be downloaded from the panel and click on **download**. 
When download, a File-list of the client will be show. You can see the new file locally. 

**Server and Client can interact only through the same port number**

## Prerequisites

If you do not have Java installed, refer to this [Java installation guide](https://www.java.com/en/download/help/download_options.xml)

### Source
The initial source code and README.md was taken from [mansimarkaur repo](https://github.com/mansimarkaur/TCP-file-transfer)

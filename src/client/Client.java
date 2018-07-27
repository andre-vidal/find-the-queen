package client;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
	Socket socket;
	Scanner scan;
	DataInputStream clientInStream;
	DataOutputStream clientOutStream;
	
	public Client() {
		try {
			socket = new Socket("127.0.0.1", 7621);
			
			// retrieve input and out streams of client
	        clientInStream = new DataInputStream(socket.getInputStream());
	        clientOutStream = new DataOutputStream(socket.getOutputStream());
	        
	        listen();
	        
	    } catch (UnknownHostException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
	}
	
	public void listen() {
		scan = new Scanner(System.in);
		
		while(true) {
			
			try {
				// reduce resource usage
				while(clientInStream.available() == 0) {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}				
				}
				
				// accept message from server
				String fromServer = clientInStream.readUTF();
				
				// respond to message based on what it contains
				if(fromServer.contains("Invalid Credentials") || fromServer.contains("Thanks For Playing")) {
					this.close(fromServer);
					break;
				}else if(fromServer.contains("Waiting") || fromServer.contains("Started!")) {
					System.out.println(fromServer);
					continue;
				}else if(fromServer.contains("queen is")) {
					this.sendMessage(fromServer);
					continue;
				}else if(fromServer.contains("that round")) {
					System.out.println(fromServer);
					this.sendMessage(fromServer);
					continue;
				}else if(fromServer.contains("VICTORY") || fromServer.contains("DEFEAT")) {
					System.out.println(fromServer);
					continue;
				}else {
					System.out.println(fromServer);
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// reduce resource usage
			while(!scan.hasNextLine()) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}				
			}
			
			// accept input from user and send to server
			String toServer = scan.nextLine();			
			this.sendMessage(toServer);		
		}
	}
	
	public void sendMessage(String message) {
		try {
			this.clientOutStream.writeUTF(message);
			this.clientOutStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// close connections and resources
	public void close(String fromServer) {
		try {
			System.out.println(fromServer);
			clientInStream.close();
			clientOutStream.close();
	        System.out.println("closing this connection...");
			socket.close();		
			System.out.println("connection closed");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]) throws UnknownHostException, IOException {
		new Client();		 
	}
}

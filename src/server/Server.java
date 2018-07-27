package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Server {
	int playerCount = 0;
	ArrayList<ClientManager> clients = new ArrayList<ClientManager>();
	ServerSocket ssocket;
	
	public Server() {
		// list of player roles
		List<String> roles = new ArrayList<String>();		
        roles.add("DEALER");
        roles.add("SPOTTER");
		
		try {
			// create sever socket and listen on port 7621
			ssocket = new ServerSocket(7621);
			System.out.println("Server Listening on port 7621...");
			
			while(true) {
				// accept connection
				Socket socket = ssocket.accept();
				System.out.println("Connection received");

				// set random role
				String role = roles.get(new Random().nextInt(roles.size()));
				if(roles.size() > 1) {
					roles.remove(role);					
				}
				
				// create a new thread object
				ClientManager client = new ClientManager(this, socket, role);
				
				// start thread
				client.start();
				
				// add thread to list of clients
				clients.add(client);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}				
	}
	
	public int getPlayerCount() {
		return this.playerCount;
	}
	
	public void setPlayerCount() {
		this.playerCount++;
	}
	
	public static void main(String args[]) throws IOException {
        new Server();
	}
}

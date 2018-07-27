package server;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientManager extends Thread {

	Server server;
	Socket socket;
	DataInputStream socketInStream;
	DataOutputStream socketOutStream;
	String role;
	int score = 0;
	
	public ClientManager(Server server, Socket socket, String role) {
		this.server = server;
		this.socket = socket;
		this.role = role;
	}
	
	@Override
    public void run() 
    {
		// retrieve input and out streams of socket	
		try {
			this.socketInStream = new DataInputStream(socket.getInputStream());
			this.socketOutStream = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}

        System.out.println("created thread for:"+ this.socket);
        
        if(!this.login()) {
        	this.close("Invalid Credentials");
        } else {
        	try {
				this.play();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }
	
	public void sendMessage(String message) {
		try {
			this.socketOutStream.writeUTF(message);
			this.socketOutStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendMessageToOpponent(String message) {
		for(int i = 0; i < server.clients.size(); i++) {
			ClientManager temp = server.clients.get(i);
			if(temp != this) {
				temp.sendMessage(message);					
			}
		}
	}
	
	public void sendMessageToAll(String message) {
		for(int i = 0; i < server.clients.size(); i++) {
			ClientManager temp = server.clients.get(i);
			temp.sendMessage(message);					
		}
	}
	
	public void toggleRole() {
		if(this.role.equals("DEALER")) {
			this.role = "SPOTTER";
		}else {
			this.role = "DEALER";
		}
	}
	
	public String play() throws IOException {
		this.sendMessage("\n\n*===========================*\n"+
						     " Find the Queen Has Started!\n"+
						     "*===========================*\n");
		
		for(int i = 0; i<5; i++) {			
			if(this.role.equals("DEALER")) {
				this.actAsDealer(i);
			}else {
				this.actAsSpotter(i);
			}			
		
			this.toggleRole();
		}
		
		this.gameOver();
        
		return "";
	}
	
	public void actAsSpotter(int round) {
		String message = "\n<--Round "+(round+1)+"-->\nYou are the SPOTTER";
		message += "\n\nWaiting on dealer...\n";
		this.sendMessage(message);
		try {
			String[] parts = this.socketInStream.readUTF().split(" ");
			this.socketOutStream.writeUTF("Where is the queen? (1, 2 or 3)");
			String answer = this.socketInStream.readUTF();
			boolean finished = checkAnswer(parts[2], answer);
			this.socketInStream.readUTF();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void actAsDealer(int round) {
		String message = "\n<--Round "+(round+1)+"-->\nYou are the DEALER";
		message += "\nHide the Queen! (1, 2, or 3)";
		this.sendMessage(message);
		try {
			String position = this.socketInStream.readUTF();
			this.sendMessageToOpponent("queen is " + position);
			this.sendMessage("\nWaiting on spotter...\n");
			this.socketInStream.readUTF();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void gameOver() {
		if(this.score > 2) {
			this.sendMessage("\n\n*===========================*\n"+
						     "            VICTORY!\n"+
						     "*===========================*\n");
		} else {
			this.sendMessage("\n\n*===========================*\n"+
						     "            DEFEAT!\n"+
						     "*===========================*\n");
		}
		this.sendMessage("Thanks For Playing\n\n");
	}
	
	public boolean checkAnswer(String position, String answer) {
		if(position.equals(answer)) {
			this.sendMessage("\nGOOD JOB! you won that round");
			this.sendMessageToOpponent("\nOH NO! you lost that round");
			// increase score if this round was won
			this.increaseScore();
		}else {
			this.sendMessage("\nOH NO! you lost that round");
			this.sendMessageToOpponent("\nGOOD JOB! you won that round");
			// increase opponent's score if this round was lost 
			for(int i = 0; i < server.clients.size(); i++) {
				ClientManager temp = server.clients.get(i);
				if(temp != this) {
					temp.increaseScore();					
				}
			}
		}

		return true;
	}
	
	public void increaseScore() {
		this.score++;
	}
	
	public boolean login() {
		String username = null;
		String password = null;
		try {
			System.out.println(""+socket+"logging in...");
			
			// Prompt for username
            this.socketOutStream.writeUTF("Login to continue \n[username]");
            username = this.socketInStream.readUTF();
            
            // Prompt for password
            this.socketOutStream.writeUTF("[password]");
            password = this.socketInStream.readUTF();
            
            if(username.equals("dannyboi") && password.equals("dre@margh_shelled") || 
          	   username.equals("matty7") && password.equals("win&win99")) {
    			System.out.println(""+socket+"login successful");
             	this.sendMessage("Login successful\nWaiting for other player...");
             	server.setPlayerCount();
             	// while server.players < 2 wait here
             	while(server.getPlayerCount() != 2) {
             		try {
    					Thread.sleep(1);
    				} catch (InterruptedException e) {
    					e.printStackTrace();
    				}
             	}
             	return true;
            } else {
    			System.out.println(""+socket+"login failed");
            	return false;
            }
            
        } catch (IOException e) {
        	e.printStackTrace();
        	return false;
        }
	}
	
	public void close(String response) {
		try {
			this.sendMessage(response);
	        System.out.println("closing this connection...");
	        this.socket.close();
			System.out.println("connection closed");
			
			// closing resources
	        this.socketInStream.close();
	        this.socketOutStream.close();
		}catch (IOException e) {
        	e.printStackTrace();
        }
		
	}
  
}
                 
            


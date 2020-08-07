import java.net.Socket;
import java.io.PrintWriter;
import java.util.regex.*;
import java.util.*;

public class ClientServiceThread implements Runnable {

	int clientId;
	private Server myServer;
	private String availableCommands;

	public ClientServiceThread(int clientId, Server server) {
		this.myServer = server;
		this.clientId = clientId;
		availableCommands = "Available commands:\n\t1. Client X: <message>\n\t2. All: <message>\n\t3. Client X,Y: <message>\n\t4. Server: List All";
	}

	public void run() {
		try {
			// send available commands first
			synchronized (myServer.getWriteStream(myServer.getSocket(clientId))) {
				myServer.getWriteStream(myServer.getSocket(clientId)).println(availableCommands);
				myServer.getWriteStream(myServer.getSocket(clientId)).println("You are client " + clientId);
			}

			// communicate
			String textReceivedFromClient;
			while ((textReceivedFromClient = myServer.getReadStream(myServer.getSocket(clientId)).readLine()) != null) {
				System.out.println("Received from Client " + clientId + ": " + textReceivedFromClient);

				if (Pattern.matches("Client ([0-9])+:.*", textReceivedFromClient)) {
					// Client X: <message>
					int clientToSend = Integer.parseInt(textReceivedFromClient.substring(7, textReceivedFromClient.indexOf(':')));
					String textToSend = textReceivedFromClient.substring(textReceivedFromClient.indexOf(':') + 1).trim();

					PrintWriter os = myServer.getWriteStream(myServer.getSocket(clientToSend));
					if (os == null) {
						myServer.getWriteStream(myServer.getSocket(clientId))
								.println("Error: Client " + clientToSend + " does not exist");
						continue;
					}
					synchronized (os) {
						os.println("Client " + clientId + " says: " + textToSend);
						System.out.println("Sent message to client " + clientToSend);
					}

				} else if (Pattern.matches("All:.*", textReceivedFromClient)) {
					// All: <message>
					String textToSend = textReceivedFromClient.substring(textReceivedFromClient.indexOf(':') + 1).trim();
					Map<Integer, Socket> currentClientsMap = myServer.getMapSocket();
					for (Map.Entry<Integer, Socket> entry : currentClientsMap.entrySet()) {
						PrintWriter os = myServer.getWriteStream(myServer.getSocket(entry.getKey()));
						if (os == null) {
							continue;
						}
						synchronized (os) {
							os.println("Client " + clientId + " says: " + textToSend);
						}
					}
				} else if (Pattern.matches("Client ([0-9])+,([0-9])+:.*", textReceivedFromClient)) {
					// Client X,Y: <message>
					int clientToSend1 = Integer
							.parseInt(textReceivedFromClient.substring(7, textReceivedFromClient.indexOf(',')));
					int clientToSend2 = Integer.parseInt(textReceivedFromClient.substring(textReceivedFromClient.indexOf(',') + 1,
							textReceivedFromClient.indexOf(':')));

					String textToSend = textReceivedFromClient.substring(textReceivedFromClient.indexOf(':') + 1).trim();

					PrintWriter os = myServer.getWriteStream(myServer.getSocket(clientToSend1));
					if (os == null) {
						myServer.getWriteStream(myServer.getSocket(clientId))
								.println("Error: Client " + clientToSend1 + " does not exist");
						continue;
					}
					synchronized (os) {
						os.println("Client " + clientId + " says: " + textToSend);
						System.out.println("Sent message to client " + clientToSend1);
					}

					os = myServer.getWriteStream(myServer.getSocket(clientToSend2));
					if (os == null) {
						myServer.getWriteStream(myServer.getSocket(clientId))
								.println("Error: Client " + clientToSend2 + " does not exist");
						continue;
					}
					synchronized (os) {
						os.println("Client " + clientId + " says: " + textToSend);
						System.out.println("Sent message to client " + clientToSend2);
					}
				} else if (Pattern.matches("Server: List All", textReceivedFromClient)) {
					// Server: List All
					Map<Integer, Socket> currentClientsMap = myServer.getMapSocket();
					String list = "Connected clients:\n";
					for (Map.Entry<Integer, Socket> entry : currentClientsMap.entrySet()) {
						list += ("\t" + entry.getKey());
						list += "\n";
					}
					list = list.substring(0, list.length() - 1);
					synchronized (myServer.getWriteStream(myServer.getSocket(clientId))) {
						myServer.getWriteStream(myServer.getSocket(clientId)).println(list);
					}
				} else {
					synchronized (myServer.getWriteStream(myServer.getSocket(clientId))) {
						myServer.getWriteStream(myServer.getSocket(clientId)).println("Command not recognized");
					}
				}

			}
			myServer.removeClient(clientId);
			System.out.println("Client " + clientId + " disconnected");
		} catch (Exception ex) {
			System.out.println("error during communication");
			ex.printStackTrace();
			System.exit(0);
		}
	}

}
import java.net.Socket;
import java.net.ServerSocket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.io.IOException;
import java.util.*;

public class Server {

	private int listeningPort;
	private ServerSocket serverSocket;
	private Map<Integer, Socket> mapSocket;
	private Map<Socket, BufferedReader> readStreamsMap;
	private Map<Socket, PrintWriter> writeStreamsMap;

	public Server(int listeningPort) {
		this.listeningPort = listeningPort;
		mapSocket = new HashMap<Integer, Socket>();
		writeStreamsMap = new HashMap<Socket, PrintWriter>();
		readStreamsMap = new HashMap<Socket, BufferedReader>();
	}

	private void addClient(Socket newClientSocket) {

		try {
			BufferedReader inputStream = new BufferedReader(new InputStreamReader(newClientSocket.getInputStream()));
			PrintWriter outputStream = new PrintWriter(newClientSocket.getOutputStream(), true);

			int newClientId = 1;
			while (mapSocket.containsKey(newClientId))
				newClientId++;

			// update maps
			mapSocket.put(newClientId, newClientSocket);
			readStreamsMap.put(newClientSocket, inputStream);
			writeStreamsMap.put(newClientSocket, outputStream);

			System.out.println("Client " + newClientId + " connected");
			Thread clientThread = new Thread(new ClientServiceThread(newClientId, this));
			clientThread.start();

		} catch (Exception ex) {
			System.out.println("error creating streams in SocketResourcesHandler");
			ex.printStackTrace();
		}

	}

	public void removeClient(int clientIdToRemove) {
		if (mapSocket.get(clientIdToRemove) == null)
			return;
		readStreamsMap.remove(mapSocket.get(clientIdToRemove));
		writeStreamsMap.remove(mapSocket.get(clientIdToRemove));
		mapSocket.remove(clientIdToRemove);
	}

	public Map<Integer, Socket> getMapSocket() {
		return mapSocket;
	}

	public Socket getSocket(int id) {
		return mapSocket.get(id);
	}

	public BufferedReader getReadStream(Socket associatedSocket) {
		return readStreamsMap.get(associatedSocket);
	}

	public PrintWriter getWriteStream(Socket associatedSocket) {
		return writeStreamsMap.get(associatedSocket);
	}

	public void start() {

		try {
			/*
			 * Creates a server socket, bound to the specified port. A port number of 0
			 * means that the port number is automatically allocated, typically from an
			 * ephemeral port range. This port number can then be retrieved by calling
			 * getLocalPort.
			 * 
			 * 
			 * IOException - if an I/O error occurs when opening the socket.
			 * SecurityException - if a security manager exists and its checkListen method
			 * doesn't allow the operation. IllegalArgumentException - if the port parameter
			 * is outside the specified range of valid port values, which is between 0 and
			 * 65535, inclusive
			 */
			serverSocket = new ServerSocket(listeningPort);
			System.out.println("server started successfully!");
			System.out.println("waiting for a connection");

			while (true) {
				/*
				 * This class implements client sockets (also called just "sockets"). A socket
				 * is an endpoint for communication between two machines.
				 * 
				 */
				Socket clientSocket = serverSocket.accept();
				addClient(clientSocket);
			}

		} catch (IOException ex) {
			System.out.println("I/O exception occurred");
			ex.printStackTrace();
			System.exit(0);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}

	}

	public static void main(String[] args) {

		if (args.length != 1) {
			System.out.println("Enter options in the format : Server <Port Number>");
			System.exit(0);
		}

		int pNum = -1;
		try {
			pNum = Integer.parseInt(args[0]);
		} catch (NumberFormatException ex) {
			System.out.println("error - port number should be a valid integer");
			ex.printStackTrace();
			System.exit(0);
		}

		Server myServer = new Server(pNum);
		myServer.start();

	}
}
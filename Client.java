import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.io.IOException;

public class Client {

	private int portNumber;
	private String ipAddress;
	private Socket connectionSocket;
	// streams for IO
	private BufferedReader inputServerStream;
	private PrintWriter outputStream;
	private BufferedReader inputStdStream;

	// thread runnable for reading receiving incoming messages
	public class ReceiveRunnable implements Runnable {
		public void run() {
			System.out.println("Client will start recieving, running recieving thread to get content from Server Stream");
			try {
				String recv_message;
				while ((recv_message = inputServerStream.readLine()) != null) {
					System.out.println(recv_message);
				}
				System.out.println("connection closed, not taking input now");
			} catch (Exception ex) {
				if (Thread.interrupted())
					return;
				ex.printStackTrace();
			}
		}
	}

	public Client(String ipAddress, int portNumber) {
		this.portNumber = portNumber;
		this.ipAddress = ipAddress;
	}

	public void start() {

		try {
			connectionSocket = new Socket(ipAddress, portNumber);
			// allocate stream objects
			inputServerStream = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			outputStream = new PrintWriter(connectionSocket.getOutputStream(), true);

		} catch (UnknownHostException ex) {
			System.out.println("Server with given IP not found");
			ex.printStackTrace();
			System.exit(0);
		} catch (IOException ex) {
			System.out.println("I/O exception occurred");
			ex.printStackTrace();
			System.exit(0);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}

		System.out.println("connection established! type \"quit\" to end session");

		Thread receiveThread = new Thread(new ReceiveRunnable());
		receiveThread.start();

		try {
			// communicate
			inputStdStream = new BufferedReader(new InputStreamReader(System.in));
			String sSend = inputStdStream.readLine();
			while (!sSend.equals("quit")) {
				outputStream.println(sSend);
				sSend = inputStdStream.readLine();
			}
			receiveThread.interrupt();
		} catch (Exception ex) {
			System.out.println("error during communication");
			ex.printStackTrace();
			System.exit(0);
		}

		try {
			// closing resources
			inputStdStream.close();
			outputStream.close();
			inputServerStream.close();
			connectionSocket.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}

	}

	public static void main(String[] args) {

		if (args.length != 2) {
			System.out.println("Enter options in the format : Client <IP Address> <Port Number>");
			System.exit(0);
		}

		int pNum = -1;
		try {
			pNum = Integer.parseInt(args[1]);
		} catch (NumberFormatException ex) {
			System.out.println("error - port number should be a valid integer");
			ex.printStackTrace();
			System.exit(0);
		}

		Client myClient = new Client(args[0], pNum);
		myClient.start();

	}
}
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import javax.swing.*;

public class ChatClient extends JFrame implements ActionListener {
	JTextArea messages = new JTextArea();
	JTextField chatbox = new JTextField();
	PrintWriter out; // Stream for sending messages to the server.
	
	public static void main(String[] args) {
		if (args.length > 0) {
			ChatClient client = new ChatClient(args[0]); // Pass the command line parameter as the username.
		}
		else {
			ChatClient client = new ChatClient("anon");
		}
	}
	
	public ChatClient(String name) {
		final String username = name;
		
		// Populate GUI.
		setTitle("Program 5 - User: " + username);
		setSize(600, 600);
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() { // Send the disconnect message before closing.
			public void windowClosing(WindowEvent e) {
				out.println("disconnect " + username);
				System.exit(0);
			}
		});
		
		configureTextArea();
		
		chatbox.addActionListener(this);
		add(chatbox, BorderLayout.SOUTH);

		setVisible(true);
		
		// Make socket connection.
		try {
			Socket s = new Socket("localhost", 4688); // Establish a new client socket
		    try {
		    	OutputStream outStream = s.getOutputStream();
			    out = new PrintWriter(outStream, true /* autoFlush */);			    
		        out.println("connect " + username); // Send the connect message to the server.
		        Thread listener = new Listen(s); // Thread to listen to the server.
		        listener.start();
		    }
		    finally {
		    }
		}
		catch (IOException e) {
			System.out.println("A connection to the server could not be made.");
		}
	}
	
	private synchronized void configureTextArea() { 
		messages.setEditable(false);
		messages.setLineWrap(true);
		messages.setWrapStyleWord(true);
		add(new JScrollPane(messages), BorderLayout.CENTER);
	}
	
	private class Listen extends Thread { // Inner class for reading from the socket. 
		Socket s;
		
		public Listen(Socket socket) { // Parameter is the socket to listen to.
			s = socket;
		}
		
		public synchronized void run() {
	    	InputStream inStream;
			try {
				inStream = s.getInputStream(); // Get an input stream from the socket.
			    Scanner serverin = new Scanner(inStream); // Create scanner to listen to the server.
			    
			    while (serverin.hasNextLine()) {
			    	String servermessage = serverin.nextLine();
			    	messages.insert(servermessage + "\n", messages.getText().length());			    	
			    }
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void actionPerformed(ActionEvent e) {
		if (e.getSource() == chatbox) {
			String line = chatbox.getText(); // The text entered by the user.
			out.println(line); // Send message to the server.
			messages.insert(line + "\n", messages.getText().length()); // Echo message to the message box. // messages.getText().length()
			chatbox.setText(""); // Clear the chat box.
		}
	}	
}

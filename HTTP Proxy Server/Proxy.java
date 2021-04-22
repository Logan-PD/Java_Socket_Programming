/*
* CPSC 441 Fall 2020
* Assignment 1
*
* Logan Perry-Din
* 30070661
* 
*/

import java.net.*;
import java.io.*;
import java.util.*;

public class Proxy {

    /** Port for the proxy */
    private static int port;
	
	/* Socket for client connections */
    private static ServerSocket socket;

    /* Create the Proxy object and the socket */
    public static void init(int p) {
		
		port = p;
		
		try {
			// Createa a new ServerSocket at with the input port number
			socket = new ServerSocket(port);

		} catch (IOException e) {
			System.out.println("Error creating socket: " + e);
			System.exit(-1);
		}
    }

    public static void handle(Socket client) {

		Socket server = null;
		HttpRequest request = null;
		HttpResponse response = null;

		/* Process request. If there are any exceptions, then simply
		* return and end this request. This unfortunately means the
		* client will hang for a while, until it timeouts. */

		// Create a buffered reader to read input from the client and pass is
		// as a request input for the new HttpRequest
		try {
			BufferedReader fromClient = new BufferedReader(new InputStreamReader(client.getInputStream())); //
			request = new HttpRequest(fromClient); 

		} catch (IOException e) {
			System.out.println("Error reading request from client: " + e);
			return;
		} 


		/* Send request to server */
		try {

			/* Open socket and write request to socket */
			// Open a socket at the HttpRequest's host address and port number
			server = new Socket(request.getHost(), request.getPort());

			// Get the ouput from the request to the server and write it
			DataOutputStream toServer = new DataOutputStream(server.getOutputStream());// use client or server?
			String reqStr = request.toString();
			toServer.write(request.toString().getBytes());
			toServer.flush();

			// Print to terminal for debugging
			System.out.println("\nRequest is:\n" + reqStr + "\n");
			

		} catch (UnknownHostException e) {
			System.out.println("Unknown host: " + request.getHost());
			System.out.println(e);
			return;
		} catch (IOException e) {
			System.out.println("Error writing request to server: " + e);
			return;
		}


		/* Read response and forward it to client */
		try {

			// Get the input from the server, which is the httpResponse
			DataInputStream fromServer = new DataInputStream(server.getInputStream()); 
			response = new HttpResponse(fromServer); 
			
			// Make an OutputStream to write the input from server to client
			DataOutputStream toClient = new DataOutputStream(client.getOutputStream());

			// Print for debugging
			System.out.println("\nResponse is:\n" + response.toString());
			

			//################# Process HTML #######################

			// Write the HttpResponse headers first
			String responseStr = new String(response.toString());
			//toClient.write(responseStr.getBytes());
			//toClient.flush();

			// Process the body:
			// Check what kind of reponse is being sent by looking at "Content-Type" header.
			// If the content is text/html, change according to specifications.
			if (responseStr.contains("Content-Type: text/html")){

				String bodyStr = new String(response.body);

				bodyStr = bodyStr.replace("2019","2219");
				bodyStr = bodyStr.replace("NBA", "TBA");
				bodyStr = bodyStr.replace("World","Titan");
				bodyStr = bodyStr.replace("Drummond", "Kobe-B24");
				
				//Insert inline CSS
				StringBuffer cssBuffer = new StringBuffer(bodyStr);
				cssBuffer.insert(5, " style=\"background-color: blue;font-weight: 900;\"");
				String withCSS = cssBuffer.toString();

				// Write the change body to the client
				toClient.write(withCSS.getBytes());
				toClient.flush();
			
			// Otherwise, the content type is not html so it must be a photo
			// so send the image without changing it.
			} else {

				toClient.write(response.body);
				toClient.flush();
			}
			
			// Close connection
			client.close();
			server.close();

		} catch (IOException e) {
			System.out.println("Error writing response to client: ");
			e.printStackTrace();
		}
    }


    /** Read command line arguments and start proxy */
    public static void main(String args[]) {

		int myPort = 0;
		
		try {
			myPort = Integer.parseInt(args[0]);
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Need port number as argument");
			System.exit(-1);
		} catch (NumberFormatException e) {
			System.out.println("Please give port number as integer.");
			System.exit(-1);
		}
		
		init(myPort);

		/** Main loop. Listen for incoming connections and spawn a new
		 * thread for handling them */
		Socket client = null;
		
		while (true) {
			try {
				// Accept any new connections to the serverSocket
				client = socket.accept(); 
				handle(client);

			} catch (IOException e) {
				System.out.println("Error reading request from client: " + e);
				/* Definitely cannot continue processing this request,
				* so skip to next iteration of while loop. */
				continue;
			}
		}

    }
}

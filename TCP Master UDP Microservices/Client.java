import java.io.*;
import java.net.*;

/*
 * CPSC 441 Fall 2020
 * Assignment 2
 *
 * Logan Perry-Din
 * 30070661
 *
#################################################################################################
## Code used in this class and all other was largely based off of the code written in tutorial. #
## Using examples such as EchoClient and EchoServer.java and DReceiver and DSender.java         #
#################################################################################################
*/

/* 
 * Class: Client
 * connects to master server via specified address and port number
 * then sends input and receives output from Master.
 * Client closes connection when done
*/
public class Client {

    public static void main(String[] args) throws IOException {

        // Set address of master server and portnumber via command line        
        if (args.length != 2) {
            System.err.println("Usage: java Client <host name> <port number>");
            System.exit(1);
        }
        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);


        // Init socket to master server and I/O streams for socket and client terminal
        try (
            //socket to server
            Socket socket = new Socket(hostName, portNumber);

            //output to server
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            //response from server
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //input from command line
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))
        ) {

            // Print instructions
            String lnSep = System.lineSeparator();
            String initialMsg = lnSep + "TCP Connection established with Master." + lnSep + lnSep
                                + "First, enter a sentence to be transformed." + lnSep
                                + "Then write a number such as '1234' to choose transformations"
                                + " 1, then 2, then 3, then 4." + lnSep + lnSep
                                + "1: Echo, 2: Reverse, 3: Upper, 4: Lower, 5: Caesar, 6:Space Out" + lnSep + lnSep
                                + "enter \"close\" to close the connection" + lnSep
                                + "Enter Message:";
            System.out.print(initialMsg);

            /*
             * Main connection loop
             * continously send input to master until client enters "close"
             * read output from master and print to terminal
            */
            String userInput;
            while (!(userInput = stdIn.readLine()).equals("close")) {
                out.println(userInput);
                System.out.println(in.readLine());
            }
            socket.close();

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + hostName);
            System.exit(1);
        } 
    }
}
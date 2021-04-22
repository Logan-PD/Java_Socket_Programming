import java.net.*;
import java.io.*;

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
 *
*/

/*
  * Class: Master Server
  * Used for communicating with client and micro servers separately
  * Master gets client input, sends to micro servers, receives their reply
  * and sends back to client
*/

public class Master {

    static boolean isTimeout = false;
    

    /*
     * SendDatagram: sends a datagram to a specified port, and returns the response datagram.
     * params: 
     *      dgSocket: the datagram Socket to send through
     *      inputString: string to be sent
     *      address: ip address of server to send to
     *      portToSendTo: port of server to send to
     * returns: string sent back from remote server
    */

    public static String sendDatagram(DatagramSocket dgSocket, String inputString, InetAddress address, int portToSendTo){

        int bufSize = 1048576;
        
        try {
            // Set timeout for unresponsive server
            dgSocket.setSoTimeout(5000);

            //buffer to store response from micro
            byte[] buf = new byte[bufSize];

            //Send the packet with the input string
            DatagramPacket request = new DatagramPacket(inputString.getBytes(), inputString.length(), address, portToSendTo);  
            dgSocket.send(request);  

            //receive the packet
            DatagramPacket response = new DatagramPacket(buf, bufSize);
            dgSocket.receive(response);

            //convert byte array to string and return
            String fromMicro = new String(response.getData(),0,response.getLength());    
            System.out.println("msg fromMicro: " + fromMicro);
            return fromMicro;

        // If 5 seconds pass with no response from server, return original input string and set flag
        } catch (SocketTimeoutException ste){
            isTimeout = true;
            return inputString;

        } catch (IOException e) {
            System.out.println("Exception caught while sending/receiving datagramSocket to port" + portToSendTo);
            System.out.println(e.getMessage());
        }

        return "Error - Uncaught Exception";       
    }
    
    /*
     * Main method: runs the master server, connects with client and interacts with micro servers
     * params: port number of master server
     * returns: none
    */
    public static void main(String[] args) throws Exception {
        
        // Line separator used for formatting
        String lnSep = System.lineSeparator();
        String timeoutMsg = "TIMEOUT - exceeded wait time from micro server";

        if (args.length != 1) {
            System.err.println("Usage: java Master <port number>");
            System.exit(1);
        }

        // Set port number and address of master server
        int port = Integer.parseInt(args[0]); //port specified by command line
        InetAddress address = InetAddress.getByName("localhost"); //address is computer that master is running on
        

        /*
         * Init all micro servers
         * creates six micro servers as thread objects with hard-coded port numbers
         * and runs all servers simultaneously
        */

        int echoPort = 1111;
        Echo echo = new Echo(echoPort);
        echo.start();
        
        int revPort = 2222;
        Reverse rev = new Reverse(revPort);
        rev.start();

        int upperPort = 3333;
        Upper upper = new Upper(upperPort);
        upper.start();

        int lowPort = 4444;
        Lower lower = new Lower(lowPort);
        lower.start();

        int caesarPort = 5555;
        Caesar caesar = new Caesar(caesarPort);
        caesar.start();

        int spacePort = 6666;
        Space space = new Space(spacePort);
        space.start();
   

        // Init connection with client
        try (
            //Server socket for accepting incoming connections
            ServerSocket serverSocket = new ServerSocket(port);
            
            //client socket accepted as incoming connection
            Socket clientSocket = serverSocket.accept();

            //output to send to client
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            //input coming from client      
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // socket for udp connection with micro services
            DatagramSocket dgSocket = new DatagramSocket();
        ) {
            
            //Initialization message for master terminal
            String endInitMsg = lnSep + "Master server and mirco servers running..." + lnSep 
                                    + "Connection with client established" + lnSep + lnSep
                                    + "all micro servers running on same address as master server" + lnSep + lnSep;
            System.out.print(endInitMsg);

            
            // Store input string to be transformed
            String inMsg = in.readLine();
            System.out.println(inMsg);

            // Next instruction for client
            out.println("Enter your integer of transformations");


            // Continuously read transformation numbers untill client sends "close"
            String transNums;
            while ((transNums = in.readLine()) != null) {
                
                System.out.println("input numbers: " + transNums); //master terminal

                if (transNums.equals("close")) break;
                
                //input has format of "4123532"
                // For every character sent in the message, send to the appropriate micro server, specified by port number 
                for (int i=0; i<transNums.length(); i++){
                    char c = transNums.charAt(i);

                    System.out.println("char read: " + c);
                    switch(c){
                        case '1':
                            inMsg = sendDatagram(dgSocket, inMsg, address, echoPort);
                            break;
                        case '2':
                            inMsg = sendDatagram(dgSocket, inMsg, address, revPort);
                            break;
                        case '3':
                            inMsg = sendDatagram(dgSocket, inMsg, address, upperPort);
                            break;
                        case '4':
                            inMsg = sendDatagram(dgSocket, inMsg, address, lowPort);
                            break;
                        case '5':
                            inMsg = sendDatagram(dgSocket, inMsg, address, caesarPort);
                            break;
                        case '6':
                            inMsg = sendDatagram(dgSocket, inMsg, address, spacePort);
                            break;

                    }
                }

                //If timeout from micro server occurs, notify client, keep unchanged string
                if (isTimeout){
                    out.println("TIMEOUT - exceeded wait time from micro server. Unchanged message: " + inMsg);
                    isTimeout = false;
                }else{
                    out.println(inMsg);
                } 
            }

            serverSocket.close();
            System.exit(1);

        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port " + port + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
}

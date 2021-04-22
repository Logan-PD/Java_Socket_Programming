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
*/

/*
 * Class: Space
 * Micro server that inserts a space between every character
 * extends Thread so that it can be run simultaneously from Master server
*/
public class Space extends Thread{

  int port;
  int bufSize = 1048576;

  public Space (int port){
    this.port = port;
 }

  public void run() {

    try (
        //socket to send and receive
        DatagramSocket socket = new DatagramSocket(port);
    ) {

        //buffer to store request
        byte[] buf = new byte[bufSize];  

        while (true){

          //packet received, sent by master
          DatagramPacket request = new DatagramPacket(buf, bufSize);  
          socket.receive(request);

          //get address and port of request, to send back to
          InetAddress clientAddress = request.getAddress();
          int clientPort = request.getPort();
            
          String reqStr = new String(request.getData(),0,request.getLength());

          //replaces all empty substrings with a space, then trims the outside spaces
          String result = reqStr.replace(""," ").trim();

          //packet to be sent to master
          DatagramPacket response = new DatagramPacket(result.getBytes(), result.length(), clientAddress, clientPort);
          socket.send(response);
        }
    } catch (IOException e){
      System.out.println("Exception caught when trying to listen on port " + port + " or listening for a connection");
      System.out.println(e.getMessage());
    }
  }  
}  

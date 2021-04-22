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
 * Class: Upper
 * Micro server that converts input to all uppercase
 * extends Thread so that it can be run simultaneously from Master server
*/
public class Upper extends Thread{

  int port;
  int bufSize = 1048576;

  public Upper (int port){
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

            //get address and port of request
            InetAddress clientAddress = request.getAddress();
            int clientPort = request.getPort(); //should be same as socket port

            //Get input string and convert to upper case
            String reqStr = new String(request.getData(),0,request.getLength());
            reqStr = reqStr.toUpperCase();

            //packet to be sent to master
            DatagramPacket response = new DatagramPacket(reqStr.getBytes(), reqStr.length(), clientAddress, clientPort);
            socket.send(response);
        }
    } catch (IOException e){
        System.out.println("Exception caught when trying to listen on port " + port + " or listening for a connection");
            System.out.println(e.getMessage());
    }
  }  
}  

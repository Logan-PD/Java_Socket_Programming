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
 * Class: Caesar
 * Micro server that performs aa Caesar cipher on input by shift of 2
 * extends Thread so that it can be run simultaneously from Master server
*/
public class Caesar extends Thread{

  int port;
  int bufSize = 1048576;

  public Caesar (int port){
    this.port = port;
 }

  public void run() {

    try (
        //socket to send and receive
        //use port specified in Master
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
          
          // Get input string
          String reqStr = new String(request.getData(),0,request.getLength());
          StringBuffer shifted = new StringBuffer();

          int shift = 2;

          // Perform shift
          /*
           * Code for the Caesar cipher was developed using the following free ressources:
           * https://www.baeldung.com/java-caesar-cipher
           * https://stackoverflow.com/questions/19108737/java-how-to-implement-a-shift-cipher-caesar-cipher
           * https://www.geeksforgeeks.org/caesar-cipher-in-cryptography/
          */
          for (int i=0; i<reqStr.length(); i++){
            char c = reqStr.charAt(i);

            if (Character.isLetter(c)) {

              if (Character.isUpperCase(c)){
                c = (char) (((int) c + shift - 65) % 26 + 65); 
              } else {
                c = (char) (((int) c + shift - 97) % 26 + 97);
              }
              shifted.append(c);
            } else {
              shifted.append(c);
            }
          }

          String result = shifted.toString();

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

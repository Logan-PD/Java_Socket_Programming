/*
 * Logan Perry-Din
 * 30070661
 *
 * CPSC 441 Fall 2020
 * Assignment 3; Part 1
 *
*/

import java.io.*;
import java.net.*;
import java.util.*;


/*
 * PingClient: Sends 10 ping messages to the ping server using UDP datagrams, 
 *              calculates RTT of each packet sent, then the average, minimum 
 *              and maximum delay of all 10 packets.
*/
public class PingClient 
{

    private static final int BUFFSIZE = 128;
    private static final String CRLF = "\r\n";


    // main function: expects two command line arguments
    //      address of pingserver and port number
    public static void main(String[] args) throws Exception 
    {
        // usage
        if (args.length != 2) 
        {
            System.out.println("Usage: java PingClient <host> <port>");
            return;
        }
    
        // get address and port of ping server from command line
        InetAddress ip = InetAddress.getByName(args[0]);
        int port = Integer.parseInt(args[1]);

        // create a socket for sending and receiving
        DatagramSocket socket = new DatagramSocket();

        // set timeout for receiving packets to one second
        socket.setSoTimeout(1000);

        // init min to highest possible, max to lowest possible and average to 0
        long min_delay = 1000;
        long max_delay = 0;
        long avg_delay = 0;

        // send 10 pings
        for (int seq_num = 0; seq_num < 10; seq_num++)
        {
            //current time in seconds
            long send_time = System.currentTimeMillis();

            // Send string with sequence number and current time
            String testStr = "PING " + seq_num + " " + send_time + CRLF;
            DatagramPacket ping = new DatagramPacket(testStr.getBytes(), testStr.length(), ip, port);
            socket.send(ping);
            
            // try to receive before timeout
            try 
            {
                // receive the packet
                DatagramPacket pong = new DatagramPacket(new byte[BUFFSIZE],BUFFSIZE);
                socket.receive(pong);

                //printData
                byte[] buf = pong.getData();
                String line = new String(buf);

                
                //compare time sent, vs time received. difference is RTT
                long receive_time = System.currentTimeMillis();
                long delay = receive_time - send_time;

                // update min, max and average
                if (delay < min_delay) min_delay = delay;
                if (delay > max_delay) max_delay = delay;
                avg_delay += delay;
                
                // send one packet every second
                Thread.sleep(1000);

                //print out RTT for this packet
                String out_str = "RTT: " + delay + " ms: " + line;
                System.out.println(out_str);

            } 
            // catch if packet took more than 1 second to receive
            catch (SocketTimeoutException ste)
            {
                // update max and avg
                max_delay = 1000;
                avg_delay += 1000;

                // print out that loss happened
                System.out.println("Packet loss\n");
            }
        }

        socket.close();

        // calculate average delay and print results
        avg_delay = avg_delay / 10;
        System.out.println("RTT: min delay: " + min_delay + " ms / max delay: " + max_delay + " ms / average delay: " + avg_delay + " ms");
    }
}
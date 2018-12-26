package skj.pro2.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Client
{
    //TODO: try exceptions, wrong server adress
    public static void main(String[] args) throws IOException {
        DatagramSocket ds = new DatagramSocket();

        // DatagramPacket dpToSend = new DatagramPacket(packetByte, packetByte.length, InetAddress.getByName(serverAddress), serverPort);

        String serverAddress = args[0];
        for(int i = 1; i < args.length; i++)
        {
            byte[] packet = ("Package #" + i).getBytes();
            DatagramPacket dpToSend = new DatagramPacket(packet, packet.length, InetAddress.getByName(serverAddress), Integer.parseInt(args[i]));
            ds.send(dpToSend);
            System.out.println("package sent: "+i);
        }

    }
}

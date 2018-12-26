package skj.pro2.server;//otwieramy zadana parametrem ilosc portow UDP i numery tych portow

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class Server {

    public static void main(String[] args) throws IOException {

        InetAddress adressSerweraDNS = InetAddress.getByName("localhost");
        DatagramSocket ds = new DatagramSocket(5011, adressSerweraDNS);

        while(true)
        {
            DatagramPacket dpToRecv = new DatagramPacket(new byte[1460], 1460);
            ds.receive(dpToRecv);
            System.out.println("received");
            String string = new String(dpToRecv.getData());
            System.out.println(string);
        }
    }


}

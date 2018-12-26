package skj.pro2.server;//otwieramy zadana parametrem ilosc portow UDP i numery tych portow

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class Server {

    private static final long SEQUENCE_TIMEOUT = 5000;

    private InetAddress serverAddress;
    private int[] validPortSeq;
    private Map<InetAddress, Boolean[]> candidates;
    private Map<InetAddress, Long> lastCheck;

    class UDPListener implements Runnable{
        private int port;

        @Override
        public void run() {
            try {
                DatagramSocket socket = new DatagramSocket(port, serverAddress);
                DatagramPacket packet = new DatagramPacket(new byte[1460], 1460);

                socket.receive(packet);
                String received = new String(packet.getData());
                System.out.println("RECV: "+received);

                String[] split = received.trim().split(":");
                int seqNo = Integer.parseInt(split[1]);

                synchronized(this)
                {
                    Boolean[] array;
                    System.out.println("Connecting IP: " + packet.getAddress());
                    if(candidates.containsKey(packet.getAddress()))
                    {
                        array = candidates.get(packet.getAddress());
                    } else array = new Boolean[validPortSeq.length];

                    for(int i = 0; i < validPortSeq.length; i++)
                    {
                        if(validPortSeq[i] == port) {
                            array[i] = seqNo == i;
                            System.out.println("Port " + port + " approved? :" + array[i]);
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        UDPListener(int port){
            this.port = port;
        }
    }

    private Server() throws UnknownHostException {
        serverAddress = InetAddress.getByName("localhost");
        candidates = new HashMap<>();
        lastCheck = new HashMap<>();
    }

    private void run(String[] args) {
        validPortSeq = new int[args.length];

        for(int i = 0; i < args.length; i++) {
            validPortSeq[i] = Integer.parseInt(args[i]);
            new Thread(new UDPListener(validPortSeq[i])).start();
        }

        System.out.println("Server started");
        //Thread for validating
        while(true)
        {
            synchronized(this)
            {
                for(Map.Entry<InetAddress, Boolean[]> entry : candidates.entrySet())
                {
                    Boolean[] accessGranted = entry.getValue();
                    boolean everyoneGrantedAccess = true;
                    boolean everyoneResponded = true;

                    for (int i = 0; i < entry.getValue().length; i++)
                    {
                        if(accessGranted[i] == null)
                            everyoneResponded = false;
                        else if(!accessGranted[i])
                        {
                            everyoneGrantedAccess = false;
                            break;
                        }
                    }

                    if (everyoneGrantedAccess) {
                        //START TCP
                        System.out.println("Everyone granted the access! starting tcp");
                    }
                    else
                    {
                        if (everyoneResponded || System.currentTimeMillis() - lastCheck.get(entry.getKey()) > SEQUENCE_TIMEOUT) {
                            candidates.remove(entry.getKey());
                            lastCheck.remove(entry.getKey());
                            System.out.println(entry.getKey() + "timeout...");
                        }
                        else lastCheck.replace(entry.getKey(), System.currentTimeMillis());
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new Server().run(args);
    }
}

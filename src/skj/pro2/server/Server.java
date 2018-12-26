package skj.pro2.server;//otwieramy zadana parametrem ilosc portow UDP i numery tych portow

import java.io.IOException;
import java.lang.reflect.Parameter;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server {

    private static final long SEQUENCE_TIMEOUT = 5000;

    private InetAddress serverAddress;
    private int[] validPortSeq;
    private Map<InetAddress, Boolean[]> candidates;
    private Map<InetAddress, Long> lastCheck;
    private final Server serverInstance;

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

                synchronized(serverInstance)
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

                    if(candidates.replace(packet.getAddress(), array) == null)
                        candidates.put(packet.getAddress(), array);
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
        serverInstance = this;
    }

    private void run(String[] args) throws InterruptedException {
        validPortSeq = new int[args.length];

        for(int i = 0; i < args.length; i++) { //TODO: synchronized?
            validPortSeq[i] = Integer.parseInt(args[i]);
            new Thread(new UDPListener(validPortSeq[i])).start();
        }

        System.out.println("Server started");

        //Thread for validating
        List<InetAddress> toRemove = new ArrayList<>();

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

                    if (everyoneResponded && everyoneGrantedAccess) {
                        //START TCP
                        System.out.println("Everyone granted the access! starting tcp");
                        toRemove.add(entry.getKey());
                    }
                    else
                    {
                        //remove monitor if 1.timeout or 2.everyone has responded
                        if(everyoneResponded || (lastCheck.containsKey(entry.getKey()) && System.currentTimeMillis() - lastCheck.get(entry.getKey()) > SEQUENCE_TIMEOUT)) {
                            toRemove.add(entry.getKey());
                            System.out.println(entry.getKey() + ": timeout...");
                        }
                        else if(!lastCheck.containsKey(entry.getKey()))
                            lastCheck.put(entry.getKey(), System.currentTimeMillis());
                    }
                }

                if(toRemove.size() > 0) {
                    lastCheck.keySet().removeAll(toRemove);
                    candidates.keySet().removeAll(toRemove);
                    toRemove.clear();
                }

                Thread.sleep(100);
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        new Server().run(args);
    }
}

package skj.pro2.server;//otwieramy zadana parametrem ilosc portow UDP i numery tych portow

import java.io.*;
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
    private Map<SocketAddress, Boolean[]> candidates;
    private Map<SocketAddress, Long> lastCheck;
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
                    System.out.println("Connecting IP: " + packet.getSocketAddress());

                    if(candidates.containsKey(packet.getSocketAddress()))
                    {
                        array = candidates.get(packet.getSocketAddress());
                    } else array = new Boolean[validPortSeq.length];

                    for(int i = 0; i < validPortSeq.length; i++)
                    {
                        if(validPortSeq[i] == port) {
                            array[i] = seqNo == i;
                            System.out.println("Port " + port + " approved? :" + array[i]);
                        }
                    }

                    if(candidates.replace(packet.getSocketAddress(), array) == null)
                        candidates.put(packet.getSocketAddress(), array);
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

    private void run(String[] args) throws InterruptedException, IOException {
        validPortSeq = new int[args.length];

        for(int i = 0; i < args.length; i++) { //TODO: synchronized?
            validPortSeq[i] = Integer.parseInt(args[i]);
            new Thread(new UDPListener(validPortSeq[i])).start();
        }

        System.out.println("Server started");

        //Thread for validating
        List<SocketAddress> toRemove = new ArrayList<>();

        while(true)
        {
            synchronized(this)
            {
                for(Map.Entry<SocketAddress, Boolean[]> entry : candidates.entrySet())
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

                        System.out.println("Everyone granted the access! starting tcp");
                        toRemove.add(entry.getKey());
                        startTCPCommunication(entry.getKey());
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

    private void startTCPCommunication(SocketAddress host) {

        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(0); //get default free port
                byte[] packet = ("WELCOME:" + serverSocket.getLocalPort()).getBytes();
                DatagramSocket ds = new DatagramSocket();
                DatagramPacket dp = new DatagramPacket(packet, packet.length, host);

                ds.send(dp);
                ds.close();

                Socket socket = serverSocket.accept();
                System.out.println("Client TCP connected! " + socket.getInetAddress());
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                writeLine(bw, "WELCOME");

                String response = br.readLine();

                writeLine(bw, "G3G5EGH26166SHH5525");

                response = br.readLine();


                System.out.println("Closing connection!");

                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    void writeLine(BufferedWriter bw, String msg) throws IOException {
        bw.write(msg);
        bw.newLine();
        bw.flush();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        new Server().run(args);
    }
}

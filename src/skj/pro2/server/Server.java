package skj.pro2.server;

import java.io.*;
import java.lang.reflect.Parameter;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                log("Start listening on " + port + " UDP port");

                while(true)
                {
                    DatagramPacket packet = new DatagramPacket(new byte[1460], 1460);

                    socket.receive(packet);
                    String received = new String(packet.getData());
                    log("RECEIVED MESSAGE [" + received + "] from [" + packet.getSocketAddress() + "] on port ["+port+"]");

                    Pattern pattern = Pattern.compile("(\\s*AUTH_REQ:(\\d+)\\s*)");
                    Matcher match = pattern.matcher(received);
                    if (!match.find()) {
                        log("Received message has wrong syntax... aborting");
                        continue;
                    }


                    int seqNo = Integer.parseInt(match.group(2));

                    synchronized (serverInstance)
                    {
                        Boolean[] permitArray;

                        //We have our client in memory already (he connected to other ports before)
                        if (candidates.containsKey(packet.getSocketAddress()))
                            permitArray = candidates.get(packet.getSocketAddress());
                        else
                            permitArray = new Boolean[validPortSeq.length];

                        //Now, we are checking is received sequence in message is corresponding to valid seq
                        for (int i = 0; i < validPortSeq.length; i++) {
                            if (validPortSeq[i] == port) {
                                permitArray[i] = seqNo == i;
                                System.out.println("UDP Port [" + port + "] has approved the client's seq?:" + permitArray[i]);
                            }
                        }

                        //update (or put) new permitArray
                        if (candidates.replace(packet.getSocketAddress(), permitArray) == null)
                            candidates.put(packet.getSocketAddress(), permitArray);
                    }
                }
            } catch (Exception e) {
                System.err.println(e);
                System.exit(1);
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

        log("Starting the server");

        synchronized (serverInstance) {
            validPortSeq = new int[args.length];

            for (int i = 0; i < args.length; i++) {
                validPortSeq[i] = Integer.parseInt(args[i]);
                new Thread(new UDPListener(validPortSeq[i])).start();
            }
        }

        List<SocketAddress> toRemove = new ArrayList<>();
        while(true)
        {
            synchronized (serverInstance)
            {
                for (Map.Entry<SocketAddress, Boolean[]> entry : candidates.entrySet()) {
                    Boolean[] accessGranted = entry.getValue();
                    boolean everyoneGrantedAccess = true;
                    boolean everyoneResponded = true;

                    for (int i = 0; i < entry.getValue().length; i++) {
                        if (accessGranted[i] == null)
                            everyoneResponded = false;
                        else if (!accessGranted[i]) {
                            everyoneGrantedAccess = false;
                            break;
                        }
                    }

                    if (everyoneResponded && everyoneGrantedAccess) {

                        log("Granting the access for [" + entry.getKey() + "]");
                        toRemove.add(entry.getKey());
                        startTCPCommunication((InetSocketAddress) entry.getKey());
                    } else {
                        //remove monitor if 1.timeout or 2.everyone has responded
                        if (everyoneResponded || (lastCheck.containsKey(entry.getKey()) && System.currentTimeMillis() - lastCheck.get(entry.getKey()) > SEQUENCE_TIMEOUT)) {
                            toRemove.add(entry.getKey());
                            log("Client [" + entry.getKey() + "] is timeout..");
                        } else if (!lastCheck.containsKey(entry.getKey()))
                            lastCheck.put(entry.getKey(), System.currentTimeMillis());
                    }
                }

                if (toRemove.size() > 0) {
                    lastCheck.keySet().removeAll(toRemove);
                    candidates.keySet().removeAll(toRemove);
                    toRemove.clear();
                }
            }
        }
    }


    private void startTCPCommunication(InetSocketAddress host) {

        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(0); //get default free port
                log("Starting TCP socket on port: " + serverSocket.getLocalPort());
                byte[] packet = ("PORT:" + serverSocket.getLocalPort()).getBytes();

                //Sending UDP with TCP port
                DatagramSocket ds = new DatagramSocket();
                DatagramPacket dp = new DatagramPacket(packet, packet.length, host);

                log("TCP port has been sent by UDP packet");

                ds.send(dp);
                ds.close();

                Socket socket = serverSocket.accept();

                //TODO: timeout
                while(!socket.getInetAddress().equals(host.getAddress()))
                {
                    log("Unpermitted client tried to connect on TCP port! " +
                            "Closing connection and waiting for valid client");
                    socket.close();
                    socket = serverSocket.accept();
                }

                log("Client ["+socket.getInetAddress()+"] has established TCP connection");
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                writeLine(bw, "WELCOME");
                if(br.readLine().equals("AUTH_REQ"))
                {
                    log("Sending authorization key");
                    writeLine(bw, "G3G5EGH26166SHH5525");

                    if(br.readLine().equals("OK"))
                        log("Client received key successfully");
                    else
                        log("Client notified issue while receiving key");
                }

                log("Closing TCP connection");
                socket.close();

            } catch (IOException e) {
                System.err.println("An error occurred while tried to communicate with remote client");
                System.err.println(e);
            }
        }).start();
    }

    private void log(String message) {
        System.out.println(message);
    }

    private void writeLine(BufferedWriter bw, String msg) throws IOException {
        bw.write(msg);
        bw.newLine();
        bw.flush();
    }

    //Check args syntax
    public static void main(String[] args) throws IOException, InterruptedException {
        new Server().run(args);
    }
}

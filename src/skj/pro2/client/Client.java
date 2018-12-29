package skj.pro2.client;

import java.io.*;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client
{
    private InetAddress serverAddress;
    private DatagramSocket datagramSocket;
    private static final int TIMEOUT = 10000; //in ms

    public class TCPListener implements Runnable {

        @Override
        public void run() {
            try {
                DatagramPacket packet = new DatagramPacket(new byte[1460], 1460);
                datagramSocket.receive(packet);

                String received = new String(packet.getData());
                System.out.println("I have received message with port! msg:["+received+"]");
                datagramSocket.close();

                Pattern pattern = Pattern.compile("(\\s*PORT:(\\d+)\\s*)");
                Matcher match = pattern.matcher(received);
                if (!match.find()) {
                    log("Received message has wrong syntax... aborting");
                    return;
                }

                int TCPPort = Integer.parseInt(match.group(2));

                log("Trying to establish TCP connection with given port: ["+TCPPort+"]");
                Socket socket = new Socket(serverAddress, TCPPort);
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                log("Connection established!");
                String request = br.readLine(); //wait for welcome

                log("Incoming message: " + request);
                log("Requesting auth key");
                writeLine(bw, "AUTH_REQ");

                request = br.readLine();
                log("Received auth key! :[" + request+"]");

                writeLine(bw, "OK");
                socket.close();
                log("Connection closed");
                System.exit(0);

            } catch (SocketException e) {
                System.err.println("Did not received back message from server!\n" +
                        "(probably bad UDP sequence or invalid ports..)");
            }
            catch (Exception e) {
                System.err.println(e);
            }
        }
    }

    void writeLine(BufferedWriter bw, String msg) throws IOException {
        bw.write(msg);
        bw.newLine();
        bw.flush();
    }

    private void run(String[] args) throws IOException, InterruptedException {
        datagramSocket = new DatagramSocket();
        log("Start knocking");

        String serverIP = args[0];
        serverAddress = InetAddress.getByName(serverIP);
        for(int i = 1; i < args.length; i++)
        {
            String message = "AUTH_REQ:" + (i-1);
            byte[] packet = message.getBytes();
            DatagramPacket dpToSend = new DatagramPacket(packet, packet.length, serverAddress, Integer.parseInt(args[i]));
            datagramSocket.send(dpToSend);
            log("Message ["+message+"] has been sent on UDP port ["+args[i]+"]");
        }

        log("Knocking finished - waiting for UDP message");

        //Wait for response for tcp port info
        new Thread(new TCPListener()).start();
        Thread.sleep(TIMEOUT);
        datagramSocket.close();
    }

    private void log(String message) {
        System.out.println(message);
    }

    public static void main(String[] args)
    {
        if(args.length < 2 || !validateIP(args[0]))
        {
            System.err.println("Wrong args syntax... aborting");
            return;
        }

        try {
            new Client().run(args);
        } catch(Exception e) {
            System.err.println("An error has occurred...");
            System.err.println(e);
        }

    }

    //https://stackoverflow.com/questions/5667371/validate-ipv4-address-in-java
    private static final Pattern PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public static boolean validateIP(final String ip) {
        return PATTERN.matcher(ip).matches();
    }
}

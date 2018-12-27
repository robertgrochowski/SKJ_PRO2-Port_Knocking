package skj.pro2.client;

import java.io.*;
import java.net.*;

public class Client
{
    private InetAddress serverAddress;
    private DatagramSocket ds;

    public class TCPListener implements Runnable {

        @Override
        public void run() {
            try {
                DatagramPacket packet = new DatagramPacket(new byte[1460], 1460);
                ds.receive(packet);

                String received = new String(packet.getData());
                System.out.println("Client RECV: "+received);
                ds.close();

                String[] split = received.trim().split(":");
                int TCPPort = Integer.parseInt(split[1]);

                Socket socket = new Socket(serverAddress, TCPPort);
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                String request = br.readLine(); //wait for welcome
                System.out.println("Read TCP: " + request);

                writeLine(bw, "AUTH_REQ");

                request = br.readLine();
                System.out.println("Auth key: " + request);

                writeLine(bw, "OK");
                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void writeLine(BufferedWriter bw, String msg) throws IOException {
        bw.write(msg);
        bw.newLine();
        bw.flush();
    }

    private void run(String[] args) throws IOException, InterruptedException {
        //wysylanie na port ktory jest ostatni w sekwencji

        ds = new DatagramSocket();

        String serverIP = args[0];
        serverAddress = InetAddress.getByName(serverIP);
        for(int i = 1; i < args.length; i++)
        {
            byte[] packet = ("SEQ_REQ:" + (i-1)).getBytes();
            DatagramPacket dpToSend = new DatagramPacket(packet, packet.length, serverAddress, Integer.parseInt(args[i]));
            ds.send(dpToSend);
            System.out.println("package sent: "+i);
        }

        //Wait for response for tcp port info

        new Thread(new TCPListener()).start();
        Thread.sleep(5000);
        ds.close();

    }

    //TODO: try exceptions, wrong server adress, no ports, bad syntax, no last port?
    public static void main(String[] args) throws IOException, InterruptedException {
        Thread.sleep(3000); //TODO
        new Client().run(args);
    }
}

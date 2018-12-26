package skj.pro2.client;

import java.io.*;
import java.net.*;

public class Client
{
    private ServerSocket serverSocket;
    private boolean hasConnection;

    public class TCPListener implements Runnable {
        @Override
        public void run() {
            try {
                Socket socket = serverSocket.accept();
                hasConnection = true;

                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                String request = br.readLine();
                System.out.println("Read TCP: " + request);

                serverSocket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Client(){
        hasConnection = false;
    }

    private void run(String[] args) throws IOException, InterruptedException {

        //Opening serverSocket, assuming that last given port is our TCP listener port as well
        int TCPPort = Integer.parseInt(args[args.length-1]);
        serverSocket = new ServerSocket(TCPPort);
        TCPListener listener = new TCPListener();
        new Thread(listener).start();

        DatagramSocket ds = new DatagramSocket();

        String serverAddress = args[0];
        for(int i = 1; i < args.length; i++)
        {
            byte[] packet = ("SEQ_REQ:" + (i-1)).getBytes();
            DatagramPacket dpToSend = new DatagramPacket(packet, packet.length, InetAddress.getByName(serverAddress), Integer.parseInt(args[i]));
            ds.send(dpToSend);
            System.out.println("package sent: "+i);
        }

        //Wait for conection
        Thread.sleep(5000);
        if(!hasConnection)
            serverSocket.close();
    }

    //TODO: try exceptions, wrong server adress, no ports, bad syntax, no last port?
    public static void main(String[] args) throws IOException, InterruptedException {
        Thread.sleep(3000); //TODO
        new Client().run(args);
    }
}

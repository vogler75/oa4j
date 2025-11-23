/*
    OA4J - WinCC Open Architecture for Java
    Copyright (C) 2017 Andreas Vogler

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
import at.rocworks.oa4j.base.*;
import at.rocworks.oa4j.var.DynVar;
import at.rocworks.oa4j.var.TextVar;
import at.rocworks.oa4j.jni.ErrCode;
import at.rocworks.oa4j.jni.ErrPrio;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.logging.Level;

/**
 *
 * @author vogler
 */
public class ApiBroadcast {
    static int PORT = 8080;
    static String DP_REQUEST = "";
    static String DP_RESPONSE = "";

    public static void main(String[] args) throws Exception {
        JManager m = new JManager();
        m.init(args).start();
        ApiBroadcast prog = new ApiBroadcast();

        for (String arg: args) {
            String[] kv = arg.split("=");
            if (kv.length>1) {
                switch ( kv[0]) {
                    case "port":
                        PORT=Integer.parseInt(kv[1]);
                        break;
                    case "dp":
                        DP_REQUEST=kv[1]+".Scan";
                        DP_RESPONSE=kv[1]+".Result";
                        break;
                }
            }
        }
        JDebug.out.info("Broadcast PORT="+ PORT +" RequestDp="+DP_REQUEST+" ResponseDp="+DP_RESPONSE);

        BroadcastClient client = new BroadcastClient();
        client.init();
        client.start(); // start thread

        BroadcastServer server = new BroadcastServer();
        server.init();
        server.start(); // start thread

        prog.hook();
        client.done();
        server.done();
        m.stop();
    }

    public void hook() throws InterruptedException {
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            synchronized ( ApiBroadcast.this ) { ApiBroadcast.this.notifyAll(); }
        }));
        synchronized ( ApiBroadcast.this ) { ApiBroadcast.this.wait(); }
        JDebug.out.info("Shutdown");
    }

    public static class BroadcastClient extends Thread {

        DatagramSocket clientSocket;
        volatile DynVar result = new DynVar();

        public void init() {
            // open a PORT to send the package
            try {
                clientSocket = new DatagramSocket();
                clientSocket.setBroadcast(true);
                clientSocket.setSoTimeout(1);
            } catch (SocketException e) {
                e.printStackTrace();
            }

            if (!DP_REQUEST.isEmpty()) {
                // winccoa connect
                JDpConnect conn = JClient.dpConnect()
                        .add(DP_REQUEST)
                        .action((JDpHLGroup hotlink) -> {
                            String val = hotlink.getItemVar(0).toString();
                            result = new DynVar();
                            request(); // send broadcast
                        })
                        .connect();
            }
        }

        public void done() {
            clientSocket.close();
        }

        @Override
        public void run() {
            while (!clientSocket.isClosed()) {
                try {
                    //Wait for a response
                    byte[] recvBuf = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                    clientSocket.receive(receivePacket);

                    //We have a response
                    String host = receivePacket.getAddress().getHostAddress().toString();
                    JDebug.out.info("Broadcast response from server: " + host);

                    //Check if the message is correct
                    String message = new String(receivePacket.getData()).trim();
                    if (message.equals("DISCOVER_RESPONSE")) {
                        result.add(new TextVar(host));
                        if (!DP_RESPONSE.isEmpty())
                            JClient.dpSet(DP_RESPONSE, result);
                    }
                } catch (SocketTimeoutException ex) {
                    // nothing
                } catch (SocketException ex) {
                    // serverSocket closed
                    System.out.println(ex.getMessage());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        public void request() {
            JDebug.out.info("Request...");

            // Find the server using UDP broadcast
            try {
                byte[] sendData = "DISCOVER_REQUEST".getBytes();

                //Try the 255.255.255.255 first
//                try {
//                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), PORT);
//                    clientSocket.send(sendPacket);
//                    JDebug.out.info(">>> Request packet sent to: 255.255.255.255 (DEFAULT):"+PORT);
//                } catch (Exception ex) {
//                    JManager.stackTrace(ErrPrio.PRIO_WARNING, ErrCode.UNEXPECTEDSTATE, ex);
//                }

                // Broadcast the message over all the network interfaces
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = interfaces.nextElement();

                    if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                        continue; // Don't want to broadcast to the loopback interface
                    }

                    for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                        InetAddress broadcast = interfaceAddress.getBroadcast();
                        if (broadcast == null) {
                            continue;
                        }

                        // Send the broadcast package!
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, PORT);
                        clientSocket.send(sendPacket);

                        JDebug.out.info("Request packet sent to: " + broadcast.getHostAddress() + ":"+ PORT +"; Interface: " + networkInterface.getDisplayName());
                    }
                }
                JDebug.out.info("Done looping over all network interfaces.");

            } catch (IOException ex) {
                JManager.stackTrace(ErrPrio.PRIO_WARNING, ErrCode.UNEXPECTEDSTATE, ex);
            }
        }
    }

    public static class BroadcastServer extends Thread {

        DatagramSocket serverSocket;

        public void init() throws UnknownHostException {
            //Open a random PORT to send the package
            try {
                JDebug.out.info("listen on PORT "+ PORT);
                serverSocket = new DatagramSocket(PORT, InetAddress.getByName("0.0.0.0"));
                serverSocket.setBroadcast(true);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }

        public void done() {
            serverSocket.close();
        }

        @Override
        public void run() {
            try {
                //Keep a serverSocket open to listen to all the UDP traffic that is destined for this PORT
                while (true) {
                    JDebug.out.info("Ready to receive broadcast packets.");

                    //Receive a packet
                    byte[] recvBuf = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                    serverSocket.receive(packet);

                    //Packet received
                    JDebug.out.info("Discovery packet received from: " + packet.getAddress().getHostAddress());
                    //JDebug.out.info(">>>Packet received; data: " + new String(packet.getData()));

                    //See if the packet holds the right command (message)
                    String message = new String(packet.getData()).trim();
                    if (message.equals("DISCOVER_REQUEST")) {
                        byte[] sendData = "DISCOVER_RESPONSE".getBytes();

                        //Send a response
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
                        serverSocket.send(sendPacket);

                        JDebug.out.info("Sent packet to: " + sendPacket.getAddress().getHostAddress());
                    }
                }
            } catch (IOException ex) {
                JManager.stackTrace(ErrPrio.PRIO_SEVERE, ErrCode.UNEXPECTEDSTATE, ex);
            }
        }
    }
}

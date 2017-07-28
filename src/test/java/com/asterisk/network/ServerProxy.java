package com.asterisk.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author donghao
 * @version 1.0
 *          2017/7/26.
 */
public class ServerProxy {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8888);
        while (true) {
            Socket socket = serverSocket.accept();
            new SocketThread(socket).start();
        }
    }

    private static class SocketThread extends Thread {

        private Socket socketIn;

        private InputStream isIn;

        private OutputStream osIn;

        private InputStream isOut;

        private OutputStream osOut;

        private Socket socketOut;

        private byte[] buffer = new byte[4096];

        private static final byte[] VER = {0x5, 0x0};

        private static final byte[] CONNECT_OK = {0x5, 0x0, 0x0, 0x1, 0, 0, 0, 0, 0, 0};

        public SocketThread(Socket socket) {
            this.socketIn = socket;
        }

        @Override
        public void run() {
            try {
                System.out.println("client connect " + socketIn.getInetAddress() + " :" + socketIn.getPort());
                isIn = socketIn.getInputStream();
                osIn = socketIn.getOutputStream();
                int len = isIn.read(buffer);
                System.out.println("<" + bytesToHexString(buffer, 0, len));
                osIn.write(VER);
                osIn.flush();
                System.out.println("> " + bytesToHexString(VER, 0, VER.length));
                len = isIn.read(buffer);
                System.out.println("< " + bytesToHexString(buffer, 0, len));
                String host = findHost(buffer, 4, 7);
                int port = findPort(buffer, 8, 9);
                socketOut = new Socket(host, port);
                isOut = socketOut.getInputStream();
                osOut = socketOut.getOutputStream();
                for (int i = 4; i <= 9; i++) {
                    CONNECT_OK[i] = buffer[i];
                }
                osIn.write(CONNECT_OK);
                osIn.flush();
                System.out.println("> " + bytesToHexString(CONNECT_OK, 0, CONNECT_OK.length));
                SocketThreadOutput out = new SocketThreadOutput(isIn, osOut);
                out.start();
                SocketThreadInput in = new SocketThreadInput(isOut, osIn);
                in.start();
                out.join();
                in.join();
            } catch (Exception e) {
                System.err.println("exception:" + e);
            } finally {
                try {
                    if (socketIn != null) {
                        socketIn.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("socket close");
        }

        private String findHost(byte[] buffer, int begin, int end) {
            StringBuilder builder = new StringBuilder();
            for (int i = begin; i <= end; i++) {
                builder.append(Integer.toString(0xFF & buffer[i]));
                builder.append(".");
            }
            builder.deleteCharAt(builder.length() - 1);
            return builder.toString();
        }

        private int findPort(byte[] buffer, int begin, int end) {
            int port = 0;
            for (int i = begin; i <= end; i++) {
                port <<= 16;
                port += buffer[i];
            }
            return port;
        }

        private String bytesToHexString(byte[] buffer, int begin, int end) {
            StringBuilder builder = new StringBuilder(buffer.length);
            for (int i = begin; i < end; i++) {
                String s = Integer.toHexString(0xFF & buffer[i]);
                if (s.length() < 2) {
                    builder.append(0);
                }
                builder.append(s.toUpperCase());
                builder.append(" ");
            }
            return builder.toString();
        }
    }

    private static class SocketThreadOutput extends Thread {

        private InputStream isIn;

        private OutputStream osOut;

        private byte[] buffer = new byte[409600];

        SocketThreadOutput(InputStream isIn, OutputStream osOut) {
            this.isIn = isIn;
            this.osOut = osOut;
        }

        @Override
        public void run() {
            try {
                int len;
                while ((len = isIn.read(buffer)) != -1) {
                    if (len > 0) {
                        System.out.println(new String(buffer, 0, len));
                        osOut.write(buffer, 0, len);
                        osOut.flush();
                    }
                }
            } catch (Exception e) {
                System.out.println("SocketThreadOutput leave");
            }
        }
    }

    private static class SocketThreadInput extends Thread {

        private InputStream isOut;

        private OutputStream osIn;

        private byte[] buffer = new byte[409600];

        SocketThreadInput(InputStream isOut, OutputStream osIn) {
            this.isOut = isOut;
            this.osIn = osIn;
        }

        @Override
        public void run() {
            try {
                int len;
                while ((len = isOut.read(buffer)) != -1) {
                    if (len > 0) {
                        System.out.println(new String(buffer, 0, len));
                        osIn.write(buffer, 0, len);
                        osIn.flush();
                    }
                }
            } catch (Exception e) {
                System.out.println("SocketThreadInput leave");
            }
        }
    }

}

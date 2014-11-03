/**
 * created on 19:59:40 3 lis 2014 by Radoslaw Jarzynka
 * 
 * @author Radoslaw Jarzynka
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

public class Server {
 
    private static final int PORT = 8000;

    private static HashSet<String> clients = new HashSet<String>();
    private static HashSet<String> servers = new HashSet<String>();

    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();
    private static HashSet<PrintWriter> serverWriters = new HashSet<PrintWriter>();

    public static void main(String[] args) throws Exception {
        System.out.println("The server is running.");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
                new Handler(listener.accept()).start();
            }
        } finally {
            listener.close();
        }
    }

    private static class Handler extends Thread {
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public Handler(Socket socket) {
            this.socket = socket;
            System.out.println("New Connection!");
        }

        public void run() {
            try {
                boolean isServer;
                in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                while (true) {
                    //numer telefonu lub "server + jakies losowe id"
                    name = in.readLine();
                    System.out.println(name);
                    if (name == null) {
                        return;
                    }
                    synchronized (clients) {
                        if (!name.contains("server") && !clients.contains(name)) {
                            clients.add(name);
                            isServer = false;
                            System.out.println("Client with number " + name);
                            break;
                        }
                    }
                    synchronized(servers) {
                        if (name.contains("server") && !servers.contains(name)) {
                            servers.add(name);
                            isServer = true;
                            System.out.println("Server with name " + name);
                            break;
                        }
                    }
                }
                if (isServer) {
                    serverWriters.add(out);
                } else {
                    writers.add(out);
                }
                
                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        return;
                    }
                    for (PrintWriter writer : serverWriters) {
                        System.out.println("Message from " + name + ": " + input);
                        writer.println(input);
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                if (name != null) {
                    System.out.println(name + " disconnected!");
                    clients.remove(name);
                    servers.remove(name);
                }
                if (out != null) {
                    writers.remove(out);
                    serverWriters.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}


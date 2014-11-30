/**
 * created on 19:59:40 3 lis 2014 by Radoslaw Jarzynka
 * 
 * @author Radoslaw Jarzynka
 */
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static final int CLIENTS_PORT = 9000;
    public static final int SERVERS_PORT = 8000;

    public void StartServer() {
        // Open server socket for listening
        ServerSocket serverSocketForClients = null;
        ServerSocket serverSocketForServers = null;
        UDPListener udpListener = null;
        try {
            serverSocketForClients = new ServerSocket(CLIENTS_PORT);
            serverSocketForServers = new ServerSocket(SERVERS_PORT);
            System.out.println("Relay for Clients started on port " + CLIENTS_PORT);
            System.out.println("Relay for Servers started on port " + SERVERS_PORT);
        } catch (IOException se) {
            System.err.println("Can not start listening on ports " + CLIENTS_PORT + " and " + SERVERS_PORT);
            se.printStackTrace();
            System.exit(-1);
        }

        ServerDispatcher serverDispatcher = new ServerDispatcher();
        Thread dispatcherThread = new Thread(serverDispatcher);
        dispatcherThread.start();
        System.out.println("Created Handler for Clients");
        Handler handlerForClients = new Handler(serverSocketForClients, serverDispatcher, false);
        Thread clientsHandlerThread = new Thread (handlerForClients);
        System.out.println("Created Handler for Servers");
        Handler handlerForServers = new Handler(serverSocketForServers, serverDispatcher, true);
        Thread serversHandlerThread = new Thread(handlerForServers);

        udpListener = new UDPListener(serverDispatcher);
        Thread udpListenerThread = new Thread(udpListener);

        clientsHandlerThread.start();
        serversHandlerThread.start();
        udpListenerThread.start();

    }

    /**
     * Entry point
     * @param args
     */
    public static void main(String[] args) {
        Server server = new Server();
        server.StartServer();
    }

    /**
     * Inner class designed to handle connections
     */
    private class Handler implements Runnable {

        private ServerSocket relayClientSocket;
        private boolean isForServers;
        private ServerDispatcher serverDispatcher;

        public Handler(ServerSocket relayClientSocket, ServerDispatcher serverDispatcher, boolean isForServers) {
            this.relayClientSocket = relayClientSocket;
            this.serverDispatcher = serverDispatcher;
            this.isForServers = isForServers;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    String type;
                    if (isForServers) {
                        type = (" Servers ");
                    } else {
                        type = (" Clients ");
                    }
                    System.out.println("Socket For" + type + "Opened");
                    Socket socket = relayClientSocket.accept();
                    RelayClientInfo relayClientInfo = new RelayClientInfo();
                    relayClientInfo.socket = socket;
                    RelayClientListener relayClientListener =
                            new RelayClientListener(relayClientInfo, serverDispatcher);
                    RelayClientSender relayClientSender =
                            new RelayClientSender(relayClientInfo, serverDispatcher);
                    relayClientInfo.relayClientListener = relayClientListener;
                    relayClientInfo.relayClientSender = relayClientSender;
                    relayClientListener.start();
                    relayClientSender.start();
                    if (isForServers) {
                        serverDispatcher.addServer(relayClientInfo);
                    } else {
                        serverDispatcher.addClient(relayClientInfo);
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }
}


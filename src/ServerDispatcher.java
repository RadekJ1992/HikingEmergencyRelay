/**
* Created by radoslawjarzynka on 05.11.14.
*/

import java.net.Socket;
import java.util.Vector;

public class ServerDispatcher implements Runnable
{
    private Vector messages = new Vector();
    private Vector clients = new Vector();
    private Vector servers = new Vector();

    public synchronized void addClient(RelayClientInfo relayClientInfo)
    {
        clients.add(relayClientInfo);
    }

    public synchronized void addServer(RelayClientInfo relayClientInfo)
    {
        servers.add(relayClientInfo);
    }


    public synchronized void deleteClient(RelayClientInfo relayClientInfo)
    {
        int clientIndex = clients.indexOf(relayClientInfo);
        if (clientIndex != -1)
            clients.removeElementAt(clientIndex);
    }

    public synchronized void deleteServer(RelayClientInfo relayClientInfo)
    {
        int clientIndex = servers.indexOf(relayClientInfo);
        if (clientIndex != -1)
            servers.removeElementAt(clientIndex);
    }

    public synchronized void dispatchMessage(RelayClientInfo relayClientInfo, String aMessage)
    {

        Socket socket = relayClientInfo.socket;
        String senderIP = socket.getInetAddress().getHostAddress();
        String senderPort = "" + socket.getPort();
        System.out.println("Received Message from " + senderIP + ":" + senderPort + " : " + aMessage);
        messages.add(aMessage);
        notify();
    }

    private synchronized String getNextMessageFromQueue()
            throws InterruptedException
    {
        if (messages.size()==0) {
            return null;
        }
        String message = (String) messages.get(0);
        messages.removeElementAt(0);
        return message;
    }

    private synchronized void sendMessageToAllClients(String aMessage)
    {
        for (int i=0; i< clients.size(); i++) {
            RelayClientInfo relayClientInfo = (RelayClientInfo) clients.get(i);
            relayClientInfo.relayClientSender.sendMessage(aMessage);
        }
    }

    private synchronized void sendMessageToAllServers(String aMessage)
    {
        System.out.println("Sending Message : " + aMessage);
        for (int i=0; i< servers.size(); i++) {
            RelayClientInfo relayClientInfo = (RelayClientInfo) servers.get(i);
            relayClientInfo.relayClientSender.sendMessage(aMessage);
        }
    }

    public void run()
    {
        System.out.println("Server Dispatcher started");
        try {
            while (true) {
                String message = getNextMessageFromQueue();
                if (message != null) {
                    sendMessageToAllServers(message);
                }
                Thread.sleep(200);
            }
        } catch (InterruptedException ie) {
        }
    }

}

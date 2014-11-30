/**
 * Created by radoslawjarzynka on 05.11.14.
 */

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Vector;

public class RelayClientSender extends Thread
{
    private Vector messages = new Vector();

    private ServerDispatcher serverDispatcher;
    private RelayClientInfo relayClientInfo;
    private PrintWriter printWriter;

    public RelayClientSender(RelayClientInfo relayClientInfo, ServerDispatcher serverDispatcher)
            throws IOException
    {
        this.relayClientInfo = relayClientInfo;
        this.serverDispatcher = serverDispatcher;
        Socket socket = relayClientInfo.socket;
        System.out.println("Sender Created for IP " + relayClientInfo.socket.getInetAddress());
        printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public synchronized void sendMessage(String aMessage)
    {
        messages.add(aMessage);
        notify();
    }

    private synchronized String getNextMessageFromQueue() throws InterruptedException
    {
        while (messages.size()==0) {
            return null;
        }
        String message = (String) messages.get(0);
        messages.removeElementAt(0);
        return message;
    }

    private void sendMessageToClient(String aMessage)
    {
        printWriter.println(aMessage);
        printWriter.flush();
    }

    public void run()
    {
        System.out.println("Sender Started for IP " + relayClientInfo.socket.getInetAddress());
        try {
            while (!isInterrupted()) {
                String message = getNextMessageFromQueue();
                if (message != null) {
                    sendMessageToClient(message);
                }
                Thread.sleep(200);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println(relayClientInfo.socket.getInetAddress() + " Disconnected");
        }

        relayClientInfo.relayClientListener.interrupt();
        serverDispatcher.deleteClient(relayClientInfo);
        serverDispatcher.deleteServer(relayClientInfo);
    }

}

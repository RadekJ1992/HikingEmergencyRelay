import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Klasa odbierająca przychodzące datagramy UDP
 *
 * Created by radoslawjarzynka on 30.11.14.
 */
public class UDPListener implements Runnable {

    //port, na którym nasłuchuje aplikacja
    public static final int UDP_PORT = 9001;

    //rozmiar pakietu
    private final static int PACKET_SIZE = 1024 ;
    private ServerDispatcher serverDispatcher;

    public UDPListener(ServerDispatcher serverDispatcher) {
        this.serverDispatcher = serverDispatcher;
    }


    public void run()
    {
        try {
            DatagramSocket socket = new DatagramSocket(UDP_PORT) ;

            System.out.println( "Listening for UDP Packets on port 8001" ) ;

        while (true) {
                DatagramPacket packet = new DatagramPacket( new byte[PACKET_SIZE], PACKET_SIZE ) ;
                socket.receive(packet) ;
                String message = new String(packet.getData());
                System.out.println("Received UDP Message: " + packet.getAddress() + " " + packet.getPort() + ": " + message);
                serverDispatcher.dispatchMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

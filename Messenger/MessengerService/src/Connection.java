import java.io.*;
import java.net.Socket;

public class Connection {
    private final ConnectionReader connectionReader;
    private final Socket socket;
    private final Thread inThread;
    private final BufferedReader in;
    private final BufferedWriter out;

    //Первый конструктор
    public Connection(ConnectionReader connectionReader, Socket socket) throws IOException {
        this.connectionReader = connectionReader;
        this.socket = socket;
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        inThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    connectionReader.newConnectionAccept(Connection.this, in.readLine());
                    while (!inThread.isInterrupted()) {
                        connectionReader.newMessageReceive(Connection.this, in.readLine());
                    }
                } catch (IOException ex) {
                    connectionReader.exceptionThrows(Connection.this, ex);
                } finally {
                    connectionReader.disconnect(Connection.this);
                }
            }
        }
        );
        inThread.start();
    }

    //Второй конструктор
    public Connection(ConnectionReader connectionReader, String ipAddress, int port) throws IOException {
        this(connectionReader, new Socket(ipAddress, port));
    }

    //Потокобезопасный метод отправки сообщения
    public synchronized void sendMessage(String msg) {
        try {
            out.write(msg + "\r\n");
            out.flush();
        } catch (IOException ex) {
            connectionReader.exceptionThrows(Connection.this, ex);
            disconnect();
        }
    }

    //Потокобезопасный метод разрыва соединения
    public synchronized void disconnect() {
        inThread.interrupt();
        try {
            socket.close();
        } catch (IOException ex) {
            connectionReader.exceptionThrows(Connection.this, ex);
        }
    }

    @Override
    public String toString() {
        return "IP:" + socket.getInetAddress() + " Port:" + socket.getPort();
    }
}
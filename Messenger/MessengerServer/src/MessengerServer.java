import java.io.*;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class MessengerServer implements ConnectionReader, Logger {
    private final ArrayList<Connection> connectionsList = new ArrayList<>();

    public static void main(String[] args) {
        new MessengerServer();
    }

    // Метод получения номера порта из файла settings
    private int getPortFromSettingsFile() throws IOException {
        String portLine = Files.readAllLines(Paths.get("settings.txt")).get(0);
        String[] portLineArr = portLine.split("Port: ");
        return Integer.parseInt(portLineArr[1]);
    }

    //Конструктор
    private MessengerServer() {
        String serverIsRunningMsg = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss "))
                + "Сервер мессенджера запущен...";
        System.out.println(serverIsRunningMsg);
        log(serverIsRunningMsg);
        try (ServerSocket serverSocket = new ServerSocket(getPortFromSettingsFile())) {
            while(true) {
                try {
                    new Connection(this, serverSocket.accept());
                } catch (IOException ex) {
                    String connectionFailedMsg = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss "))
                            + "Ошибка соединения:" + ex;
                    System.out.println(connectionFailedMsg);
                    log(connectionFailedMsg);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    //Новое подключение
    @Override
    public synchronized void newConnectionAccept(Connection connection, String name) {

        //Добавляет новое подключение в список
        connectionsList.add(connection);

        //Добавляет даёт название потоку с помощью полученного от соединения имени
        Thread.currentThread().setName(name);

        //Создаёт строку для отправки сообщения и логирования
        String clientConnectedMsg = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss ")) + Thread.currentThread().getName()
                + " присоединил(ась)ся к чату.";

        //Отправляет сообщение всем соединениям в списке
        sendToAllClients(clientConnectedMsg);

        //Логирует сообщение
        log(clientConnectedMsg + " " + connection);
    }

    //Новое сообщение
    @Override
    public synchronized void newMessageReceive(Connection connection, String msg) {
        //Создаёт строку для отправки сообщения и логирования
        String receiveMsg = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss ")) + Thread.currentThread().getName() + ": " + msg;
        //Отправляет сообщение всем соединениям в списке
        sendToAllClients(receiveMsg);
        //Логирует сообщение
        log(receiveMsg);

    }

    //Разрыв соединения
    @Override
    public synchronized void disconnect(Connection connection) {

        //Удаляет соединение из списка
        connectionsList.remove(connection);

        //Создаёт строку для отправки сообщения и логирования
        String clientDisconnectedMsg = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss ")) + Thread.currentThread().getName()
                + " покинул(а) чат.";

        //Отправляет сообщение всем соединениям в списке
        sendToAllClients(clientDisconnectedMsg);

        //Логирует сообщение
        log(clientDisconnectedMsg);
    }

    //Сообщение об исключении
    @Override
    public synchronized void exceptionThrows(Connection connection, Exception ex) {

        //Создаёт строку для отправки сообщения об исключении
        String exceptionMsg = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss "))
                + ex.getMessage();

        //Выводит сообщение об исключении на консоль сервера
        System.out.println(exceptionMsg);

        //Логирует сообщение об исключении
        log(exceptionMsg);
    }

    //Отправка сообщения всем подключениям
    private void sendToAllClients(String msg) {

        //Выводит сообщение на консоль сервера
        System.out.println(msg);

        //Перебирает все соединения и отправляет каждому сообщение
        for (Connection connection : connectionsList) {
            connection.sendMessage(msg);
        }
    }

    //Логирование
    @Override
    public void log(String msg) {

        //создаёт класс для записи в файл с указанием пути и дозаписи строк
        try (FileWriter writer = new FileWriter("log.txt", true)) {
            writer.write(msg);
            writer.append("\r\n");
            writer.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
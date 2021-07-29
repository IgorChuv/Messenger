import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class MessengerClient implements ConnectionReader {
    private static final Scanner scanner = new Scanner(System.in);
    public static Connection connection;

    public static void main(String[] args) {
        System.out.println("Для входа в чат укажите никнейм: ");

        //До подключения к чату требуется указать никнейм
        String nickName = scanner.nextLine();

        //Создаётся объект класса клиента с указанием никнейма
        new MessengerClient(nickName);

        //Отправляет сообщение до тех пор пока не будет введена строка /exit
        while(true) {
            String msg = "";
            msg = scanner.nextLine();

            if (msg.equals("/exit")) {
                break;
            }
            connection.sendMessage(msg);
        }
        connection.disconnect();
    }

    //Конструктор класса
    public MessengerClient(String name) {
        try {
            connection = new Connection(this, getIpFromSettingsFile(), getPortFromSettingsFile());
            connection.sendMessage(name);
        } catch (IOException ex) {
            ex.getMessage();
        }
    }

    // Метод получения номера порта из файла settings
    private int getPortFromSettingsFile() throws IOException {
        String portLine = Files.readAllLines(Paths.get("settings.txt")).get(0);
        String[] portLineArr = portLine.split("Port: ");
        return Integer.parseInt(portLineArr[1]);
    }

    // Метод получения номера IP из файла settings
    private String getIpFromSettingsFile() throws IOException {
        String portLine = Files.readAllLines(Paths.get("settings.txt")).get(1);
        String[] portLineArr = portLine.split("IP: ");
        return portLineArr[1];
    }

    //Получает сообщение о подключении
    @Override
    public void newConnectionAccept(Connection connection, String message) {
        System.out.println(message);
    }

    //Выводит на консоль полученное сообщение
    @Override
    public void newMessageReceive(Connection connection, String msg) {
        System.out.println(msg);
    }

    //Выводит на консоль сообщение об отключении от чата
    @Override
    public void disconnect(Connection connection) {
        System.out.println("Вы покинули чат.");

    }

    //Выводит на консоль сообщение исключении
    //TODO: Клиенту не требуется. Нужно изменить код.
    @Override
    public void exceptionThrows(Connection connection, Exception ex) {
        //System.out.println("Соединение закрыто.");

    }
}
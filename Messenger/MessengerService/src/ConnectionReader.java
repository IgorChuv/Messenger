public interface ConnectionReader {

    //Метод получения нового соединения
    void newConnectionAccept(Connection connection, String name);

    //Метод получения нового сообщения
    void newMessageReceive(Connection connection, String msg);

    //Метод обрыва соединения
    void disconnect(Connection connection);

    //Метод получения исключения
    void exceptionThrows(Connection connection, Exception ex);
}

package server;

import java.io.*;
import java.net.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Главный класс сервера мессенджера.
 * Создаёт ServerSocket, принимает клиентов и запускает обработку подключений.
 */
public class ServerMain {

    private static final Logger logger = LogManager.getLogger(ServerMain.class);


    /**
     * Точка входа сервера.
     * Инициализирует базу данных, создаёт таблицы и ждёт подключения клиентов.
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args){
        DataBase.setConnection();
        DataBase.createTables();
        try {
            logger.info("Waiting for a client");
            ServerSocket serverSocket = new ServerSocket(9806);
            while (true) {
                Socket soc = serverSocket.accept();
                soc.setSoTimeout(300000);
                ClientHandler clientHandler = new ClientHandler(soc);
                clientHandler.start();
            }
        } catch (IOException e){logger.error("Server error: " + e.getMessage());}

    }
}

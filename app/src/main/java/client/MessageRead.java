package client;

import java.io.BufferedReader;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Класс для чтения сообщений от сервера.
 * Работает в отдельном потоке.
 */
public class MessageRead extends Thread{

    private static final Logger logger = LogManager.getLogger(MessageRead.class);

    private BufferedReader in;

    /**
     * Создаёт поток для чтения сообщений.
     * @param in BufferedReader для чтения от сервера
     */
    public MessageRead(BufferedReader in) {
        this.in = in;
    }

    /**
     * Основной метод потока.
     * Получает сообщения от сервера и выводит их на консоль.
     */
    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null){
                System.out.println(message);
            }
        }catch (IOException e){
            if (!"Socket closed".equals(e.getMessage())) {
                logger.error("Message reading error: " + e.getMessage());
            }
        }
    }
}

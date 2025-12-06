package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Класс для отправки сообщений на сервер.
 * Работает в отдельном потоке.
 */
public class MessageSend extends Thread{

    private static final Logger logger = LogManager.getLogger(MessageSend.class);

    private BufferedReader inputReader;
    private PrintWriter out;
    private Socket soc;

    /**
     * Создаёт поток для отправки сообщений.
     * @param out PrintWriter для отправки на сервер
     * @param inputReader BufferedReader для чтения сообщений с консоли
     * @param soc сокет соединения
     */
    public MessageSend(PrintWriter out, BufferedReader inputReader, Socket soc){
        this.out = out;
        this.inputReader = inputReader;
        this.soc = soc;
    }

    /**
     * Основной метод потока.
     * Читает сообщения с консоли и отправляет на сервер.
     */
    public void run() {
        try {
            String message;
            while ((message = inputReader.readLine()) != null) {
                if (message.equals("/exit")){
                    out.println(message);
                    soc.close();
                    break;
                }
                out.println(message);
            }
        } catch (IOException e){
            logger.error("Message sending error: " + e.getMessage());
        }
    }
}

package client;

import java.io.*;
import java.net.*;

/**
 * Главный класс клиента мессенджера.
 * Подключается к серверу, запускает потоки для чтения и отправки сообщений.
 */
public class ClientMain {

    /**
     * Точка входа клиента.
     * Инициализирует соединение, запускает потоки MessageSend и MessageRead.
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        try {
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
            Socket soc = new Socket("127.0.0.1", 9806);
            BufferedReader in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
            PrintWriter out = new PrintWriter(soc.getOutputStream(), true);

            System.out.println("Help about commands");
            System.out.println("-------------------");
            System.out.println("Registration: /register login password");
            System.out.println("Entrance: /login login password");
            System.out.println("Sending a message: /msg target your_message");
            System.out.println("Message history: /history user");
            System.out.println("Ending: /exit");
            System.out.println("Be careful to enter commands correctly.");
            System.out.println("-------------------");

            new MessageSend(out, inputReader, soc).start();
            new MessageRead(in).start();

        } catch (IOException e){System.err.println("Connection error");}

    }
}

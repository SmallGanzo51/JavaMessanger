package server;

import java.net.Socket;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Класс ClientHandler обрабатывает соединение с одним клиентом.
 * <p>
 * Отвечает за регистрацию, вход в систему, отправку и получение сообщений,
 * а также управление историей сообщений и активными пользователями.
 * Логирование действий происходит через Log4j2.
 */
public class ClientHandler extends Thread{

    private static final Logger logger = LogManager.getLogger(ClientHandler.class);

    Socket soc;
    String login = null;
    PrintWriter out = null;
    BufferedReader in = null;
    private boolean authenticated = false;

    /**
     * Создаёт новый обработчик клиента с заданным сокетом.
     *
     * @param soc сокет, соединяющий сервер с клиентом
     */
    public ClientHandler(Socket soc){
        this.soc = soc;
    }

    /**
     * Проверяет, закрыто ли соединение с клиентом.
     *
     * @return {@code true}, если сокет закрыт или null, иначе {@code false}
     */
    public boolean isClosed() {
        return soc == null || soc.isClosed();
    }

    /**
     * Основной метод потока, который запускается при старте ClientHandler.
     * <p>
     * Обрабатывает команды клиента:
     * <ul>
     *     <li>/register login password — регистрация нового пользователя</li>
     *     <li>/login login password — вход в систему</li>
     *     <li>/msg target message — отправка сообщения другому пользователю</li>
     *     <li>/history user — вывод истории переписки с другим пользователем</li>
     *     <li>/exit — завершение соединения</li>
     * </ul>
     * <p>
     * Логирование событий:
     * <ul>
     *     <li>Подключение клиента</li>
     *     <li>Регистрация и вход пользователя</li>
     *     <li>Отправка сообщений</li>
     *     <li>Ошибки при обработке сообщений</li>
     * </ul>
     */
    public void run() {

        try {

            logger.info("Connection established");
            in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
            out = new PrintWriter(soc.getOutputStream(), true);

            while (true) {

                String string = in.readLine();

                if (string == null || string.equals("/exit")){
                    break;
                }

                if (!authenticated){

                    if (string.startsWith("/register")) {
                        String[] comands = string.split(" ", 3);
                        if (comands.length < 3){
                            out.println("Please enter your login and password correctly.");
                            continue;
                        }
                        this.login = comands[1];
                        if (DataBase.registerUser(login, comands[2])){
                            authenticated = true;
                            out.println("Registration successful");
                            Users.setActiveUser(login, this);
                            logger.info("User registered: " + login);
                        }
                        else {
                            out.println("Login already taken");
                        }
                    }

                    else if (string.startsWith("/login")) {
                        String[] comands = string.split(" ", 3);
                        if (comands.length < 3) {
                            out.println("Please enter your login and password correctly.");
                            continue;
                        }
                        this.login = comands[1];
                        if (DataBase.loginUser(login, comands[2])) {
                            authenticated = true;
                            out.println("Welcome!");
                            Users.setActiveUser(login, this);
                            List<String> offlineMessages = DataBase.getOfflineMessages(login);
                            for (String item : offlineMessages){out.println(item);}
                            DataBase.markDeliveredMessages(login);
                            logger.info("User logged in: " + login);
                        } else {
                            out.println("Uncorrected login or password");
                        }
                    }

                    else if(string.startsWith("/msg")) {
                        out.println("Please register or log in first.");
                    }

                    else if(string.startsWith("/history")) {
                        out.println("Please register or log in first.");
                    }

                    else {
                        out.println("Enter the correct command");
                    }
                }

                else {

                    if (string.startsWith("/msg")) {
                        String[] commands = string.split(" ", 3);
                        if (commands.length < 3){
                            out.println("Please enter the recipient and message correctly.");
                            continue;
                        }

                        String target = commands[1];
                        String message = commands[2];

                        if (!DataBase.userCheck(target)){
                            out.println("User " + target + " is not found");
                            continue;
                        }
                        ClientHandler recipient = Users.getActiveUser(target);
                        boolean delivered = false;
                        if (recipient != null && !recipient.isClosed()) {
                            try {
                                recipient.sendMessage("From " + login + ": " + message);
                                delivered = true;
                                out.println("Successful");
                                logger.info("Message sent from " + login + " to " + target);
                            } catch (Exception e) {
                                delivered = false;
                                out.println("User is currently unavailable, message saved offline.");
                            }
                        } else {
                            out.println("Successful");
                        }
                        DataBase.saveMessages(login, target, message, delivered);
                    }
                    else if (string.startsWith("/register") || string.startsWith("/login")) {
                        out.println("You are already logged in");
                    }

                    else if (string.startsWith("/history")){
                        String[] commands = string.split(" ", 2);
                        if (commands.length < 2){
                            out.println("Please enter the command correctly to see message history");
                            continue;
                        }
                        String secondUser = commands[1];
                        if (!DataBase.userCheck(secondUser)){
                            out.println("User " + secondUser + " is not found");
                            continue;
                        }
                        List<String> messageHistory = DataBase.getMessageHistory(login, secondUser);
                        if (messageHistory.isEmpty()){out.println("There are no messages with " + secondUser + ".");}
                        else {
                            for (String item : messageHistory) {
                                out.println(item);
                            }
                        }
                    }

                    else {
                        out.println("Enter the correct command");
                    }
                }
            }


        }catch (IOException e){
            logger.error("Client Handler error" + e.getMessage());
        } finally {
            try {
                if (this.login != null) {
                    Users.removeUser(login);
                }
                if (in != null) in.close();
                if (out != null) out.close();
                if (soc != null && !soc.isClosed()) soc.close();
            } catch (IOException e) {
                System.err.println("Resources closing error: " + e.getMessage());
            }
        }
    }

    /**
     * Отправляет текстовое сообщение клиенту через сокет.
     *
     * @param message текст сообщения
     */
    public void sendMessage(String message){
        out.println(message);
    }
}

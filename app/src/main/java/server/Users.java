package server;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Класс для хранения активных пользователей на сервере.
 * Управляет добавлением, получением и удалением клиентов.
 */
public class Users {

    private static Map<String, ClientHandler> activeClients = new ConcurrentHashMap<>();

    /**
     * Добавляет пользователя в список активных.
     * @param login логин пользователя
     * @param clientHandler обработчик клиента
     */
    public static void setActiveUser(String login, ClientHandler clientHandler){
        activeClients.put(login, clientHandler);

    }

    /**
     * Удаляет пользователя из списка активных.
     * @param login логин пользователя
     */
    public static void removeUser(String login){activeClients.remove(login);}

    /**
     * Возвращает обработчик активного пользователя по логину.
     * @param target логин пользователя
     * @return ClientHandler или null, если пользователь неактивен
     */
    public static ClientHandler getActiveUser(String target){
       return activeClients.get(target);
    }
}



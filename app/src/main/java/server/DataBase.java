package server;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.security.MessageDigest;
import java.util.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Класс для работы с базой данных SQLite.
 * Содержит методы регистрации пользователей, логина, сохранения сообщений и истории.
 */
public class DataBase {

    private static final Logger logger = LogManager.getLogger(DataBase.class);

    private static Connection connection;
    private static final int hash_iterations = 100000;

    /**
     * Устанавливает соединение с базой данных.
     * Создаёт файл database.db, если его ещё нет.
     */
    public static void setConnection() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:database.db");
            logger.info("Connect to DB");
        } catch (SQLException e) {
            logger.error("DataBase connection error: " + e.getMessage());
        }
    }

    /**
     * Создаёт таблицы Users и Messages, если они ещё не существуют.
     */
    public static void createTables(){
        try(Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS Users(ID INTEGER PRIMARY KEY AUTOINCREMENT, Login TEXT UNIQUE, salt TEXT, password TEXT)");
            statement.execute("CREATE TABLE IF NOT EXISTS Messages(ID INTEGER PRIMARY KEY AUTOINCREMENT, sender TEXT, recipient TEXT, message TEXT, time DATETIME DEFAULT CURRENT_TIMESTAMP, delivered INTEGER DEFAULT 0)");
            logger.info("Tables have been created");
        }catch (SQLException e){
            logger.error("DataBase creation error: " + e.getMessage());
        }
    }

    /**
     * Генерирует случайную соль для пароля.
     * Используется для безопасного хэширования пароля пользователя.
     *
     * @return случайная строка в формате Base64, представляющая соль
     */
    private static String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Хэширует пароль с использованием соли и алгоритма SHA-256.
     * Применяет многократное хэширование (hash_iterations) для повышения безопасности.
     *
     * @param password пароль пользователя
     * @param salt строка соли
     * @return хэш пароля в формате Base64, или null в случае ошибки
     */
    private static String hashPassword(String password, String salt) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = (salt + password).getBytes();
            for (int i = 0; i < hash_iterations; i++) {
                messageDigest.reset();
                hash = messageDigest.digest(hash);
            }
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Hash error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Регистрирует нового пользователя с логином и паролем.
     * @param login логин пользователя
     * @param password пароль пользователя
     * @return true, если регистрация успешна, false если логин занят
     */
    public static boolean registerUser(String login, String password){
        String salt = generateSalt();
        try(PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Users (login, salt, password) VALUES(?, ?, ?)")) {
            String hash = hashPassword(password, salt);
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, salt);
            preparedStatement.setString(3, hash);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.error("Register user error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Проверяет логин и пароль пользователя при входе.
     * @param login логин
     * @param password пароль
     * @return true, если логин и пароль верны
     */
    public static boolean loginUser(String login, String password) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT salt, password FROM Users WHERE login = ?")) {
            preparedStatement.setString(1, login);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) return false;
            String salt = resultSet.getString("salt");
            String storedHash = resultSet.getString("password");
            String hashAttempt = hashPassword(password, salt);
            return storedHash.equals(hashAttempt);
        } catch (SQLException e) {
            logger.error("Login user error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Сохраняет сообщение между пользователями в базе.
     * @param sender отправитель
     * @param recipient получатель
     * @param message текст сообщения
     * @param delivered true, если сообщение доставлено, false если оффлайн
     */
    public static void saveMessages(String sender, String recipient, String message, boolean delivered){
        try(PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Messages (sender, recipient, message, delivered) VALUES (?, ?, ?, ?)")){
            preparedStatement.setString(1, sender);
            preparedStatement.setString(2, recipient);
            preparedStatement.setString(3, message);
            preparedStatement.setInt(4, delivered ? 1 : 0);
            preparedStatement.executeUpdate();
        }catch (SQLException e) {
            logger.error("Save message Error: " + e.getMessage());
        }
    }

    /**
     * Получает оффлайн-сообщения для пользователя.
     * @param login логин получателя
     * @return список сообщений в формате [время] отправитель: сообщение
     */
    public static List<String> getOfflineMessages(String login) {
        List<String> messages = new ArrayList<>();
        String sql = "SELECT sender, message, time FROM Messages WHERE recipient = ? AND delivered = 0 ORDER BY time";
        try (PreparedStatement prepareStatement = connection.prepareStatement(sql)) {
            prepareStatement.setString(1, login);
            try (ResultSet rs = prepareStatement.executeQuery()) {
                while (rs.next()) {
                    String from = rs.getString("sender");
                    String message = rs.getString("message");
                    String time = rs.getString("time");

                    messages.add("[" + time + "] " + from + ": " + message);
                }
            }
        } catch (SQLException e) {
            logger.error("Get offline messages error: " + e.getMessage());
        }
        return messages;
    }

    /**
     * Помечает все недоставленные сообщения пользователя как доставленные.
     * @param login логин получателя
     */
    public static void markDeliveredMessages(String login){
        try(PreparedStatement preparedStatement = connection.prepareStatement("UPDATE Messages SET delivered = 1 WHERE recipient = ? AND delivered = 0")){
            preparedStatement.setString(1, login);
            preparedStatement.executeUpdate();
        }catch (SQLException e){
            logger.error("Mark delivered messages error: " + e.getMessage());
        }
    }

    /**
     * Проверяет, существует ли пользователь с заданным логином.
     * @param login логин
     * @return true, если пользователь существует
     */
    public static boolean userCheck(String login){
        try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT 1 FROM Users WHERE login = ?")) {
            preparedStatement.setString(1, login);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        }catch (SQLException e){
            logger.error("User check error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Возвращает историю сообщений между двумя пользователями.
     * @param user1 первый пользователь
     * @param user2 второй пользователь
     * @return список сообщений с временными отметками
     */
    public static List<String> getMessageHistory(String user1, String user2){
        List<String> messageHistory = new ArrayList<>();
        try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT sender, recipient, message, time FROM Messages WHERE (sender = ? AND recipient = ?) OR (sender = ? AND recipient = ?) ORDER BY time")) {
            preparedStatement.setString(1, user1);
            preparedStatement.setString(2, user2);
            preparedStatement.setString(3, user2);
            preparedStatement.setString(4, user1);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                String sender = resultSet.getString("sender");
                String recipient = resultSet.getString("recipient");
                String message = resultSet.getString("message");
                String time = resultSet.getString("time");
                messageHistory.add("[" + time + "] " + "from " + sender + " to " + recipient + ": " + message);
            }
        }catch (SQLException e){
            logger.error("Message history error: " + e.getMessage());
        }
        return messageHistory;
    }
}

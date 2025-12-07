Java Messanger

A simple Java messenger with message storage in SQLite and logging via Log4j2.

Features:
- User registration and login with password hashing and salt.
- Sending messages to other users.
- Offline message storage and delivery.
- Viewing message history between users.
- Logging messages and server events to both console and file (`logs/messenger.log`).

Software used:
- Java 25
- Gradle 9.2.1
- SQLite JDBC (`org.xerial:sqlite-jdbc`)
- Log4j2 (`org.apache.logging.log4j:log4j-api` and `log4j-core`)

!!!You can see the documentation about classes and methods used in the code [here]: https://smallganzo51.github.io/JavaMessanger/

!!!SETUP INSTRUCTION!!!
1. Clone the repository
```bash
git clone <repository-url>
cd JavaMessanger
```
2.Build the Application

#Build server JAR
```bash
./gradlew serverJar
```
#Build client JAR
```bash
./gradlew clientJar
```
The JARs will be created in app/build/libs/

3.Run the application 

#Run Server
```bash
java -jar app/build/libs/MessengerServer-1.0.jar
```
#Run Client
```bash
java -jar app/build/libs/MessengerClient-1.0.jar
```

!The client can be run multiple times to simulate multiple users connecting to the server.

!Notes

-Database:

1)The SQLite database (database.db) is automatically created in the project root when the server starts.

2)Tables Users and Messages are created automatically if they do not exist.

-Logging:

1)Logs are written to both the console and logs/messenger.log.

2)Make sure the logs/ folder exists, or it will be created automatically.

-Dependencies:

1)All required libraries are managed through Gradle. No manual download is needed.

2)Running on Different Machines:
Because the database and logs are created automatically, the application can be run on any machine without additional setup.

Author: SmallGanzo51

Contact: egorov2006g@gmail.com



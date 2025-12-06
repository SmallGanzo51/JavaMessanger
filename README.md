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

!!!SETUP INSTRUCTION!!!
1. Clone the repository
```bash
git clone <repository-url>
cd Gradle_messanger
2.Build the Application
#Build server JAR
./gradlew serverJar
#Build client JAR
./gradlew clientJar
The JARs will be created in app/build/libs/
3.Run the application 
#Run Server
java -jar app/build/libs/MessengerServer-1.0.jar
#Run Client
java -jar app/build/libs/MessengerClient-1.0.jar

!The client can be run multiple times to simulate multiple users connecting to the server.

Notes
Database:
The SQLite database (database.db) is automatically created in the project root when the server starts.
Tables Users and Messages are created automatically if they do not exist.
Logging:
Logs are written to both the console and logs/messenger.log.
Make sure the logs/ folder exists, or it will be created automatically.
Dependencies:
All required libraries are managed through Gradle. No manual download is needed.
Running on Different Machines:
Because the database and logs are created automatically, the application can be run on any machine without additional setup.

Author
SmallGanzo
Contact: egorov2006g@gmail.com



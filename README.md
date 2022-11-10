# GuessingGame
This Android guessing game (written in Java SE) has two modes:
- Singleplayer mode where you try to guess a word randomly assigned assigned by the server (note: the pool of words is based off user word submissions)
- Bluetooth multiplayer mode where two players play against each other over a Bluetooth connection

A RESTful web service is used to run and moderate the game (created using Jakarta EE 9, a Glassfish web server, and a mySQL database)
- Uses REST endpoints to receive and send game information (JSON) from/to mobile clients
- Uses Enterprise Java Beans (EJBs) to handle game logic internally
- Uses Object Relational Mapping (ORM) for database operations through Java Entities
- Uses the Java Message Service (JMS) and a Message Driven Bean to add or deduct points from users based on the results of a multiplayer game

Created in Android Studio and Netbeans for two university projects.

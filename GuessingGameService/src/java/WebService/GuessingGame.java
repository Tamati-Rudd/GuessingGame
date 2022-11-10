package WebService;

import EJBs.DatabaseOperations;
import EJBs.GuessMaster;
import Entities.Vxz7784Leaderboard;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.inject.Named;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageListener;
import jakarta.jms.MessageProducer;
import jakarta.jms.ObjectMessage;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import java.io.Serializable;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonValue;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * RESTful GuessingGame Web Service
 * Assignment 3: Runs a game allowing users (on mobile) to guess user-submitted words & phrases for points
 * Assignment 4 Extension: facilitates the Bluetooth versus mode of the Guessing Game, including use of JMS for database update operations
 * @author Tamati Rudd 18045626
 */
@Named
@Path("guessinggame")
public class GuessingGame implements Serializable {

    @Context
    private UriInfo context;
    private Logger logger;
    //Assignment 3 EJBs
    @EJB
    private DatabaseOperations persistBean;
    @EJB
    private GuessMaster controllerBean;
    //Assignment 4 Enterprise Messaging Fields
    @Resource(mappedName = "jms/GuessingGameConnectionFactory") private ConnectionFactory connectionFactory;
    @Resource(mappedName = "jms/GuessingGameQueue") private Queue queue;
    private Connection sendingConnection;
    private Session sendingSession;
    
    /**
     * Creates a new instance of GuessingGame
     */
    public GuessingGame() {
        logger = Logger.getLogger(getClass().getName());
    }

    //Ensure JMS connections & sessions are created successfully
    @PostConstruct
    public void setupJMSSessions() {
       if (connectionFactory == null) {
          logger.warning("Dependency injection of jms/GuessingGameConnectionFactory failed");
       }
       else {
          try {
             // obtain a connection to the JMS provider
             sendingConnection = connectionFactory.createConnection();
             // obtain an untransacted context for producing messages
             sendingSession = sendingConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
             logger.info("Dependency injection of jms/GuessingGameConnectionFactory was successful");
          }
          catch (JMSException e) {
             logger.warning("Error while creating session: " + e);
          }
       }
       if (queue == null) {
          logger.warning("Dependency injection of jms/GuessingGameQueue failed");
       }
       else {
          logger.info("Dependency injection of jms/GuessingGameQueue was successful");
       }
    }
    
    //Ensure JMS connections & sessions are closed successfully
    @PreDestroy
    public void closeJMSSessions() {
       try {
          if (sendingSession != null)
             sendingSession.close();
          if (sendingConnection != null)
             sendingConnection.close();
       }
       catch (JMSException e) {
          logger.warning("Unable to close connection: " + e);
       }
    }
    
     //Assignment 3 Endpoints
     //This endpoint logs a user in, creating their account (or getting it if it exists)
     @PUT
     @Consumes(MediaType.TEXT_PLAIN)
     @Produces(MediaType.APPLICATION_JSON)
     @Path("user/{username}")
     public String login(@PathParam("username") String username) {
         Vxz7784Leaderboard user = (Vxz7784Leaderboard) persistBean.getOrAddUser(username);
         if (user != null) {
             JsonObject userJson = Json.createObjectBuilder() //Build JSON object containing user data
                 .add("username", user.getUsername())
                 .add("points", user.getPoints())
                 .add("correctGuesses", user.getCorrectGuesses())
                 .build();
             String jsonStr = userJson.toString(); 
             return jsonStr;
         }  
         else
             return null;
     }
    
    //This endpoint gets data for a single user
    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("user/{username}")
    public String getUserData(@PathParam("username") String username) {
        Vxz7784Leaderboard user = persistBean.getUser(username);
         if (user != null) {
            JsonObject userJson = Json.createObjectBuilder() //Build JSON object containing user data
                .add("username", user.getUsername())
                .add("points", user.getPoints())
                .add("correctGuesses", user.getCorrectGuesses())
                .build();
            String jsonStr = userJson.toString(); 
            return jsonStr;
        }  
        else
            return null;
    }
    
    //This endpoint adds a String to the database of guessable Strings
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("user/{username}/{string}")
    public String putString(
            @PathParam("username") String username,
            @PathParam("string") String strToAdd) {
        boolean added = persistBean.addNewString(strToAdd, username);
        if (added) //Return whether the string was added or already existed
            return "String \""+strToAdd+"\" Added!";
        else
            return "String \""+strToAdd+"\" Already Exists!";
    }
    
    //This endpoint returns a fully hidden representation of the String to be guessed
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("gethidden")
    public String getStringHidden() {
        controllerBean.chooseNewString();
        return controllerBean.getStringHidden();
    }
    
    //This endpoint allows a user to guess a single character in a String
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("user/{username}/guess/{guess}")
    public String makeCharGuess (
        @PathParam("username") String username,
        @PathParam("guess") char guess,
        JsonObject json) {

        //Parse JSON
        JsonArray jsonArray = json.getJsonArray("characters");
        ArrayList<Character> charList = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) { //Get each character from the JSON array
            JsonValue j = jsonArray.get(i);
            Character c = j.toString().charAt(1); //Index 1 as character surrounded by double quotes
            charList.add(c);
        }
        
        //Check the guess through the Singleton EJB
        guess = Character.toLowerCase(guess); //Ensure lowercase
        ArrayList<Character> updatedList = controllerBean.processCharGuess(charList, guess);
        String knownString = controllerBean.getKnownString(updatedList); //Get string representation for mobile UI
        
        //Build JSON Array from ArrayList
        JsonArrayBuilder arr = Json.createArrayBuilder();
        for (Character c : updatedList) {
            arr.add(c.toString());
        }
        
        //Build JSON object
        JsonObject userJson = Json.createObjectBuilder() 
                .add("knownString", knownString)
                .add("characters", arr) //Add array
                .build();
        return userJson.toString();
    }
    
    //This endpoint allows a user to guess a String
    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("user/{username}/guess/{guess}/{points}")
    public String makeStringGuess(
        @PathParam("username") String username,
        @PathParam("guess") String guess, 
        @PathParam("points") int pointsPossible) {
       
        boolean GuessCorrect = controllerBean.processStringGuess(guess); //Check if guess correct
        if (GuessCorrect) { //Guess correct: return correct & update DB
            persistBean.registerCorrectGuess(username, pointsPossible);
            return "Guess Correct";
        }
        else { //Guess incorrect
            return "Guess Incorrect";
        }
    }
    
    //Assignment 4 Endpoints
    //This endpoint gets the list of strings from the database
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("user/{username}/versus/getList")
    public String getStringsList(
            @PathParam("username") String username) {
        ArrayList<String> strings = persistBean.getStrings();
        
        //Build JSON Array from ArrayList
        JsonArrayBuilder arr = Json.createArrayBuilder();
        for (String s : strings) {
            arr.add(s);
        }
        JsonObject userJson = Json.createObjectBuilder() 
                .add("strings", arr) //Add array
                .build();
        return userJson.toString();
    }

    //This endpoint adjusts the points of a user following their winning or losing of a Guessing Game versus match
    //Uses a JMS Producer & Message Driven Bean to perform the database operation(s)
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("user/{username}/versus/{points}")
    public String versusAdjustPoints(
        @PathParam("username") String username,
        @PathParam("points") String points) {
        
        try {
            //Send JMS message to MessageDrivenBean to update database
            MessageProducer producer = sendingSession.createProducer(queue);
            ArrayList<String> userData = new ArrayList<>();
            userData.add(username);
            userData.add(points);
            ObjectMessage message = sendingSession.createObjectMessage(userData);
            producer.send(message);
            return "updated";
        } catch (JMSException ex) {
             Logger.getLogger(GuessingGame.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}

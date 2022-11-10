package EJBs;

import Entities.Vxz7784Strings;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Random;
import jakarta.ejb.Singleton;
import jakarta.persistence.Query;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;

/**
 * This singleton session bean administrates the guessing game by:
 * - Controlling which string users are guessing
 * - Determining whether a guess is correct or not
 * - Building Strings representing the current known String that is being guessed
 * @author Tamati Rudd 18045626
 */
@Singleton
public class GuessMaster {
    @PersistenceContext(unitName="GuessingGameServicePU") private EntityManager entityManager;
    public String currentString = "";
    Random rand = new Random();
    
    //Ensure a String is set for guessing when the EJB is initialized
    @PostConstruct
    public void chooseFirstString() {
        chooseNewString();
    }
    
    //Choose a new string from the database for users to guess
    public void chooseNewString() {
        //Query the DB for all not-guessed Strings
        Query query = entityManager.createNamedQuery("Vxz7784Strings.findAll");
        List<Vxz7784Strings> stringsList = query.getResultList();
        
        //Randomly set a new string to guess from the list
        currentString = stringsList.get(rand.nextInt(stringsList.size())).getString().toLowerCase();
    }
    
    //Get a fully hidden representation of the String to guess
    public String getStringHidden() {
        String hiddenString = "";
        for (char c : currentString.toCharArray()) {
            if (c == ' ')
                hiddenString += "  ";
            else
                hiddenString += "_ ";
        }
        return hiddenString;
    }

    //Process a User's String Guess, checking if it matches the string to be guessed
    public boolean processStringGuess(String userGuess) {
        if (userGuess.toLowerCase().equals(currentString)) {
            return true;
        }
        else
            return false;
    }
    
    //Process a User's character guess, checking for matches between the char they guessed and the string
    public ArrayList<Character> processCharGuess(ArrayList<Character> knownChars, char guess) {
        int index = 0;
        for (char c : currentString.toCharArray()) {
            if (knownChars.get(index) == '_') { //If a character is unknown (to the user)
                if (c == guess) //If character in String matches user guess
                    knownChars.set(index, guess); //Make character known
            }
            if (c != ' ') //Don't increment index on space
                index++;
        }
        return knownChars;
    }  
    
    //Get a String representation of the String that is known to the user
    public String getKnownString(ArrayList<Character> knownChars) {
        String knownString = "";
        int index = 0;
        for (char c : currentString.toCharArray()) {
            if (c == ' ') //Space - print a space, don't increment index
                knownString += "  ";
            else if (c == knownChars.get(index)) { //Known character - print it
                knownString += c+" ";
                index++;
            }
            else if (knownChars.get(index) == '_') { //Unknown character - print _ placeholder
                knownString += "_ ";
                index++;
            }
        }
        
        return knownString;
    }
}

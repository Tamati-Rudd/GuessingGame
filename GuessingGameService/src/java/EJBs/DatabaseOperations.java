package EJBs;

import Entities.Vxz7784Strings;
import Entities.Vxz7784Leaderboard;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.Query;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

/**
 * This stateless session bean performs database operations
 * @author Tamati Rudd 18045626
 */
@Stateless
public class DatabaseOperations {
    @PersistenceContext(unitName="GuessingGameServicePU") private EntityManager entityManager;

    //Add a new user to the leaderboard (database)
    public Vxz7784Leaderboard addNewUser(String username) {
        try {
            Vxz7784Leaderboard newUser = new Vxz7784Leaderboard(username);
            entityManager.persist(newUser);
            return newUser;
        } catch (Exception e) {
            Logger.getLogger(DatabaseOperations.class.getName()).log(Level.SEVERE, null, e);
            return null;
        } 
    }
    
    //Get a user from the leaderboard (database)
    public Vxz7784Leaderboard getUser(String username) {
        try {
            Query query = entityManager.createNamedQuery("Vxz7784Leaderboard.findByUsername");
            query.setParameter("username", username);
            Vxz7784Leaderboard foundUser = (Vxz7784Leaderboard) query.getSingleResult();
            return foundUser;
        }
        catch (NoResultException e) {
            return null;
        }
    }
    
    //Check whether a user exists in the leaderboard (database). Create the user if not
    public Vxz7784Leaderboard getOrAddUser(String username) {
        try {
            Query query = entityManager.createNamedQuery("Vxz7784Leaderboard.findByUsername");
            query.setParameter("username", username);
            Vxz7784Leaderboard foundUser = (Vxz7784Leaderboard) query.getSingleResult();
            return foundUser;
        }
        catch (NoResultException e) {
            return addNewUser(username); 
        }   
    }

    //Add a new String to the database of strings (Vxz7784Strings) that can be selected for guessing
    public boolean addNewString(String newGuessString, String submittedBy) {
        try {
            Vxz7784Strings newString = new Vxz7784Strings(newGuessString, submittedBy);
            entityManager.persist(newString);
            return true;
        } catch (EntityExistsException e) {
            return false;
        } 
    }
    
    //Update a user (Vxz7784Leadderboard record) to indicate they have guessed a String correctly
    public void registerCorrectGuess(String username, int points) {
        Vxz7784Leaderboard user = getUser(username);
        user.setPoints(user.getPoints()+points);
        user.setCorrectGuesses(user.getCorrectGuesses()+1);
        entityManager.merge(user);
    }
    
    //Assignment 4 Business Method
    //Get all strings from the database
    public ArrayList<String> getStrings() {
        Query query = entityManager.createQuery("SELECT v.string FROM Vxz7784Strings v");
        List<String> queryResult = query.getResultList();
        ArrayList<String> strings = new ArrayList<>();
        strings.addAll(queryResult);
        return strings;
    }
}

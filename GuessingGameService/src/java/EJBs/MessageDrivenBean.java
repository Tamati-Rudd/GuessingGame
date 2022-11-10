package EJBs;

import Entities.Vxz7784Leaderboard;
import jakarta.annotation.Resource;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.ObjectMessage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * This message driven bean performs database update operations following the completion of a Guessing Game Bluetooth versus match
 * @author Tamati Rudd 18045626
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "clientId", propertyValue = "jms/GuessingGameQueue"),
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/GuessingGameQueue"),
    @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable"),
    @ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "jms/GuessingGameQueue"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue")
})
public class MessageDrivenBean implements MessageListener {
    private Logger logger;
    @PersistenceContext(unitName="GuessingGameServicePU") private EntityManager entityManager;
    @Resource(mappedName = "jms/GuessingGameConnectionFactory") private ConnectionFactory connectionFactory;
   
    //Constructor
    public MessageDrivenBean() {
        logger = Logger.getLogger(getClass().getName());
    }
    
    //Receive a message
    @Override
    public void onMessage(Message message) {
        try {  
            if (message instanceof ObjectMessage) { //User lost: deduct 100 points
                //Read object fields
                ArrayList<String> userData = (ArrayList<String>) ((ObjectMessage) message).getObject();
                String name = userData.get(0);
                int pointChange = Integer.parseInt(userData.get(1));
                //Get entity
                Query query = entityManager.createNamedQuery("Vxz7784Leaderboard.findByUsername");
                query.setParameter("username", name);
                Vxz7784Leaderboard foundUser = (Vxz7784Leaderboard) query.getSingleResult();
                //Update database row
                logger.info("Performing database operations on "+name);
                foundUser.setPoints(foundUser.getPoints()+pointChange);
                if (pointChange > 0) //If gaining points, then guessed correctly
                    foundUser.setCorrectGuesses(foundUser.getCorrectGuesses()+1);
                entityManager.merge(foundUser);
            }
        } catch (JMSException e){
             logger.warning("Exception with incoming message: " + e);
        }
    }
}

package Entities;

import java.io.Serializable;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Entity that maps to a database table containing the leader board
 * Maps to table vxz7784_leaderboard
 * @author Tamati Rudd 18045626
 */
@Entity
@Table(name = "vxz7784_leaderboard")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Vxz7784Leaderboard.findAll", query = "SELECT v FROM Vxz7784Leaderboard v"),
    @NamedQuery(name = "Vxz7784Leaderboard.findByUsername", query = "SELECT v FROM Vxz7784Leaderboard v WHERE v.username = :username"),
    @NamedQuery(name = "Vxz7784Leaderboard.findByPoints", query = "SELECT v FROM Vxz7784Leaderboard v WHERE v.points = :points"),
    @NamedQuery(name = "Vxz7784Leaderboard.findByCorrectGuesses", query = "SELECT v FROM Vxz7784Leaderboard v WHERE v.correctGuesses = :correctGuesses")})
public class Vxz7784Leaderboard implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "username")
    private String username;
    @Basic(optional = false)
    @Column(name = "points")
    private int points;
    @Basic(optional = false)
    @Column(name = "correct_guesses")
    private int correctGuesses;

    //Constructors
    public Vxz7784Leaderboard() {
    }

    public Vxz7784Leaderboard(String username) {
        this.username = username;
    }

    public Vxz7784Leaderboard(String username, int points, int correctGuesses) {
        this.username = username;
        this.points = points;
        this.correctGuesses = correctGuesses;
    }

    //Accessors & Mutators
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getCorrectGuesses() {
        return correctGuesses;
    }

    public void setCorrectGuesses(int correctGuesses) {
        this.correctGuesses = correctGuesses;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (username != null ? username.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Vxz7784Leaderboard)) {
            return false;
        }
        Vxz7784Leaderboard other = (Vxz7784Leaderboard) object;
        if ((this.username == null && other.username != null) || (this.username != null && !this.username.equals(other.username))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Entities.Vxz7784Leaderboard[ username=" + username + " ]";
    }
    
}

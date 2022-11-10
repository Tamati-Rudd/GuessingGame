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
 * This Object represents a String that can be guessed in the Guessing Game
 * Maps to table Vxz7784_strings
 * @author Tamati Rudd 18045626
 */
@Entity
@Table(name = "vxz7784_strings")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Vxz7784Strings.findAll", query = "SELECT v FROM Vxz7784Strings v"),
    @NamedQuery(name = "Vxz7784Strings.findByString", query = "SELECT v FROM Vxz7784Strings v WHERE v.string = :string"),
    @NamedQuery(name = "Vxz7784Strings.findByGuessed", query = "SELECT v FROM Vxz7784Strings v WHERE v.guessed = :guessed"),
    @NamedQuery(name = "Vxz7784Strings.findBySubmittedBy", query = "SELECT v FROM Vxz7784Strings v WHERE v.submittedBy = :submittedBy")})
public class Vxz7784Strings implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "string")
    private String string;
    @Basic(optional = false)
    @Column(name = "guessed")
    private boolean guessed;
    @Column(name = "submitted_by")
    private String submittedBy;

    //Constructors
    public Vxz7784Strings() {
    }

    public Vxz7784Strings(String string) {
        this.string = string;
        this.guessed = false;
    }

    public Vxz7784Strings(String string, String submittedBy) {
        this.string = string;
        this.guessed = false;
        this.submittedBy = submittedBy;
    }

    //Accessors & Mutators
    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public boolean getGuessed() {
        return guessed;
    }

    public void setGuessed(boolean guessed) {
        this.guessed = guessed;
    }

    public String getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(String submittedBy) {
        this.submittedBy = submittedBy;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (string != null ? string.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Vxz7784Strings)) {
            return false;
        }
        Vxz7784Strings other = (Vxz7784Strings) object;
        if ((this.string == null && other.string != null) || (this.string != null && !this.string.equals(other.string))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Entities.Vxz7784Strings[ string=" + string + " ]";
    }
    
}

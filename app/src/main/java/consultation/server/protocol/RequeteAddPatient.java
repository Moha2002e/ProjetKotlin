package consultation.server.protocol;

import java.io.Serializable;

public class RequeteAddPatient implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String nom;
    private final String prenom;

    public RequeteAddPatient(String nom, String prenom) {
        this.nom = nom;
        this.prenom = prenom;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }
}

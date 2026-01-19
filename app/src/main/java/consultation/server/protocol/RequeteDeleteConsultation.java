package consultation.server.protocol;

import java.io.Serializable;

public class RequeteDeleteConsultation implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int consultationId;

    public RequeteDeleteConsultation(int consultationId) {
        this.consultationId = consultationId;
    }

    public int getConsultationId() {
        return consultationId;
    }
}

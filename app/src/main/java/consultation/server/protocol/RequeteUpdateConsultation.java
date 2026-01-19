package consultation.server.protocol;

import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

public class RequeteUpdateConsultation implements Serializable {
    private static final long serialVersionUID = 1L;
    private transient LocalDate newDate;
    private transient LocalTime newTime;
    private transient int consultationId;
    private transient Integer patientId;
    private transient String reason;
    private transient String newDateString;
    private transient String newTimeString;

    public RequeteUpdateConsultation(int consultationId, LocalDate newDate, LocalTime newTime, Integer patientId,
            String reason) {
        this.consultationId = consultationId;
        this.newDate = newDate;
        this.newTime = newTime;
        this.patientId = patientId;
        this.reason = reason;
        this.newDateString = newDate != null ? newDate.toString() : null;
        this.newTimeString = newTime != null ? newTime.toString() : null;
    }

    public RequeteUpdateConsultation(int consultationId, String newDateString, String newTimeString, Integer patientId,
            String reason) {
        this.consultationId = consultationId;
        this.patientId = patientId;
        this.reason = reason;
        this.newDateString = newDateString;
        this.newTimeString = newTimeString;
        if (newDateString != null && !newDateString.isEmpty()) {
            try {
                this.newDate = LocalDate.parse(newDateString);
            } catch (Exception e) {
                this.newDate = null;
            }
        } else {
            this.newDate = null;
        }
        if (newTimeString != null && !newTimeString.isEmpty()) {
            try {
                this.newTime = LocalTime.parse(newTimeString);
            } catch (Exception e) {
                this.newTime = null;
            }
        } else {
            this.newTime = null;
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        String dateStr = newDate != null ? newDate.toString() : null;
        String timeStr = newTime != null ? newTime.toString() : null;
        out.writeInt(consultationId);
        out.writeObject(patientId);
        out.writeObject(reason);
        out.writeObject(dateStr);
        out.writeObject(timeStr);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.consultationId = in.readInt();
        this.patientId = (Integer) in.readObject();
        this.reason = (String) in.readObject();
        String dateStr = (String) in.readObject();
        String timeStr = (String) in.readObject();

        this.newDateString = dateStr;
        this.newTimeString = timeStr;

        if (dateStr != null && !dateStr.isEmpty()) {
            try {
                this.newDate = LocalDate.parse(dateStr);
            } catch (Exception e) {
                this.newDate = null;
            }
        } else {
            this.newDate = null;
        }
        if (timeStr != null && !timeStr.isEmpty()) {
            try {
                this.newTime = LocalTime.parse(timeStr);
            } catch (Exception e) {
                this.newTime = null;
            }
        } else {
            this.newTime = null;
        }
    }

    public int getConsultationId() {
        return consultationId;
    }

    public LocalDate getNewDate() {
        if (newDate == null && newDateString != null && !newDateString.isEmpty()) {
            try {
                newDate = LocalDate.parse(newDateString);
            } catch (Exception e) {
            }
        }
        return newDate;
    }

    public LocalTime getNewTime() {
        if (newTime == null && newTimeString != null && !newTimeString.isEmpty()) {
            try {
                newTime = LocalTime.parse(newTimeString);
            } catch (Exception e) {
            }
        }
        return newTime;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public String getReason() {
        return reason;
    }
}

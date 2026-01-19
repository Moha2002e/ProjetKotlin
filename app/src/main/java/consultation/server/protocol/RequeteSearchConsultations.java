package consultation.server.protocol;

import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.time.LocalDate;

public class RequeteSearchConsultations implements Serializable {
    private static final long serialVersionUID = 1L;
    private transient LocalDate fromDate;
    private transient LocalDate toDate;
    private transient Integer doctorId;
    private transient Integer patientId;
    private transient String fromDateString;
    private transient String toDateString;

    public RequeteSearchConsultations(Integer doctorId, Integer patientId, LocalDate fromDate, LocalDate toDate) {
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.fromDateString = fromDate != null ? fromDate.toString() : null;
        this.toDateString = toDate != null ? toDate.toString() : null;
    }

    public RequeteSearchConsultations(Integer doctorId, Integer patientId, String fromDateString, String toDateString) {
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.fromDateString = fromDateString;
        this.toDateString = toDateString;
        if (fromDateString != null && !fromDateString.isEmpty()) {
            try {
                this.fromDate = LocalDate.parse(fromDateString);
            } catch (Exception e) {
                this.fromDate = null;
            }
        } else {
            this.fromDate = null;
        }
        if (toDateString != null && !toDateString.isEmpty()) {
            try {
                this.toDate = LocalDate.parse(toDateString);
            } catch (Exception e) {
                this.toDate = null;
            }
        } else {
            this.toDate = null;
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        String fromStr = fromDate != null ? fromDate.toString() : null;
        String toStr = toDate != null ? toDate.toString() : null;
        out.writeObject(doctorId);
        out.writeObject(patientId);
        out.writeObject(fromStr);
        out.writeObject(toStr);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.doctorId = (Integer) in.readObject();
        this.patientId = (Integer) in.readObject();
        String fromStr = (String) in.readObject();
        String toStr = (String) in.readObject();

        this.fromDateString = fromStr;
        this.toDateString = toStr;

        if (fromStr != null && !fromStr.isEmpty()) {
            try {
                this.fromDate = LocalDate.parse(fromStr);
            } catch (Exception e) {
                this.fromDate = null;
            }
        } else {
            this.fromDate = null;
        }
        if (toStr != null && !toStr.isEmpty()) {
            try {
                this.toDate = LocalDate.parse(toStr);
            } catch (Exception e) {
                this.toDate = null;
            }
        } else {
            this.toDate = null;
        }
    }

    public Integer getDoctorId() {
        return doctorId;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public LocalDate getFromDate() {
        if (fromDate == null && fromDateString != null && !fromDateString.isEmpty()) {
            try {
                fromDate = LocalDate.parse(fromDateString);
            } catch (Exception e) {
            }
        }
        return fromDate;
    }

    public LocalDate getToDate() {
        if (toDate == null && toDateString != null && !toDateString.isEmpty()) {
            try {
                toDate = LocalDate.parse(toDateString);
            } catch (Exception e) {
            }
        }
        return toDate;
    }
}

package consultation.server.protocol;

import java.io.Serializable;

public class ReponseTraitee implements Serializable {
    private static final long serialVersionUID = -5437301116538973669L;

    private final boolean success;
    private final String message;
    private final Object data;

    public ReponseTraitee(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }
}

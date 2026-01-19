package consultation.server.protocol;

import java.io.Serializable;

public class RequeteLogin implements Serializable {
    private static final long serialVersionUID = -5968792954984562726L;

    private final String login;
    private final String password;

    public RequeteLogin(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}

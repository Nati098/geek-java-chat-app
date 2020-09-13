package data;

public enum Commands {
    REG("/reg"),
    REG_OK("/regok"),
    REG_NO("/regno"),
    AUTH("/auth"),
    AUTHOK("/authok"),
    END("/end"),
    PRIVATE_MSG("/w"),
    CLIENTS_LIST("/clientlist");

    private String cmd;

    Commands(String cmd) {
        this.cmd = cmd;
    }

    public String getCommand() {
        return cmd;
    }
}

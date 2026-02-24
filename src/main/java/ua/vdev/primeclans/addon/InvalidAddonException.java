package ua.vdev.primeclans.addon;

public class InvalidAddonException extends Exception {

    public InvalidAddonException(String message) {
        super(message);
    }

    public InvalidAddonException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidAddonException(Throwable cause) {
        super(cause);
    }
}
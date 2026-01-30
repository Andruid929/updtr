package net.druidlabs.updtr.api.requests;

public class RequestException extends RuntimeException {

    private final String message;

    public RequestException(Exception e, String message) {
        super(e);

        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

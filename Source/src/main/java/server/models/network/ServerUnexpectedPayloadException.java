package server.models.network;

public class ServerUnexpectedPayloadException extends ServerException {

    public ServerUnexpectedPayloadException(String msg) {
        super(msg);
    }

    public ServerUnexpectedPayloadException() {
        super();
    }
}
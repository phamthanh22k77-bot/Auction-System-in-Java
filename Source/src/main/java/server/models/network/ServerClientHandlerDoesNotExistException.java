package server.models.network;

public class ServerClientHandlerDoesNotExistException extends ServerException {

    public ServerClientHandlerDoesNotExistException(String msg) {
        super(msg);
    }

    public ServerClientHandlerDoesNotExistException() {
        super();
    }
}
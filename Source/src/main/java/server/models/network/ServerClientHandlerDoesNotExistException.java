package Server.ServerException;

public class ServerClientHandlerDoesNotExistException extends ServerException {

    public ServerClientHandlerDoesNotExistException(String msg) {
        super(msg);
    }

    public ServerClientHandlerDoesNotExistException() {
        super();
    }
}
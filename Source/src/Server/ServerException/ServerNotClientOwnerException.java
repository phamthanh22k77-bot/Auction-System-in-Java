package Server.ServerException;

public class ServerNotClientOwnerException extends ServerException {

    public ServerNotClientOwnerException(String msg) {
        super(msg);
    }

    public ServerNotClientOwnerException() {
        super();
    }
}
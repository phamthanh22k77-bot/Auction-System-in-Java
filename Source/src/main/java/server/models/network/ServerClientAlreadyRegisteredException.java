package Server.ServerException;

public class ServerClientAlreadyRegisteredException extends ServerException {

    public ServerClientAlreadyRegisteredException(String msg) {
        super(msg);
    }

    public ServerClientAlreadyRegisteredException() {
        super();
    }
}
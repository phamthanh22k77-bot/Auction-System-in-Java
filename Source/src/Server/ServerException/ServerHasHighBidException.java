package Server.ServerException;

public class ServerHasHighBidException extends ServerException {

    public ServerHasHighBidException(String msg) {
        super(msg);
    }

    public ServerHasHighBidException() {
        super();
    }
}
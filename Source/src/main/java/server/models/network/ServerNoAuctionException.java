package server.models.network;

public class ServerNoAuctionException extends ServerException {

    public ServerNoAuctionException(String msg) {
        super(msg);
    }

    public ServerNoAuctionException() {
        super();
    }
}
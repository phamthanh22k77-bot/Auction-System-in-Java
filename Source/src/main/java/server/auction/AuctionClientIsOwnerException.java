package server.auction;

public class AuctionClientIsOwnerException extends AuctionException {

    public AuctionClientIsOwnerException(String msg) {
        super(msg);
    }

    public AuctionClientIsOwnerException() {
        super();
    }
}
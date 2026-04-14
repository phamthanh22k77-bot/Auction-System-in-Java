package server.auction;

public class AuctionNotRegisteredException extends AuctionException {

    public AuctionNotRegisteredException(String msg) {
        super(msg);
    }

    public AuctionNotRegisteredException() {
        super();
    }
}
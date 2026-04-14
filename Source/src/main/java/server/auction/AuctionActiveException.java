package server.auction;

public class AuctionActiveException extends AuctionException {

    public AuctionActiveException(String msg) {
        super(msg);
    }

    public AuctionActiveException() {
        super();
    }
}
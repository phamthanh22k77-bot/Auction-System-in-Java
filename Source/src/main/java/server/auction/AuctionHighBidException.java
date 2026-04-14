package server.auction;

public class AuctionHighBidException extends AuctionException {

    public AuctionHighBidException(String msg) {
        super(msg);
    }

    public AuctionHighBidException() {
        super();
    }
}
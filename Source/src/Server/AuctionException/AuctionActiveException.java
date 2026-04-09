package Server.AuctionException;

public class AuctionActiveException extends AuctionException {

    public AuctionActiveException(String msg) {
        super(msg);
    }

    public AuctionActiveException() {
        super();
    }
}
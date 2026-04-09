package Server.AuctionException;

public class AuctionAlreadyRegisteredException extends AuctionException {

    public AuctionAlreadyRegisteredException(String msg) {
        super(msg);
    }

    public AuctionAlreadyRegisteredException() {
        super();
    }
}
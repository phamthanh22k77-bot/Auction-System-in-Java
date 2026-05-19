package server.auction;

public enum AuctionStatus {
    OPEN, RUNNING, FINISHED, PAID, CANCELED
}
// Trạng thái của phiên đấu giá.
// Vòng đời: OPEN -> RUNNING -> FINISHED -> PAID -> CANCELED


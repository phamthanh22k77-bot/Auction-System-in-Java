Note lỗi:
-1. Khi user mới đăng nhập vào chưa load được lịch sử đặt giá của phiên đấu giá
-2. Giá tiền không thay đổi trong màn hình seller khi có người khác bid 
-3. Khi có người dùng mới tạo tài khoản thì không hiện trong danh sách user của admin, chỉ hiện sau khi admin đăng xuất rồi đăng nhập lại
-4. Graph khi đặt giá của các bên client không được lưu lại giống nhau

Note khắc phục:
-1. Định nghĩa thêm REQUEST_BID_HISTORY / SEND_BID_HISTORY vào MessageType.java.
Chỉnh sửa ClientHandler.java để tiếp nhận yêu cầu và gửi trả lại lịch sử đấu giá in-memory thực tế từ Server qua Socket.

Note cho Thảo:
Chỉnh sửa ItemDetailController.java và BiddingController.java để yêu cầu và nhận lịch sử không đồng bộ qua Socket thay vì gọi trực tiếp DAO cục bộ.

-2. Chỉnh sửa ClientHandler.java (dòng 853-854).
Gọi thêm broadcastAuctionList() ngay sau khi xử lý bid thành công để phát sóng tín hiệu SEND_ACTIVE_AUCTION_LIST cho tất cả Client đang online.
Dashboard của Seller tự động bắt được tín hiệu phát sóng này và làm mới (Refresh) bảng sản phẩm với giá tiền mới nhất theo thời gian thực (Real-time).

-3. Chỉnh sửa ClientHandler.java.
Định nghĩa thêm phương thức broadcastToAdmins() để phát sóng tín hiệu SEND_ALL_USERS chứa danh sách tài khoản mới cập nhật tới tất cả các Admin đang online.
Gọi broadcastToAdmins() ngay sau khi xử lý đăng ký tài khoản mới thành công (trong handleRegister) hoặc khi thay đổi trạng thái khóa tài khoản (trong handleToggleUserLock).
Admin Dashboard tự động bắt tín hiệu phát sóng này và làm mới danh sách người dùng hiển thị trên màn hình quản trị theo thời gian thực (Real-time).

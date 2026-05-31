# Hệ Thống Đấu Giá Trực Tuyến (Online Auction System)

> **Dự án Bài Tập Lớn môn Lập Trình Nâng Cao**
> **Trường Đại học Công nghệ - ĐHQGHN (UET - VNU)**
> **Kiến trúc: Client-Server qua TCP Socket & Mô hình MVC JavaFX**

🔗 **[Link Báo Cáo Tổng Kết (PDF)](./Báo%20Cáo%20-%20Hệ%20Thống%20Đấu%20Giá%20Trực%20Tuyến.pdf)** : **https://drive.google.com/file/d/1RQjZZXa--dfV5x1xaBLFkGcrI16twpHz/view?usp=sharing**

 🎥 **[Link Video Demo](#)  : https://drive.google.com/file/d/1Ax8bCFBMpwOvnPlQOnbONWJ2R_TudYE7/view?usp=sharing**

---

## 1. Mô tả bài toán và Phạm vi hệ thống

### Mô tả bài toán:

Hệ thống mô phỏng một sàn giao dịch đấu giá trực tuyến thời gian thực (tương tự như eBay), cho phép nhiều người dùng từ các máy tính khác nhau kết nối mạng và đồng thời tham gia đấu thầu sản phẩm. Hệ thống tự động xác định người chiến thắng khi hết thời gian đếm ngược và đảm bảo tính an toàn tài chính cũng như tính đồng bộ dữ liệu đa luồng.

### Phạm vi hệ thống (3 Vai trò chính):

* **Bidder (Người đặt giá):** Đăng ký, đăng nhập, xem danh sách phiên đang chạy, nạp tiền vào tài khoản, đặt giá thầu thời gian thực (Make Bid), theo dõi biến động biểu đồ giá và lịch sử thầu trực quan.
* **Seller (Người bán):** Tạo sản phẩm đấu giá thuộc 3 danh mục (Đồ điện tử, Nghệ thuật, Xe cộ), thiết lập thời gian bắt đầu/kết thúc, giá khởi điểm, bước nhảy thầu tối thiểu; quản lý và có quyền hủy phiên đấu giá của mình trước khi kết thúc.
* **Admin (Quản trị viên):** Xem danh sách toàn bộ người dùng và lịch sử thầu hệ thống; có quyền khóa/mở khóa tài khoản người dùng ngay lập tức (cưỡng chế ngắt kết nối nếu tài khoản bị khóa đang online).

---

## 2. Tính năng & Phân công công việc

Dự án được chia làm 4 mảng công việc lớn, giao cụ thể cho từng thành viên:

### 👤 Thành — OOP Design & Architecture Lead

* Xây dựng **Class Diagram**: Các thực thể `Entity`, `User` (Bidder/Seller/Admin), `Item` (Electronics/Art/Vehicle), `Auction`, `BidTransaction`.
* Áp dụng 4 nguyên tắc OOP (Đóng gói, Kế thừa, Đa hình, Trừu tượng).
* Implement **Factory Method Pattern** để khởi tạo các loại `Item`.
* Quản lý GitHub, Conventional Commits, viết file báo cáo quá trình làm việc.

### ⚙️ Kiệt — Backend, Business Logic & Concurrency

* Thiết kế Data Access Object (DAO) lưu trữ dữ liệu.
* Quản lý CRUD Sản phẩm và Logic phiên đấu (Chuyển trạng thái OPEN → RUNNING → FINISHED → PAID/CANCELED).
* Trọng tâm xử lý **Đa luồng (Concurrency)**: Xử lý đấu giá đồng thời để tránh Lost Update (bằng `synchronized` hoặc `ReentrantLock`).
* Implement **Singleton Pattern** cho `AuctionManager`.
* **Tính năng nâng cao (0,5đ)**:Thuật toán `Anti-sniping` gia hạn giờ phút chót.
* Viết Unit Test (JUnit).

### 🌐 Hoàng — Networking & Real-time Communication

* Thiết kế mạng Client–Server bằng Socket TCP.
* Implement **Observer Pattern** (phía server) để notify toàn bộ client khi có Bid mới.
* Xử lý Đa luồng Socket: Multi-thread per client, xử lý khi client ngắt kết nối.
* Thiết lập CI/CD chạy test tự động.
* Áp dụng chuẩn code Google Java Style.

### 🖥️ Thảo — Frontend JavaFX & Visualization

* Áp dụng kiến trúc MVC cho giao diện (JavaFX + FXML + Controllers).
* Thiết kế Form đa dạng phân quyền: Màn hình Login/Register, Màn hình Bidder, Màn hình Seller, Màn hình Admin.
* Tích hợp Server-Client real-time lên giao diện.
* **Tính năng nâng cao (0.5đ)**: Visualization bằng Line Chart thể hiện lịch sử giá trị của phiên đấu giá theo thời gian thực.

---

## 3. Công nghệ sử dụng & Yêu cầu hệ thống

### Công nghệ sử dụng:

* **Ngôn ngữ chính:** Java 17+
* **Giao diện:** JavaFX 21 + FXML (MVC Pattern)
* **Giao tiếp mạng:** TCP Socket, Java Object Serialization (truyền nhận PacketMessage định dạng sẵn)
* **Cơ sở dữ liệu:** Lưu trữ dạng file cấu trúc JSON (đọc/ghi qua Gson thư viện)
* **Công cụ build:** Maven 3.9+
* **Kiểm thử:** JUnit 5 cho logic nghiệp vụ

### Yêu cầu cài đặt môi trường chạy:

* Đã cài đặt JDK 17 trở lên và cấu hình biến môi trường `JAVA_HOME`.
* Đã cài đặt Apache Maven (để tự động biên dịch và nạp thư viện JavaFX).

---

## 4. Cấu trúc thư mục (Module chính)

```
Auction-System-in-Java/
├── Helpful Docs/                  # Chứa tài liệu thiết kế hệ thống
│   ├── Class Diagram OOP.drawio   # Bản vẽ thiết kế Class Diagram OOP (Thành)
│   └── Design Pattern.drawio      # Bản vẽ sơ đồ áp dụng Singleton, Factory, Observer
└── Source/
    ├── src/main/java/
    │   ├── client/                # Mã nguồn phía Client (Hoàng, Thảo)
    │   │   ├── controllers/       # Lớp điều khiển xử lý sự kiện giao diện FXML
    │   │   ├── message/           # Chứa PacketMessage, MessageType chung
    │   │   ├── network/           # Kết nối mạng TCP Socket phía Client
    │   │   └── views/             # Giao diện người dùng thiết kế FXML
    │   └── server/                # Mã nguồn phía Server (Thành, Kiệt, Hoàng)
    │       ├── auction/           # Quản lý đấu giá (AuctionManager, ItemFactory, Observers)
    │       ├── dao/               # Tương tác Database file JSON (UserDAO, AuctionDAO...)
    │       ├── models/            # Thực thể Entity, User, Item, Auction, BidTransaction
    │       ├── network/           # Socket Server lắng nghe kết nối, ClientHandler đa luồng
    │       └── payload/           # Các Payload đóng gói dữ liệu truyền qua Socket
    └── pom.xml                    # File cấu hình Maven Dependencies (JavaFX, Gson, JUnit)
```

---

## 5. Vị trí các file .jar sau khi đóng gói

Khi bạn thực hiện đóng gói dự án thông qua Maven (sử dụng **IntelliJ Maven tool** -> chọn **clean** -> **package**), hệ thống sẽ biên dịch và tạo ra các file `.jar` có khả năng tự chạy (Fat JAR) tại thư mục sau:

* **Thư mục chứa file `.jar`:** `Auction-System-in-Java/Source/target/`
* **Các file `.jar` được tạo ra:**
  * `AuctionServer.jar`: Bản build đóng gói đầy đủ phía Server (chứa sẵn thư viện Gson).
  * `AuctionClient.jar`: Bản build đóng gói đầy đủ phía Client (chứa sẵn thư viện JavaFX và Gson).

---

## 6. Hướng dẫn đóng gói và chạy hệ thống bằng Terminal

Để chạy hệ thống ổn định và tải được dữ liệu tài khoản/phiên đấu giá từ file JSON, bạn cần tuân thủ thứ tự: **Khởi động Server trước, sau đó khởi động các Client**.

### Bước 1: Đóng gói dự án (Nếu có thay đổi mã nguồn)

1. Mở **IntelliJ IDEA**.
2. Mở tab **Maven** ở góc phải màn hình.
3. Chọn dự án `BigProjectAuctionSystem` -> click đúp vào **clean**, sau đó click đúp vào **package**.
4. Maven sẽ tự động build và tạo ra 2 file `AuctionServer.jar` và `AuctionClient.jar` bên trong thư mục `Source/target`.

### Bước 2: Chạy Server (Bắt buộc chạy trước)

**Cách chạy chuẩn xác nhất:** Mở terminal tại thư mục **`Source`** và chạy lệnh:

```bash
# Đứng tại thư mục Source để chạy Server
java -jar target/AuctionServer.jar
```

### Bước 3: Chạy Client (Chạy sau khi Server đã mở cổng)

Bạn có thể mở nhiều cửa sổ terminal khác nhau để khởi động nhiều Client cùng lúc (mỗi Client đại diện cho một người tham gia đấu giá).

* Mở terminal tại thư mục **`Source`** và chạy lệnh:

```bash
# Đứng tại thư mục Source để chạy Client
java -jar target/AuctionClient.jar
```

### # Chạy hệ thống trên nhiều máy tính khác nhau (Mạng LAN)


**Yêu cầu bắt buộc:** Máy chạy Server và các máy tính chạy Client phải kết nối chung một mạng Wi-Fi (hoặc chung mạng LAN).

*(Phần hướng dẫn chi tiết đang được cập nhật...)*

---

## 7. Danh sách chức năng đã hoàn thành

### Nhóm chức năng bắt buộc (Mandatory - Hoàn thành 100%):

* **Đăng ký / Đăng nhập & Phân quyền:** Hỗ trợ 3 vai trò: Bidder, Seller, Admin. Phân luồng giao diện FXML động theo quyền hạn.
* **Quản lý người dùng:** Khóa/mở khóa tài khoản thời gian thực (Admin).
* **Quản lý sản phẩm (CRUD):** Thêm, sửa, xóa sản phẩm thuộc 3 danh mục khác nhau.
* **Nghiệp vụ Đấu giá Realtime:** Người dùng đặt giá, hệ thống kiểm duyệt 3 quy tắc thầu an toàn và cập nhật giá lập tức tới toàn bộ phòng đấu thầu bằng Socket Broadcast.
* **Xử lý đa luồng an toàn (Concurrency):** Sử dụng khối đồng bộ mịn `synchronized(auction)` để ngăn chặn triệt để lỗi **Lost Update** và **Race Condition** khi nhiều người cùng bid 1 sản phẩm.
* **Tự động kết thúc phiên:** Sử dụng bộ quét định kỳ (Scheduler) quét 1 giây/lần ở Server để tự cập nhật trạng thái phiên thầu, khóa đặt giá và thông báo người chiến thắng.
* **Lưu trữ dữ liệu:** Persist dữ liệu an toàn dạng file JSON (`users.json`, `auctions.json`, `items.json`) qua DAO Pattern.

### Nhóm chức năng nâng cao (Advanced):

* **Thuật toán Anti-Sniping (Chống bắn tỉa giây cuối):** Tự động gia hạn thêm 30 giây thời gian đếm ngược nếu có lượt bid phát sinh trong 30 giây cuối cùng của phiên (giới hạn tối đa 5 lần gia hạn).
* **Trực quan hóa diễn biến giá (Visualization):** Vẽ biểu đồ đường (`Line Chart`) thể hiện diễn biến giá thầu tăng dần theo thời gian thực mà không cần tải lại trang.

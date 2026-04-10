# 🏆 Online Auction System

> **Hệ Thống Đấu Giá Trực Tuyến** — Bài Tập Lớn Lập Trình Nâng Cao
> UET — Nhóm 4 người

<div align="center">

![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?style=for-the-badge&logo=java&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.9+-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)
![JUnit](https://img.shields.io/badge/JUnit5-5.10-25A162?style=for-the-badge&logo=junit5&logoColor=white)
![Socket](https://img.shields.io/badge/TCP_Socket-Protocol-orange?style=for-the-badge)

</div>

---

## 📌 Mô tả dự án

Hệ thống đấu giá trực tuyến cho phép **nhiều người dùng** tham gia đấu giá các mặt hàng theo **thời gian thực** thông qua kết nối mạng mạng Client-Server (TCP/Socket). Hệ thống phân quyền chặt chẽ với 3 vai trò: **Bidder** (người đặt giá), **Seller** (người bán) và **Admin** (quản trị viên).

---

## ✨ Tính năng & Phân công công việc

Dự án được chia làm 4 mảng công việc lớn, giao cụ thể cho từng thành viên:

### 👤 Thành — OOP Design & Architecture Lead
- Xây dựng **Class Diagram**: Các thực thể `Entity`, `User` (Bidder/Seller/Admin), `Item` (Electronics/Art/Vehicle), `Auction`, `BidTransaction`.
- Áp dụng 4 nguyên tắc OOP (Đóng gói, Kế thừa, Đa hình, Trừu tượng).
- Implement **Factory Method Pattern** để khởi tạo các loại `Item`.
- Quản lý GitHub, Conventional Commits, viết file báo cáo quá trình làm việc.

### ⚙️ Kiệt — Backend, Business Logic & Concurrency
- Thiết kế Data Access Object (DAO) lưu trữ dữ liệu.
- Quản lý CRUD Sản phẩm và Logic phiên đấu (Chuyển trạng thái OPEN → RUNNING → FINISHED → PAID/CANCELED).
- Trọng tâm xử lý **Đa luồng (Concurrency)**: Xử lý đấu giá đồng thời để tránh Lost Update (bằng `synchronized` hoặc `ReentrantLock`).
- Implement **Singleton Pattern** cho `AuctionManager`.
- **Tính năng nâng cao (1đ)**: Thiết kế Logic `Auto-Bidding` bằng PriorityQueue và thuật toán `Anti-sniping` gia hạn giờ phút chót.
- Viết Unit Test (JUnit).

### 🌐 Hoàng — Networking & Real-time Communication
- Thiết kế mạng Client–Server bằng Socket TCP.
- Implement **Observer Pattern** (phía server) để notify toàn bộ client khi có Bid mới.
- Xử lý Đa luồng Socket: Multi-thread per client, xử lý khi client ngắt kết nối.
- Thiết lập CI/CD chạy test tự động.
- Áp dụng chuẩn code Google Java Style.

### 🖥️ Thảo — Frontend JavaFX & Visualization
- Áp dụng kiến trúc MVC cho giao diện (JavaFX + FXML + Controllers).
- Thiết kế Form đa dạng phân quyền: Màn hình Login/Register, Màn hình Bidder, Màn hình Seller, Màn hình Admin.
- Tích hợp Server-Client real-time lên giao diện.
- **Tính năng nâng cao (0.5đ)**: Visualization bằng Line Chart thể hiện lịch sử giá trị của phiên đấu giá theo thời gian thực.

---

## 🔒 Cơ chế phiên đấu giá & Anti-Sniping

- **Đóng phiên cơ bản**: Một phiên chạy theo khoảng **thời gian cố định** (Time-based). Người có giá cao nhất (`currentHighestBid`) khi thời gian đếm ngược kết thúc (`endTime`) sẽ là Winner.
- **Thuật toán Anti-sniping (Kiệt phụ trách)**: Nếu có bất kỳ người dùng nào đặt giá (`Bid`) ở những giây X cuối cùng của phiên đấu giá, hệ thống tự động **cộng thêm (gia hạn)** thời gian đếm ngược. Mục đích chặn những người dùng rình mò đợi giây cuối mới tung giá (snipers).

---

## 🏗️ Kiến trúc hệ thống

```
┌─────────────────────────────────────────────────────┐
│                   CLIENT SIDE                       │
│  JavaFX UI  ──►  Controller  ──►  Network Layer     │
│  (FXML Views)    (MVC)            (Socket Client)   │
└─────────────────────┬───────────────────────────────┘
                      │  TCP Socket
                      │  Protocol: Object / JSON
┌─────────────────────▼───────────────────────────────┐
│                   SERVER SIDE                       │
│  ServerSocket  ──►  ClientHandler (Thread)          │
│                          │                          │
│                     AuctionManager ◄── Singleton    │
│                          │                          │
│                     Auction (Subject) ──► Observer  │
│                          │                          │
│                  Business Logic Layer               │
│                  (Validate, Concurrency)            │
└─────────────────────────────────────────────────────┘
```

---

## 📡 Giao thức truyền tin (Application Protocol)
*Theo thiết kế trong file `Se vờ 3.drawio`*

Hệ thống giao tiếp thông qua Message Object truyền qua TCP với các Lệnh Cốt Lõi được thiết kế như sau:

### Lệnh gửi từ Client đến Server:
- `CREATE_AUCTION`: Seller gửi yêu cầu tạo một mặt hàng đấu giá mới.
- `REQUEST_ACTIVE_AUCTION_LIST`: Lấy danh sách các phiên đang mở.
- `REGISTER_IN_AUCTION`: Ghi danh làm thành viên tham dự vào 1 phiên đấu giá cụ thể.
- `UNREGISTER_FROM_AUCTION` / `CANCEL_AUCTION`: Rút danh tham dự hoặc hủy phiên.
- `MAKE_BID`: Thực hiện nâng giá (Cần Validate của Người 2).
- `REQUEST_HIGHER_BID` / `REQUEST_AUCTION`: Truy vấn thông tin giá / thông tin cấu trúc phiên.
- `DISCONNECT`: Thoát phiên làm việc.

### Lệnh phản hồi từ Server về Client:
- Các sự kiện Broadcast (Báo về mọi người): `JOIN`, `BID` (có người mới nâng quá), `LEAVE`.
- `ERROR`: Ném trả lỗi hệ thống (như `AuctionLowBidException` nếu đặt giá thấp hơn hiện tại, `AuctionAlreadyRegisteredException`, `ServerHasHighBidException`...).

---

## 📐 Class Diagram OOP
*Bám sát file `Class Diagram OOP.drawio`*

```
Entity (abstract)
├── User (abstract)           [username, email, password]
│   ├── Bidder                [balance]
│   ├── Seller                [companyName, rating]
│   └── Admin                 [department]
├── Item (abstract)           [name, description, startingPrice, currentPrice]
│   ├── Electronics           [warranty, brand, model]
│   ├── Art                   [artist, medium, year]
│   └── Vehicle               [engineType, modelYear, mileage, licensePlate]
├── Auction                   [item, seller, startTime, endTime, status, winner]
└── BidTransaction            [bidder, auctionId, amount, timestamp]

AutoBid                       [maxBid, bidStep, isActive]
Enum AuctionStatus            [OPEN, RUNNING, FINISHED, PAID, CANCELED]
```

**Mẫu thiết kế áp dụng (Design Patterns):**
1. **Factory Method** (`ItemFactory`): Tự động tạo `Electronics`, `Art` hoặc `Vehicle` không cần new trực tiếp đối tượng class.
2. **Singleton** (`AuctionManager`): Giữ tính đồng nhất bộ nhớ ở tầng quản lý phiên cấm conflict Đa luồng.
3. **Observer** (`BidObserver` / `SendPacket`): Tự động lắng nghe và Notify lên các `ClientHandler`.

---

## 📦 Cấu trúc thư mục (Chuẩn hóa)

```
Auction-System-in-Java/
├── Helpful Docs/                  # Chứa tài liệu thiết kế
│   ├── Class Diagram OOP.drawio   # Bản vẽ thiết kế OOP (Thành)
│   ├── Design Pattern.drawio      # Bản vẽ sơ đồ mẫu thiết kế
│   └── Se vờ 3.drawio             # Sơ đồ Networking
└── Source/
    └── src/main/java/
        ├── client/                # Hoàng, Thảo
        │   ├── controllers/       
        │   ├── network/           
        │   └── views/             
        ├── common/                # Shared Protocol (Ai cũng đọc)
        │   ├── protocol/          
        │   └── utils/             
        └── server/                # Thành, Kiệt, Hoàng
            ├── auction/           # AuctionManager, ItemFactory (Design Patterns)
            ├── dao/               # Tương tác Database
            ├── models/            # Entity, User, Bidder, Admin, Item...
            └── network/           # Socket Server, ClientHandler
```

---

## 🚀 Hướng dẫn cài đặt & chạy

### Yêu cầu hệ thống
- Java 17+ (OpenJDK 17 hoặc mới hơn)
- Maven 3.9+
- JavaFX 21

### Clone project
```bash
git clone https://github.com/phamthanh22k77-bot/Auction-System-in-Java.git
cd Auction-System-in-Java/Source
```

### Build project
```bash
mvn clean install
```

### Chạy Server (Mảng của Kiệt & Hoàng)
```bash
mvn exec:java -Dexec.mainClass="server.network.AuctionServer" -Dexec.args="9999"
```

### Chạy Client UI (Mảng của Thảo)
```bash
mvn exec:java -Dexec.mainClass="client.MainApp" -Dexec.args="127.0.0.1 9999"
```

---

## 📄 Giấy phép

Dự án phục vụ mục đích học tập — UET, môn Lập Trình Nâng Cao.
hi
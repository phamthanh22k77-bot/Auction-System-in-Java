<<<<<<< HEAD
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

Hệ thống đấu giá trực tuyến cho phép **nhiều người dùng** tham gia đấu giá các mặt hàng theo **thời gian thực** thông qua kết nối mạng TCP/Socket. Hệ thống hỗ trợ 3 vai trò: **Bidder** (người đặt giá), **Seller** (người bán) và **Admin** (quản trị viên), đồng thời phân biệt người dùng **Cá nhân (Individual)** và **Tổ chức (Organization)**.

---

## ✨ Tính năng chính

### 👤 Phía Client (Người dùng)

| Tính năng | Mô tả | Trạng thái |
|---|---|---|
| 🔐 Đăng ký / Đăng nhập | Phân quyền theo vai trò (Bidder / Seller / Admin), hỗ trợ cá nhân & tổ chức | 🔧 Đang phát triển |
| 🌐 Kết nối Server | Kết nối TCP đến server, nhận thông báo chào khi kết nối thành công | 🔧 Đang phát triển |
| 🛒 Đăng sản phẩm đấu giá | Seller thêm sản phẩm (Electronics, Art, Vehicle) với giá khởi điểm & kiểu đóng phiên | 🔧 Đang phát triển |
| 📋 Xem danh sách phiên | Lấy danh sách tất cả phiên đấu giá đang hoạt động từ server | 🔧 Đang phát triển |
| 📝 Đăng ký tham gia | Đăng ký vào một phiên đấu giá cụ thể theo `auctionId` | 🔧 Đang phát triển |
| 💰 Đặt giá (Bid) | Đặt giá cao hơn giá hiện tại, server lưu thời điểm đặt giá | 🔧 Đang phát triển |
| 🔍 Kiểm tra giá cao nhất | Truy vấn giá cao nhất hiện tại và thời điểm đặt của một phiên | 🔧 Đang phát triển |
| 🚪 Rút khỏi phiên | Rút khỏi phiên đang tham gia (trừ khi đang giữ giá cao nhất) | 🔧 Đang phát triển |
| 🤖 Auto-Bidding | Tự động đặt giá theo mức tối đa & bước giá đã cài sẵn | 🔧 Đang phát triển |
| 🔌 Ngắt kết nối | Ngắt kết nối an toàn (trừ khi đang giữ giá cao nhất ở phiên nào đó) | 🔧 Đang phát triển |

### 🖥️ Phía Server

| Tính năng | Mô tả | Trạng thái |
|---|---|---|
| 🔔 Xử lý kết nối | Chấp nhận kết nối, gửi thông báo chào đến client mới | 🔧 Đang phát triển |
| ⚡ Cập nhật Bid real-time | Thông báo tức thì đến toàn bộ người tham gia khi có bid mới | 🔧 Đang phát triển |
| 🏁 Đóng phiên đấu giá | Thông báo người thắng, giá cuối cùng đến tất cả participants | 🔧 Đang phát triển |
| 🛡️ Anti-Sniping | Gia hạn phiên tự động khi có bid trong X giây cuối | 🔧 Đang phát triển |
| 🔒 Kiểm soát ngắt kết nối | Từ chối ngắt kết nối nếu client đang giữ giá cao nhất | 🔧 Đang phát triển |
| 🔄 Đa luồng (Multi-thread) | Mỗi client được xử lý bởi một thread riêng | 🔧 Đang phát triển |
| 🧪 Unit Testing | JUnit 5 + Concurrent Test cho logic đấu giá | 🔧 Đang phát triển |

---

## 🔒 Cơ chế đóng phiên đấu giá

Hệ thống hỗ trợ **2 kiểu đóng phiên**, do Seller lựa chọn khi tạo phiên:

### Kiểu 1 — Đóng theo thời gian (Timed)
> Phiên chạy trong khoảng thời gian cố định do Seller thiết lập. Người có giá cao nhất khi hết giờ sẽ thắng.

### Kiểu 2 — Đóng theo bid (Resetting Timer)
> Timer bắt đầu khi có bid được đặt. Nếu không có bid mới trong thời gian quy định:
> 1. Server gửi: *"Going once — Item X: giá Y"*
> 2. Sau 5 giây: *"Going twice — Item X: giá Y"*
> 3. Sau 5 giây nữa: *"SOLD — Item X giá Y cho người mua Z"* → thông báo tới tất cả participants
>
> Timer reset lại nếu có bid mới trong khoảng thời gian này.

---

## 🏗️ Kiến trúc hệ thống

```
┌─────────────────────────────────────────────────────┐
│                   CLIENT SIDE                       │
│  JavaFX UI  ──►  Controller  ──►  Network Layer     │
│  (FXML Views)    (MVC)            (Socket Client)   │
└─────────────────────┬───────────────────────────────┘
                      │  TCP Socket
                      │  Protocol: JSON Messages
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

## 🎨 Design Patterns

### 1. 🏭 Factory Pattern — `ItemFactory`
Tạo các loại sản phẩm đấu giá mà không cần biết class cụ thể, đảm bảo tính mở rộng khi thêm loại sản phẩm mới.

```java
Item item    = ItemFactory.createItem("electronics", warranty, brand, model);
Item art     = ItemFactory.createItem("art", artist, medium, year);
Item vehicle = ItemFactory.createItem("vehicle", engineType, modelYear, mileage);
```

### 2. 🔒 Singleton Pattern — `AuctionManager`
Đảm bảo chỉ có 1 instance duy nhất quản lý toàn bộ phiên đấu giá, tránh race condition giữa các thread.

```java
AuctionManager manager = AuctionManager.getInstance();
List<Auction> actives  = manager.getActiveAuctions();
```

### 3. 👁️ Observer Pattern — `BidObserver`
Tự động thông báo tới tất cả client khi có bid mới, không cần polling.

```java
auction.addObserver(clientHandler);      // server push đến client
auction.addObserver(biddingController);  // cập nhật UI realtime
// Khi có bid mới → notifyObservers() → tất cả nhận được ngay
```

---

## 📐 Class Diagram

### Sơ đồ tổng quan các lớp

```
Entity (abstract)
├── User (abstract)           [name, email, password, userType: UserType]
│   ├── Bidder                [balance, bids: List<BidTransaction>]
│   ├── Seller                [companyName, rating]
│   └── Admin                 [department]
├── Item (abstract)           [name, description, startingPrice]
│   ├── Electronics           [warranty, brand, model]
│   ├── Art                   [artist, medium, year]
│   └── Vehicle               [engineType, modelYear, mileage, licensePlate]
├── Auction                   [item, seller, startTime, endTime, status: AuctionStatus, bids, winner]
└── BidTransaction            [bidder, auction, amount, timestamp]

AutoBid                       [bidder, auction, maxBid, bidStep, isActive]

Enum AuctionStatus: OPEN → RUNNING → FINISHED → PAID / CANCELED
Enum UserType: INDIVIDUAL / ORGANIZATION
```

### Quan hệ giữa các lớp

| Quan hệ | Mô tả |
|---|---|
| `Seller` 1 → * `Auction` | Một Seller tạo nhiều phiên đấu giá |
| `Auction` 1 → 1 `Item` | Mỗi phiên đấu giá một sản phẩm |
| `Auction` 1 → * `BidTransaction` | Mỗi phiên có nhiều lượt đặt giá |
| `Bidder` 1 → * `BidTransaction` | Một Bidder đặt giá nhiều lần |
| `Auction` 1 → 0..1 `Bidder` (winner) | Phiên kết thúc có 0 hoặc 1 người thắng |
| `AutoBid` → `Bidder`, `Auction` | AutoBid hoạt động cho 1 Bidder tại 1 Auction |

> 📁 Xem class diagram chi tiết (file `.drawio`) tại thư mục `Helpful Docs/`

---

## 📦 Cấu trúc dự án

```
Auction-System-in-Java/
├── Helpful Docs/                  # Tài liệu & Class Diagram
│   ├── Class PRF2.drawio          # Class diagram (draw.io)
│   └── Design Patterns.drawio    # Sơ đồ Design Patterns
└── Source/
    └── src/main/java/
        ├── client/
        │   ├── controllers/       # JavaFX Controllers (MVC)
        │   │   ├── LoginController.java
        │   │   ├── BiddingController.java
        │   │   └── AuctionListController.java
        │   ├── network/           # Socket Client
        │   │   └── AuctionClient.java
        │   └── views/             # FXML UI files
        │       ├── login.fxml
        │       ├── bidding.fxml
        │       └── auction_list.fxml
        ├── common/
        │   ├── protocol/          # Giao thức trao đổi dữ liệu
        │   │   ├── Command.java   # Enum các lệnh
        │   │   └── Message.java   # Định dạng JSON message
        │   └── utils/             # Tiện ích dùng chung
        └── server/
            ├── auction/
            │   └── AuctionManager.java  # Singleton
            ├── dao/               # Data Access Objects
            ├── models/            # Entity classes
            │   ├── Entity.java
            │   ├── User.java / Bidder.java / Seller.java / Admin.java
            │   ├── Item.java / Electronics.java / Art.java / Vehicle.java
            │   ├── Auction.java
            │   ├── BidTransaction.java
            │   └── AutoBid.java
            └── network/
                ├── AuctionServer.java   # ServerSocket entry point
                └── ClientHandler.java   # Thread per client
```

---

## 📡 Giao thức truyền tin (Application Protocol)

Hệ thống sử dụng **TCP Socket** với định dạng **JSON** để trao đổi dữ liệu, đảm bảo độ tin cậy và thứ tự gói tin.

> **Lý do chọn TCP thay vì UDP:** Đấu giá yêu cầu tính toàn vẹn dữ liệu tuyệt đối — mỗi bid phải được ghi nhận đúng thứ tự, không được thất lạc. TCP đảm bảo điều này, trong khi UDP không phù hợp cho các giao dịch tài chính.

### Client → Server

| Command | Format JSON | Mô tả |
|---|---|---|
| `LOGIN` | `{"cmd":"LOGIN","username":"alice","password":"123"}` | Đăng nhập |
| `REGISTER` | `{"cmd":"REGISTER","username":"alice","password":"123","role":"BIDDER","type":"INDIVIDUAL"}` | Đăng ký tài khoản |
| `LIST_AUCTIONS` | `{"cmd":"LIST_AUCTIONS"}` | Lấy danh sách phiên đang mở |
| `JOIN_AUCTION` | `{"cmd":"JOIN_AUCTION","auctionId":"A001"}` | Đăng ký tham gia phiên |
| `BID` | `{"cmd":"BID","auctionId":"A001","amount":5000000}` | Đặt giá |
| `CHECK_BID` | `{"cmd":"CHECK_BID","auctionId":"A001"}` | Kiểm tra giá cao nhất hiện tại |
| `WITHDRAW` | `{"cmd":"WITHDRAW","auctionId":"A001"}` | Rút khỏi phiên |
| `CREATE_AUCTION` | `{"cmd":"CREATE_AUCTION","itemType":"electronics","closeType":1,"duration":60}` | Tạo phiên đấu giá |
| `LOGOUT` | `{"cmd":"LOGOUT"}` | Đăng xuất & ngắt kết nối |

### Server → Client

| Event | Format JSON | Mô tả |
|---|---|---|
| `WELCOME` | `{"status":"OK","message":"Welcome alice!"}` | Kết nối thành công |
| `OK` | `{"status":"OK","message":"..."}` | Thao tác thành công |
| `ERROR` | `{"status":"ERROR","message":"Bid too low"}` | Lỗi |
| `BID_UPDATE` | `{"event":"BID_UPDATE","auctionId":"A001","price":5500000,"bidder":"alice","time":"..."}` | Có bid mới |
| `GOING_ONCE` | `{"event":"GOING_ONCE","auctionId":"A001","price":5500000}` | Cảnh báo sắp đóng lần 1 |
| `GOING_TWICE` | `{"event":"GOING_TWICE","auctionId":"A001","price":5500000}` | Cảnh báo sắp đóng lần 2 |
| `AUCTION_CLOSED` | `{"event":"AUCTION_CLOSED","auctionId":"A001","winner":"alice","finalPrice":6000000}` | Phiên kết thúc |
| `GOODBYE` | `{"status":"OK","message":"Goodbye alice!"}` | Ngắt kết nối thành công |

### ⚠️ Kiểm soát lỗi (Error Control)

Server phải gửi thông báo lỗi rõ ràng cho các tình huống:
- Bid thấp hơn hoặc bằng giá hiện tại
- Rút khỏi phiên khi đang giữ giá cao nhất
- Ngắt kết nối khi đang là highest bidder
- Đăng ký vào phiên không tồn tại hoặc đã đóng
- Đặt giá khi chưa đăng ký tham gia phiên

---

## 👥 Phân công nhóm

| Thành viên | Vai trò | Phạm vi công việc |
|---|---|---|
| **Người 1** | OOP Design & Architecture Lead | Class Diagram, Design Patterns, Models (`Entity`, `User`, `Item`, `Auction`, `BidTransaction`), Báo cáo |
| **Người 2** | Backend & Business Logic | DAO, Logic đấu giá, Concurrency, Unit Test (JUnit 5), Auto-Bid |
| **Người 3** | Networking & Real-time | Socket/Protocol (TCP+JSON), Observer Pattern, Server `ClientHandler`, CI/CD |
| **Người 4** | Frontend JavaFX | GUI (FXML), MVC Controllers, LineChart real-time |

---

## 🚀 Hướng dẫn cài đặt & chạy

### Yêu cầu hệ thống

- Java 17+ (OpenJDK 17 hoặc mới hơn)
- Maven 3.9+
- JavaFX 21

### Clone project

```bash
git clone https://github.com/<your-repo>/Auction-System-in-Java.git
cd Auction-System-in-Java/Source
```

### Build project

```bash
mvn clean install
```

### Chạy Server

Server yêu cầu 1 tham số: **PORT**

```bash
mvn exec:java -Dexec.mainClass="server.network.AuctionServer" -Dexec.args="9999"
```

### Chạy Client

Client yêu cầu 2 tham số: **Server IP** và **Port**

```bash
mvn exec:java -Dexec.mainClass="client.MainApp" -Dexec.args="127.0.0.1 9999"
```

> ⚠️ **Lưu ý:** Port phía client và server phải giống nhau.

---

## 🧪 Chạy Unit Test

```bash
mvn test
```

Các test case bao gồm:
- Logic đặt giá hợp lệ / không hợp lệ
- Kiểm tra đóng phiên (Timed & Resetting Timer)
- Concurrent bidding (nhiều client cùng đặt giá đồng thời)
- Kiểm tra Auto-Bid logic

---

## 🗓️ Lộ trình phát triển

- [x] **Giai đoạn 1** — Thiết kế: Class Diagram + Design Patterns + Protocol + Mockup UI
- [ ] **Giai đoạn 2** — Nền móng: Models, ServerSocket, JavaFX skeleton, DAO
- [ ] **Giai đoạn 3** — Tính năng: Bidding logic, Socket communication, UI hoàn chỉnh
- [ ] **Giai đoạn 4** — Hoàn thiện: Auto-Bid, Anti-Snipe, LineChart real-time, CI/CD, Bug fix

---

## 📝 Quy tắc Commit

Dự án tuân theo **Conventional Commits**:

```
feat(auction): add auto-bidding logic
fix(socket): handle client disconnect gracefully
test(bid): add concurrent bidding unit test
docs(readme): update installation guide
refactor(factory): improve ItemFactory switch to enum
chore(ci): add GitHub Actions workflow
```

---

## 📄 Giấy phép

Dự án phục vụ mục đích học tập — UET, môn Lập Trình Nâng Cao.
=======
v
>>>>>>> 454f55f6986ffff30645cc084ca89269d4f8f991

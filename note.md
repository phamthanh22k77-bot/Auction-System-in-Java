# 📝 Hướng Dẫn Chạy & Phân Công Nhiệm Vụ Dự Án

Tài liệu này lưu lại các bước để xây dựng (build) và khởi chạy hệ thống đấu giá (Auction System), cùng với kế hoạch phân công công việc của các thành viên trong nhóm.

---

## 🚀 1. Hướng Dẫn Chạy Hệ Thống

Hãy thực hiện theo các bước dưới đây để build và chạy Server & Client:

### Bước 1: Build dự án bằng IntelliJ & Maven
1. Mở dự án trong **IntelliJ IDEA**.
2. Tìm thanh công cụ Maven ở phía bên phải:
   `Maven` -> `Bigproject...` (Tên project Maven của bạn) -> `Lifecycle`.
3. Chạy lần lượt các lệnh:
   * **`Clean`** (để xóa thư mục build cũ).
   * **`Package`** (để build dự án ra file `.jar`).

### Bước 2: Di chuyển đến thư mục Output
Mở terminal (PowerShell, Command Prompt, hoặc Terminal tích hợp trong IntelliJ) và di chuyển vào thư mục chứa file `.jar` đã build:
```powershell
cd "C:\Users\Admin\Downloads\Bai_Tap_Lon\Bai_Tap_Lon\Auction-System-in-Java\Source\target"
```
*(Hoặc đường dẫn tương đương chứa file `AuctionServer.jar` và `AuctionClient.jar`)*

### Bước 3: Khởi chạy Server
Chạy lệnh sau để bật Server:
```powershell
java -jar AuctionServer.jar
```
> [!IMPORTANT]
> **Không được đóng tab terminal này** trong suốt quá trình chạy ứng dụng để đảm bảo Server hoạt động liên tục.

### Bước 4: Khởi chạy Client
Mở một tab terminal mới, cũng di chuyển tới thư mục chứa các file jar, rồi chạy lệnh:
```powershell
java -jar AuctionClient.jar
```

---

## 👥 2. Phân Công Công Việc & Lộ Trình

| Thành viên | Nhiệm vụ chi tiết | Thời hạn |
| :--- | :--- | :--- |
| **Hoàng** | - Viết lại chi tiết cách hoạt động của Client - Socket.<br>- Nghiên cứu làm báo cáo (tự nghiên cứu trước theo yêu cầu thầy đăng tải trên LMS). | Cập nhật hàng ngày |
| **Kiệt** | - Làm báo cáo PDF (tự nghiên cứu yêu cầu trên LMS).<br>- Prompt AI nốt để ra bản hoàn thiện logic hệ thống + Code sạch (giống trên GitHub). | **Trưa thứ 2** (Hoàn chỉnh) |
| **Thảo** | - Quay video demo theo yêu cầu (tự nghiên cứu trước cách quay).<br>- Thực hiện **sau khi tất cả các phần khác đã hoàn thành**. | Sau khi xong code/báo cáo |

> [!NOTE]
> **Quy trình cập nhật hàng ngày:** Mỗi ngày một người (tức mỗi ngày phải review xong được nhiệm vụ của 1 người) cập nhật lên Google Drive.

---

## ⚠️ 3. Lưu Ý Đặc Biệt Về Code & Commit (Quan Trọng)

> [!WARNING]
> **Vấn đề mã nguồn từ AI:**
> Do có sự hỗ trợ từ AI trong quá trình thực hiện, mã nguồn có thể sẽ chưa hoàn toàn sạch sẽ hoặc không khớp hoàn hảo với cấu trúc chuẩn như trên GitHub ban đầu.

### Hướng giải quyết:
1. **Tự quản lý file:** Sau khi Kiệt gửi bản hoàn chỉnh để nộp bài, các thành viên phải **tự nhận diện các file thuộc phần việc của mình** để chỉnh sửa lại.
2. **Clean Code & Review:** Từng thành viên phải tự chỉnh sửa, dọn dẹp code cho sạch sẽ (**Clean Code**), kiểm tra thật kỹ từng file một trước khi commit lên GitHub (đáng lẽ ra là mình làm trước khi commit lên).
3. **Chủ động commit:** Các thành viên cần chủ động phối hợp commit đều đặn với nhau. Không nên để chỉ một người duy nhất up toàn bộ code lên cùng lúc để tránh gây nghi ngờ về tính độc lập/đóng góp của từng người.

# 📘 Ghi chú giải thích UserDAO.java

## UserDAO là gì?

**DAO** = Data Access Object = **"Người giữ kho dữ liệu"**

UserDAO chỉ làm 1 việc: **lưu và tải danh sách User từ file `data/users.json`**.

Không có logic nghiệp vụ ở đây — chỉ đọc và ghi file.

---

## Cấu trúc tổng quan

```
UserDAO
├── saveAll()       → Ghi toàn bộ danh sách ra file JSON
├── loadAll()       → Đọc file JSON, tạo lại danh sách User
├── them()          → Thêm 1 user mới vào file
├── xoaTheoId()     → Xóa user theo ID
├── timTheoId()     → Tìm user theo ID
│
├── [private] userToJson()      → Chuyển 1 User → chuỗi JSON
├── [private] tachCacObject()   → Tách chuỗi JSON → danh sách { }
└── [private] layGiaTri()       → Đọc giá trị 1 trường trong JSON
```

> Các hàm `private` chỉ phục vụ nội bộ, bên ngoài không gọi trực tiếp.

---

## Tư duy thiết kế: Tại sao không dùng thư viện?

File này parse JSON **thủ công bằng Java thuần** — không dùng Gson hay Jackson.

Vì sao? → Để học cách tư duy, không phụ thuộc thư viện ngoài.

Nhược điểm: code dài hơn, dễ lỗi hơn với JSON phức tạp.

---

## Giải thích từng hàm

---

### `saveAll(List<User> users)`

**Mục đích:** Ghi toàn bộ danh sách User ra file JSON.

**Tư duy:** Xây dựng 1 chuỗi JSON dài rồi ghi 1 lần duy nhất.

```
Bắt đầu:   [
User 1:       { "id": "...", "role": "BIDDER", ... },
User 2:       { "id": "...", "role": "ADMIN",  ... }
Kết thúc:   ]
```

**Lưu ý quan trọng:** Hàm này **ghi đè** toàn bộ file mỗi lần gọi.
Nếu muốn thêm 1 user → phải dùng `them()` chứ không gọi thẳng `saveAll()`.

---

### `loadAll()`

**Mục đích:** Đọc file JSON và tạo lại danh sách User trong bộ nhớ.

**Các bước:**
```
1. Kiểm tra file có tồn tại không
2. Đọc toàn bộ file thành 1 chuỗi
3. Bỏ dấu [ đầu và ] cuối
4. Tách thành danh sách { ... } bằng tachCacObject()
5. Với mỗi object:
   - Đọc "role" để biết loại User
   - Tạo đúng loại: Admin / Bidder / Seller
6. Trả về danh sách
```

**Điểm mấu chốt:** Trường `"role"` trong JSON là kim chỉ nam —
nếu thiếu `"role"`, loadAll() sẽ không tạo được User nào.

---

### `them(User user)`

**Mục đích:** Thêm 1 user vào file.

**Tư duy đơn giản:**
```
loadAll()  →  thêm user mới vào danh sách  →  saveAll() lại
```

Không ghi thẳng vào file — tải hết ra, thêm, ghi lại toàn bộ.

---

### `xoaTheoId(String id)`

**Mục đích:** Xóa user có ID khớp khỏi file.

```
loadAll()  →  xóa phần tử có id trùng  →  saveAll() lại
```

Dùng `removeIf()` — bỏ những phần tử thỏa điều kiện.

---

### `timTheoId(String id)`

**Mục đích:** Tìm và trả về 1 User có ID khớp, trả `null` nếu không thấy.

**Tư duy:** Duyệt từng user, so ID, thấy thì trả về ngay.

```java
for (User u : danhSach) {
    if (u.getId().equals(id)) {
        return u;      // Tìm thấy → trả về ngay, dừng vòng lặp
    }
}
return null;   // Hết danh sách vẫn không thấy
```

---

### `[private] userToJson(User u)`

**Mục đích:** Chuyển 1 đối tượng User thành chuỗi JSON.

**Ví dụ kết quả:**
```json
  {
    "id": "abc-123",
    "role": "BIDDER",
    "username": "ThanhBot",
    "email": "thanh@mail.com",
    "password": "VNU123@",
    "balance": 1000.0
  }
```

**Dùng `StringBuffer`** để ghép dần chuỗi (an toàn hơn StringBuilder trong đa luồng).

**Xử lý đa hình:** `instanceof` kiểm tra loại thật của User để ghi trường đặc thù:
- `Admin` → thêm `"department"`
- `Bidder` → thêm `"balance"`
- `Seller` → thêm `"companyName"` và `"rating"`

---

### `[private] tachCacObject(String content)`

**Mục đích:** Từ 1 chuỗi JSON mảng, tách ra thành danh sách các object `{ }`.

**Tư duy:** Đếm dấu ngoặc `{` và `}`:
- Gặp `{` → đếm lên 1 (`depth++`)
- Gặp `}` → đếm xuống 1 (`depth--`)
- Khi `depth == 0` → đã đóng xong 1 object → lưu lại

```
Chuỗi:  { "a": 1 }, { "b": { "c": 2 } }
         ^^^^^^^^^^   ^^^^^^^^^^^^^^^^^^
         object 1     object 2 (có { lồng bên trong)
```

Kỹ thuật đếm ngoặc này xử lý đúng cả JSON có **nested object**.

---

### `[private] layGiaTri(String json, String tenTruong)`

**Mục đích:** Đọc giá trị của 1 trường trong chuỗi JSON.

**Ví dụ:**
```
layGiaTri(obj, "role")    → "ADMIN"
layGiaTri(obj, "balance") → "1000.0"
```

**Các bước:**
```
1. Tìm vị trí của "tenTruong": trong chuỗi
2. Cắt lấy phần chuỗi bên phải dấu :
3. Nếu bắt đầu bằng " → giá trị là chuỗi → lấy đến " tiếp theo
   Nếu không → giá trị là số → lấy đến , hoặc }
```

**Lưu ý:** Hàm này giả định JSON được ghi đúng định dạng.
Nếu JSON bị lỗi (thiếu dấu `"`, thiếu `,`...) sẽ gây ra lỗi runtime.

---

## Sơ đồ luồng dữ liệu

```
[Chương trình]
     │
     │ them(user)
     ▼
[UserDAO]
     │ loadAll() → đọc file → tạo List<User>
     │ thêm user mới vào List
     │ saveAll() → chuyển List → JSON → ghi file
     ▼
[data/users.json]
```

---

## Lỗi thường gặp

| Lỗi | Nguyên nhân |
|-----|-------------|
| `StringIndexOutOfBoundsException` tại `loadAll()` | File JSON rỗng hoặc không hợp lệ |
| User không load được (danh sách rỗng) | Thiếu trường `"role"` trong JSON |
| `getPassword()` không dùng được từ DAO | `getPassword()` trong `User.java` phải là `public` |
| Thêm user nhưng file không cập nhật | Gọi `saveAll()` tự do thay vì dùng `them()` |

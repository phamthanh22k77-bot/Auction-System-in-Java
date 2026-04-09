-- Tạo cơ sở dữ liệu cho ứng dụng đấu giá
CREATE DATABASE login;
USE login;

-- Tạo bảng users dựa trên Class Diagram
CREATE TABLE users (
                       id VARCHAR(50) PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(50) NOT NULL,
                       role VARCHAR(20) NOT NULL,

    -- Các thuộc tính riêng biệt (Cho phép NULL vì không phải role nào cũng có)
    (!! Có gi xem lại chỗ này!!)
                       department VARCHAR(100),                -- Dành riêng cho Admin
                       company_name VARCHAR(100),              -- Dành riêng cho Seller
                       rating DOUBLE DEFAULT 0.0,              -- Dành riêng cho Seller (Điểm đánh giá)
                       balance DOUBLE DEFAULT 0.0              -- Dành riêng cho Bidder (Số dư tài khoản)
);

-- ==========================================
-- CHÈN DỮ LIỆU MẪU ĐỂ TEST CHỨC NĂNG LOGIN
-- ==========================================

-- 1. Tạo 1 tài khoản ADMIN
INSERT INTO users (id, username, email, password, role, department)
VALUES ('A001', 'admin_vip', 'admin@auction.com', '123456', 'ADMIN', 'Quản trị hệ thống');

-- 2. Tạo 1 tài khoản SELLER (Người bán)
INSERT INTO users (id, username, email, password, role, company_name, rating)
VALUES ('S001', 'seller_tech', 'seller@auction.com', '123456', 'SELLER', 'TechCorp VN', 4.8);

-- 3. Tạo 1 tài khoản BIDDER (Người đấu giá)
INSERT INTO users (id, username, email, password, role, balance)
VALUES ('B001', 'bidder_pro', 'bidder@auction.com', '123456', 'BIDDER', 5000000.0);
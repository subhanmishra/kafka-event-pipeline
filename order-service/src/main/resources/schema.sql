CREATE DATABASE IF NOT EXISTS edtech_orders;

USE edtech_orders;
CREATE TABLE IF NOT EXISTS orders (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        user_id BIGINT NOT NULL,
                        course_id BIGINT NOT NULL,
                        amount DECIMAL(10, 2) NOT NULL,
                        status VARCHAR(50) NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        INDEX idx_user_id (user_id),
                        INDEX idx_status (status)
);
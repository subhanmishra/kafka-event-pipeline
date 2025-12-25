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

CREATE TABLE IF NOT EXISTS outbox_events (
                                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             aggregate_type VARCHAR(100) NOT NULL,
                                             aggregate_id BIGINT NOT NULL,
                                             type VARCHAR(100) NOT NULL,
                                             payload JSON NOT NULL,
                                             status VARCHAR(20) NOT NULL DEFAULT 'NEW',
                                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                             sent_at TIMESTAMP NULL,
                                             INDEX idx_status_created_at (status, created_at)
);
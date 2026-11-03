-- Fresh-install schema. For an existing database, run migration.sql instead.
CREATE DATABASE IF NOT EXISTS library_db;
USE library_db;
CREATE TABLE IF NOT EXISTS categories (category_id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(100) NOT NULL UNIQUE);
INSERT IGNORE INTO categories (name) VALUES
('Computer Science & Technology'), ('Science & Mathematics'), ('Engineering & Applied Sciences'), ('Business & Management'), ('Literature & Languages'), ('General Knowledge & Social Sciences');
CREATE TABLE IF NOT EXISTS books (book_id INT PRIMARY KEY AUTO_INCREMENT, title VARCHAR(100) NOT NULL, author VARCHAR(100) NOT NULL, isbn VARCHAR(20) NOT NULL UNIQUE, category_id INT NOT NULL, total_copies INT NOT NULL, available_copies INT NOT NULL, CONSTRAINT fk_books_category FOREIGN KEY (category_id) REFERENCES categories(category_id), CHECK (total_copies >= 0), CHECK (available_copies BETWEEN 0 AND total_copies));
CREATE TABLE IF NOT EXISTS members (member_id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(100) NOT NULL, department VARCHAR(50) NOT NULL, phone VARCHAR(15) NOT NULL);
CREATE TABLE IF NOT EXISTS issues (issue_id INT PRIMARY KEY AUTO_INCREMENT, book_id INT NOT NULL, member_id INT NOT NULL, issue_date DATE NOT NULL, due_date DATE NOT NULL, return_date DATE, status VARCHAR(20) NOT NULL DEFAULT 'ISSUED', FOREIGN KEY (book_id) REFERENCES books(book_id), FOREIGN KEY (member_id) REFERENCES members(member_id), CHECK (status IN ('ISSUED', 'RETURNED')), CHECK (return_date IS NULL OR return_date >= issue_date));

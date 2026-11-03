-- Safe, idempotent upgrade. Never drops a database or existing data.
USE library_db;
CREATE TABLE IF NOT EXISTS categories (category_id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(100) NOT NULL UNIQUE);
INSERT IGNORE INTO categories (name) VALUES
('Computer Science & Technology'), ('Science & Mathematics'), ('Engineering & Applied Sciences'), ('Business & Management'), ('Literature & Languages'), ('General Knowledge & Social Sciences');
DELIMITER //
CREATE PROCEDURE upgrade_library_schema()
BEGIN
 IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='books' AND column_name='isbn') THEN ALTER TABLE books ADD COLUMN isbn VARCHAR(20) NULL; END IF;
 IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='books' AND column_name='category_id') THEN ALTER TABLE books ADD COLUMN category_id INT NULL; END IF;
 IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='issues' AND column_name='due_date') THEN ALTER TABLE issues ADD COLUMN due_date DATE NULL; END IF;
END //
CALL upgrade_library_schema() //
DROP PROCEDURE upgrade_library_schema //
DELIMITER ;
UPDATE books b JOIN categories c ON c.name='General Knowledge & Social Sciences' SET b.category_id=c.category_id WHERE b.category_id IS NULL;
UPDATE books SET isbn=CONCAT('LEGACY-', book_id) WHERE isbn IS NULL OR TRIM(isbn)='';
UPDATE issues SET due_date=DATE_ADD(issue_date, INTERVAL 14 DAY) WHERE due_date IS NULL;
ALTER TABLE books MODIFY isbn VARCHAR(20) NOT NULL, MODIFY category_id INT NOT NULL;
ALTER TABLE issues MODIFY due_date DATE NOT NULL;
DELIMITER //
CREATE PROCEDURE add_upgrade_constraints()
BEGIN
 IF NOT EXISTS (SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='books' AND index_name='uq_books_isbn') THEN
   CREATE UNIQUE INDEX uq_books_isbn ON books(isbn);
 END IF;
 IF NOT EXISTS (SELECT 1 FROM information_schema.key_column_usage WHERE table_schema=DATABASE() AND table_name='books' AND column_name='category_id' AND referenced_table_name='categories') THEN
   ALTER TABLE books ADD CONSTRAINT fk_books_category FOREIGN KEY (category_id) REFERENCES categories(category_id);
 END IF;
END //
CALL add_upgrade_constraints() //
DROP PROCEDURE add_upgrade_constraints //
DELIMITER ;

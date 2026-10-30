
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    public boolean addBook(Book book) throws SQLException {
        validateBookDetails(book);

        if (book.getTotalCopies() <= 0) {
            throw new IllegalArgumentException(
                    "Total copies must be greater than zero.");
        }

        String sql = """
                INSERT INTO books
                (title, author, category, total_copies, available_copies)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, book.getTitle().trim());
            ps.setString(2, book.getAuthor().trim());
            ps.setString(3, book.getCategory().trim());
            ps.setInt(4, book.getTotalCopies());
            ps.setInt(5, book.getTotalCopies());

            return ps.executeUpdate() == 1;
        }
    }

    public List<Book> getAllBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books ORDER BY book_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                books.add(mapBook(rs));
            }
        }

        return books;
    }

    public Book getBookById(int bookId) throws SQLException {
        validateId(bookId, "Book ID");

        String sql = "SELECT * FROM books WHERE book_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, bookId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapBook(rs);
                }
            }
        }

        return null;
    }

    public List<Book> searchBooksByTitle(String title)
            throws SQLException {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Search title cannot be empty.");
        }

        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE title LIKE ? ORDER BY title";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + title.trim() + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(mapBook(rs));
                }
            }
        }

        return books;
    }

    public boolean updateBook(Book book) throws SQLException {
        validateBookDetails(book);

        if (book.getTotalCopies() < 0) {
            throw new IllegalArgumentException(
                    "Total copies cannot be negative.");
        }

        validateId(book.getBookId(), "Book ID");

        String sql = """
                UPDATE books
                SET title = ?, author = ?, category = ?,
                    total_copies = ?, available_copies = ?
                WHERE book_id = ?
                """;

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            try {
                int issuedCopies;

                String checkSql = """
                        SELECT total_copies, available_copies
                        FROM books
                        WHERE book_id = ?
                        FOR UPDATE
                        """;

                try (PreparedStatement check =
                             con.prepareStatement(checkSql)) {
                    check.setInt(1, book.getBookId());

                    try (ResultSet rs = check.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            return false;
                        }

                        int total = rs.getInt("total_copies");
                        int available = rs.getInt("available_copies");

                        if (total < 0 || available < 0 || available > total) {
                            throw new SQLException(
                                    "Existing book copy counts are inconsistent.");
                        }

                        issuedCopies = total - available;
                    }
                }

                if (book.getTotalCopies() < issuedCopies) {
                    throw new IllegalArgumentException(
                            "Total copies cannot be less than the number "
                                    + "of copies currently issued ("
                                    + issuedCopies + ").");
                }

                int availableCopies =
                        book.getTotalCopies() - issuedCopies;

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, book.getTitle().trim());
                    ps.setString(2, book.getAuthor().trim());
                    ps.setString(3, book.getCategory().trim());
                    ps.setInt(4, book.getTotalCopies());
                    ps.setInt(5, availableCopies);
                    ps.setInt(6, book.getBookId());

                    boolean updated = ps.executeUpdate() == 1;
                    con.commit();
                    return updated;
                }

            } catch (SQLException | RuntimeException e) {
                rollbackPreservingError(con, e);
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public boolean deleteBook(int bookId) throws SQLException {
        validateId(bookId, "Book ID");

        String sql = "DELETE FROM books WHERE book_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, bookId);
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {
                throw new SQLException(
                        "Cannot delete this book because issue history "
                                + "references it. Keep the record to preserve "
                                + "the issue history.",
                        e);
            }

            throw e;
        }
    }

    private void validateBookDetails(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Book details are required.");
        }

        validateText(book.getTitle(), "Title", 100);
        validateText(book.getAuthor(), "Author", 100);
        validateText(book.getCategory(), "Category", 50);
    }

    private void validateText(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required.");
        }

        if (value.trim().length() > maxLength) {
            throw new IllegalArgumentException(
                    field + " cannot exceed " + maxLength + " characters.");
        }
    }

    private void validateId(int id, String field) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    field + " must be greater than zero.");
        }
    }

    private void rollbackPreservingError(
            Connection con, Exception originalError) {
        try {
            con.rollback();
        } catch (SQLException rollbackError) {
            originalError.addSuppressed(rollbackError);
        }
    }

    private Book mapBook(ResultSet rs) throws SQLException {
        return new Book(
                rs.getInt("book_id"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("category"),
                rs.getInt("total_copies"),
                rs.getInt("available_copies")
        );
    }
}

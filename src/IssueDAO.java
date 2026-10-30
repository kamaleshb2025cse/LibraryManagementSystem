
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class IssueDAO {

    public boolean issueBook(int bookId, int memberId) throws SQLException {
        validateId(bookId, "Book ID");
        validateId(memberId, "Member ID");

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            try {
                int availableCopies;

                String bookSql = """
                        SELECT total_copies, available_copies
                        FROM books
                        WHERE book_id = ?
                        FOR UPDATE
                        """;

                try (PreparedStatement ps = con.prepareStatement(bookSql)) {
                    ps.setInt(1, bookId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Book ID not found.");
                        }

                        int totalCopies = rs.getInt("total_copies");
                        availableCopies = rs.getInt("available_copies");

                        if (totalCopies < 0
                                || availableCopies < 0
                                || availableCopies > totalCopies) {
                            throw new SQLException(
                                    "Book copy counts are inconsistent. "
                                            + "Please check the book record.");
                        }

                        if (availableCopies == 0) {
                            throw new SQLException(
                                    "No copies of this book are available.");
                        }
                    }
                }

                String memberSql =
                        "SELECT member_id FROM members WHERE member_id = ?";

                try (PreparedStatement ps = con.prepareStatement(memberSql)) {
                    ps.setInt(1, memberId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Member ID not found.");
                        }
                    }
                }

                String issueSql = """
                        INSERT INTO issues
                        (book_id, member_id, issue_date, status)
                        VALUES (?, ?, ?, 'ISSUED')
                        """;

                try (PreparedStatement ps = con.prepareStatement(issueSql)) {
                    ps.setInt(1, bookId);
                    ps.setInt(2, memberId);
                    ps.setDate(3, new Date(System.currentTimeMillis()));
                    ps.executeUpdate();
                }

                String updateBookSql = """
                        UPDATE books
                        SET available_copies = available_copies - 1
                        WHERE book_id = ? AND available_copies > 0
                        """;

                try (PreparedStatement ps =
                             con.prepareStatement(updateBookSql)) {
                    ps.setInt(1, bookId);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                                "Unable to update the available book count.");
                    }
                }

                con.commit();
                return true;

            } catch (SQLException | RuntimeException e) {
                rollbackPreservingError(con, e);
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public boolean returnBook(int issueId) throws SQLException {
        validateId(issueId, "Issue ID");

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            try {
                int bookId;

                String issueSql = """
                        SELECT book_id, status
                        FROM issues
                        WHERE issue_id = ?
                        FOR UPDATE
                        """;

                try (PreparedStatement ps = con.prepareStatement(issueSql)) {
                    ps.setInt(1, issueId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Issue ID not found.");
                        }

                        String status = rs.getString("status");

                        if (!"ISSUED".equalsIgnoreCase(status)) {
                            throw new SQLException(
                                    "This book has already been returned.");
                        }

                        bookId = rs.getInt("book_id");
                    }
                }

                // Lock the associated book and validate its copy counts.
                String bookSql = """
                        SELECT total_copies, available_copies
                        FROM books
                        WHERE book_id = ?
                        FOR UPDATE
                        """;

                try (PreparedStatement ps = con.prepareStatement(bookSql)) {
                    ps.setInt(1, bookId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException(
                                    "The associated book record was not found.");
                        }

                        int totalCopies = rs.getInt("total_copies");
                        int availableCopies = rs.getInt("available_copies");

                        if (totalCopies < 0
                                || availableCopies < 0
                                || availableCopies > totalCopies) {
                            throw new SQLException(
                                    "Book copy counts are inconsistent. "
                                            + "Please check the book record.");
                        }

                        if (availableCopies >= totalCopies) {
                            throw new SQLException(
                                    "Available copies already equal total copies. "
                                            + "The return was not recorded.");
                        }
                    }
                }

                String updateIssueSql = """
                        UPDATE issues
                        SET return_date = ?, status = 'RETURNED'
                        WHERE issue_id = ? AND status = 'ISSUED'
                        """;

                try (PreparedStatement ps =
                             con.prepareStatement(updateIssueSql)) {
                    ps.setDate(1, new Date(System.currentTimeMillis()));
                    ps.setInt(2, issueId);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                                "Unable to record the book return.");
                    }
                }

                String updateBookSql = """
                        UPDATE books
                        SET available_copies = available_copies + 1
                        WHERE book_id = ?
                          AND available_copies < total_copies
                        """;

                try (PreparedStatement ps =
                             con.prepareStatement(updateBookSql)) {
                    ps.setInt(1, bookId);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                                "Unable to update the available book count.");
                    }
                }

                con.commit();
                return true;

            } catch (SQLException | RuntimeException e) {
                rollbackPreservingError(con, e);
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public List<Issue> getAllIssues() throws SQLException {
        List<Issue> issues = new ArrayList<>();

        String sql = """
                SELECT issue_id, book_id, member_id, issue_date,
                       return_date, status
                FROM issues
                ORDER BY issue_id
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                issues.add(new Issue(
                        rs.getInt("issue_id"),
                        rs.getInt("book_id"),
                        rs.getInt("member_id"),
                        rs.getDate("issue_date"),
                        rs.getDate("return_date"),
                        rs.getString("status")
                ));
            }
        }

        return issues;
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
}
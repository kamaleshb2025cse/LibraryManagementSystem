
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Scanner sc = new Scanner(System.in);

    private static final BookDAO bookDAO = new BookDAO();
    private static final MemberDAO memberDAO = new MemberDAO();
    private static final IssueDAO issueDAO = new IssueDAO();

    public static void main(String[] args) {
        boolean running = true;

        while (running) {
            printMenu();
            String choice = sc.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> addBook();
                    case "2" -> viewBooks(bookDAO.getAllBooks());
                    case "3" -> searchBook();
                    case "4" -> updateBook();
                    case "5" -> deleteBook();
                    case "6" -> addMember();
                    case "7" -> viewMembers();
                    case "8" -> issueBook();
                    case "9" -> returnBook();
                    case "10" -> viewIssues();
                    case "0" -> {
                        System.out.println("Thank you for using the system!");
                        running = false;
                    }
                    default -> System.out.println(
                            "Invalid choice. Please enter a number from 0 to 10.");
                }
            } catch (SQLException e) {
                System.out.println("Database operation failed: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid input: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("An unexpected error occurred: "
                        + e.getMessage());
            }

            if (running) {
                System.out.println();
            }
        }

        sc.close();
    }

    private static void printMenu() {
        System.out.println("\n===== LIBRARY MANAGEMENT SYSTEM =====");
        System.out.println("1. Add Book");
        System.out.println("2. View All Books");
        System.out.println("3. Search Book by Title");
        System.out.println("4. Update Book");
        System.out.println("5. Delete Book");
        System.out.println("6. Register Member");
        System.out.println("7. View All Members");
        System.out.println("8. Issue Book");
        System.out.println("9. Return Book");
        System.out.println("10. View Issue Records");
        System.out.println("0. Exit");
        System.out.print("Enter choice: ");
    }

    // BOOK MANAGEMENT

    private static void addBook() throws SQLException {
        String title = readRequiredText("Title: ", 100);
        String author = readRequiredText("Author: ", 100);
        String category = readRequiredText("Category: ", 50);
        int copies = readPositiveInt("Total copies: ");

        Book book = new Book(title, author, category, copies);

        if (bookDAO.addBook(book)) {
            System.out.println("Book added successfully!");
        } else {
            System.out.println("Book could not be added.");
        }
    }

    private static void searchBook() throws SQLException {
        String title = readRequiredText("Enter title to search: ", 100);
        viewBooks(bookDAO.searchBooksByTitle(title));
    }

    private static void viewBooks(List<Book> books) {
        if (books.isEmpty()) {
            System.out.println("No books found.");
            return;
        }

        System.out.println("\n--- BOOK LIST ---");
        for (Book book : books) {
            System.out.println(book);
        }
    }

    private static void updateBook() throws SQLException {
        int id = readPositiveInt("Enter book ID to update: ");
        Book oldBook = bookDAO.getBookById(id);

        if (oldBook == null) {
            System.out.println("Book not found.");
            return;
        }

        System.out.println("Current details: " + oldBook);

        String title = readRequiredText("New title: ", 100);
        String author = readRequiredText("New author: ", 100);
        String category = readRequiredText("New category: ", 50);
        int copies = readNonNegativeInt("New total copies: ");

        Book updatedBook = new Book(
                id,
                title,
                author,
                category,
                copies,
                oldBook.getAvailableCopies()
        );

        if (bookDAO.updateBook(updatedBook)) {
            System.out.println("Book updated successfully!");
        } else {
            System.out.println("Book was not updated. Check the book ID.");
        }
    }

    private static void deleteBook() throws SQLException {
        int id = readPositiveInt("Enter book ID to delete: ");

        Book book = bookDAO.getBookById(id);
        if (book == null) {
            System.out.println("Book not found.");
            return;
        }

        System.out.println("Selected book: " + book);
        System.out.print("Confirm deletion? Type YES to continue: ");
        String confirmation = sc.nextLine().trim();

        if (!confirmation.equalsIgnoreCase("YES")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        if (bookDAO.deleteBook(id)) {
            System.out.println("Book deleted successfully!");
        } else {
            System.out.println("Book could not be deleted.");
        }
    }

    // MEMBER MANAGEMENT

    private static void addMember() throws SQLException {
        String name = readRequiredText("Member name: ", 100);
        String department = readRequiredText("Department: ", 50);
        String phone = readRequiredText("Phone: ", 15);

        Member member = new Member(name, department, phone);

        if (memberDAO.addMember(member)) {
            System.out.println("Member registered successfully!");
        } else {
            System.out.println("Member could not be registered.");
        }
    }

    private static void viewMembers() throws SQLException {
        List<Member> members = memberDAO.getAllMembers();

        if (members.isEmpty()) {
            System.out.println("No members registered yet.");
            return;
        }

        System.out.println("\n--- MEMBER LIST ---");
        for (Member member : members) {
            System.out.println(member);
        }
    }

    // ISSUE AND RETURN

    private static void issueBook() throws SQLException {
        int bookId = readPositiveInt("Enter book ID: ");
        int memberId = readPositiveInt("Enter member ID: ");

        if (issueDAO.issueBook(bookId, memberId)) {
            System.out.println("Book issued successfully!");
        } else {
            System.out.println("Book could not be issued.");
        }
    }

    private static void returnBook() throws SQLException {
        int issueId = readPositiveInt("Enter issue ID to return: ");

        if (issueDAO.returnBook(issueId)) {
            System.out.println("Book returned successfully!");
        } else {
            System.out.println("Book could not be returned.");
        }
    }

    private static void viewIssues() throws SQLException {
        List<Issue> issues = issueDAO.getAllIssues();

        if (issues.isEmpty()) {
            System.out.println("No issue records found.");
            return;
        }

        System.out.println("\n--- ISSUE RECORDS ---");
        for (Issue issue : issues) {
            System.out.println(issue);
        }
    }

    // INPUT VALIDATION

    private static String readRequiredText(String prompt, int maxLength) {
        System.out.print(prompt);
        String value = sc.nextLine().trim();

        if (value.isBlank()) {
            throw new IllegalArgumentException("This field is required.");
        }

        if (value.length() > maxLength) {
            throw new IllegalArgumentException(
                    "Maximum allowed length is " + maxLength + " characters.");
        }

        return value;
    }

    private static int readPositiveInt(String prompt) {
        int value = readInt(prompt);

        if (value <= 0) {
            throw new IllegalArgumentException(
                    "Enter a whole number greater than zero.");
        }

        return value;
    }

    private static int readNonNegativeInt(String prompt) {
        int value = readInt(prompt);

        if (value < 0) {
            throw new IllegalArgumentException(
                    "Enter zero or a positive whole number.");
        }

        return value;
    }

    private static int readInt(String prompt) {
        System.out.print(prompt);
        String value = sc.nextLine().trim();

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Please enter a valid whole number.");
        }
    }
}
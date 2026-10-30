
public class Book {

    private int bookId;
    private String title;
    private String author;
    private String category;
    private int totalCopies;
    private int availableCopies;

    public Book(int bookId, String title, String author,
                String category, int totalCopies, int availableCopies) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.category = category;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    public Book(String title, String author, String category,
                int totalCopies) {
        this(0, title, author, category, totalCopies, totalCopies);
    }

    public int getBookId() {
        return bookId;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getCategory() {
        return category;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty.");
        }
        this.title = title.trim();
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setTotalCopies(int totalCopies) {
        if (totalCopies < 0 || totalCopies < this.totalCopies - this.availableCopies) {
            throw new IllegalArgumentException(
                    "Total copies cannot be less than copies currently issued."
            );
        }
        this.availableCopies += totalCopies - this.totalCopies;
        this.totalCopies = totalCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        if (availableCopies < 0 || availableCopies > totalCopies) {
            throw new IllegalArgumentException("Invalid available copy count.");
        }
        this.availableCopies = availableCopies;
    }

    @Override
    public String toString() {
        return "Book ID: " + bookId
                + " | Title: " + title
                + " | Author: " + author
                + " | Category: " + category
                + " | Total: " + totalCopies
                + " | Available: " + availableCopies;
    }
}
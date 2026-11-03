public class Book {
    private final int bookId, categoryId, totalCopies, availableCopies;
    private final String title, author, isbn, categoryName;
    public Book(int id,String title,String author,String isbn,int categoryId,String categoryName,int total,int available){this.bookId=id;this.title=title;this.author=author;this.isbn=isbn;this.categoryId=categoryId;this.categoryName=categoryName;this.totalCopies=total;this.availableCopies=available;}
    public Book(String title,String author,String isbn,int categoryId,int total){this(0,title,author,isbn,categoryId,"",total,total);}
    public int getBookId(){return bookId;} public String getTitle(){return title;} public String getAuthor(){return author;} public String getIsbn(){return isbn;} public int getCategoryId(){return categoryId;} public String getCategoryName(){return categoryName;} public int getTotalCopies(){return totalCopies;} public int getAvailableCopies(){return availableCopies;}
    @Override public String toString(){return String.format("%-4d %-28s %-20s %-16s %-34s %3d/%-3d",bookId,title,author,isbn,categoryName,availableCopies,totalCopies);}
}

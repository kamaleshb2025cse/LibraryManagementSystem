# Library Management System

## 1. Project Title and Statement

**Project Title:** Library Management System Using Java and MySQL

**Project Statement**

The Library Management System is a console-based application developed using Java and MySQL to simplify library operations. It enables users to manage book records, register library members, issue books, and process book returns.

The application uses Java Database Connectivity (JDBC) to connect to the MySQL database. It maintains book availability and issue/return records to support accurate and efficient library management.

## 2. Output Menu

The application provides the following menu-driven interface:

```text
========== LIBRARY MANAGEMENT SYSTEM ==========

1. Add Book
2. View All Books
3. Search Book by Title
4. Update Book
5. Delete Book
6. Register Member
7. View All Members
8. Issue Book
9. Return Book
10. View All Issue Records
0. Exit

Enter your choice:
```

## 3. Project File Structure

```text
LibraryManagementSystem/
│
├── src/
│   ├── Main.java
│   ├── DBConnection.java
│   ├── Book.java
│   ├── BookDAO.java
│   ├── Member.java
│   ├── MemberDAO.java
│   ├── Issue.java
│   └── IssueDAO.java
│
├── database/
│   └── schema.sql
│
├── lib/
│   └── mysql-connector-j-26.10.0.jar
│
├── out/                         # Compiled Java class files
│
├── README.md
└── .gitignore
```

### File Descriptions

- **Main.java** — Provides the menu-driven interface and handles user input.
- **DBConnection.java** — Establishes the connection between Java and MySQL.
- **Book.java** — Represents book information.
- **BookDAO.java** — Handles book-related database operations.
- **Member.java** — Represents library member information.
- **MemberDAO.java** — Handles member-related database operations.
- **Issue.java** — Represents book issue and return records.
- **IssueDAO.java** — Handles book issuing, returning, and issue record retrieval.
- **database/schema.sql** — Contains the SQL schema used to set up the database.
- **lib/** — Contains the MySQL JDBC driver.
- **out/** — Stores compiled Java class files generated during compilation.

## 4. Database Design and Connection

The project uses **MySQL** as its relational database and **JDBC** to connect the Java application to the database.

**Database name:** `library_db`

### Database Tables

#### 1. Books

Stores book details and copy availability.

| Column | Description |
|---|---|
| `book_id` | Primary key identifying each book |
| `title` | Title of the book |
| `author` | Author's name |
| `category` | Book category |
| `total_copies` | Total number of copies |
| `available_copies` | Number of copies available for issue |

#### 2. Members

Stores registered library member information.

| Column | Description |
|---|---|
| `member_id` | Primary key identifying each member |
| `name` | Member's name |
| `department` | Member's department |
| `phone` | Contact number |

#### 3. Issues

Maintains book issue and return records.

| Column | Description |
|---|---|
| `issue_id` | Primary key identifying each issue record |
| `book_id` | Foreign key referencing `books.book_id` |
| `member_id` | Foreign key referencing `members.member_id` |
| `issue_date` | Date the book was issued |
| `return_date` | Date the book was returned; empty until return |
| `status` | Issue status, such as `ISSUED` or `RETURNED` |

### Database Relationships

- A book can have multiple issue records over time.
- A member can have multiple issue records.
- The `issues` table connects books and members through foreign keys.

### Database Connection

The `DBConnection.java` file uses the MySQL JDBC driver to establish a connection to `library_db`. Database credentials are provided through environment variables instead of being hard-coded into the source code.

The application uses SQL statements to perform database operations. Book issuing and returning update the available copy count so that the inventory reflects the current issue status.

### Database Setup

1. Start the MySQL server.
2. Execute `database/schema.sql` in MySQL Workbench or the MySQL command-line client.
3. Ensure the database and tables are created successfully.
4. Configure the database username and password using environment variables.

## 5. Technology Stack

| Technology | Purpose |
|---|---|
| Java | Application logic and console interface |
| MySQL | Relational database management |
| JDBC | Connecting Java to MySQL |
| MySQL Connector/J | JDBC driver for MySQL |
| Visual Studio Code | Development environment |
| PowerShell | Compiling and running the application |

### Compile and Run

Run these commands from the project root directory in PowerShell.

**Set database credentials:**

```powershell
$env:DB_USER = "root"
$env:DB_PASSWORD = Read-Host "Enter your MySQL password"
```

Use your own MySQL username if it differs from `root`.

**Compile the application:**

```powershell
javac -cp ".\lib\*" -d .\out .\src\*.java
```

**Run the application:**

```powershell
java -cp ".\out;.\lib\*" Main
```

Ensure that MySQL is running and the database has been created before launching the application.

## 6. Team Members

| S. No. | Name | Registration Number |
|---:|---|---|
| 1 | Jeevan Kumaar B | 2116250701277 |
| 2 | Kamalesh B | 2116250701308 |
| 3 | Karthik Raj A | 2116250701325 |

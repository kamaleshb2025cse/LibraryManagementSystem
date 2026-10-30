
public class Member {
    private int memberId;
    private String name;
    private String department;
    private String phone;

    public Member(int memberId, String name, String department, String phone) {
        this.memberId = memberId;
        this.name = name;
        this.department = department;
        this.phone = phone;
    }

    public Member(String name, String department, String phone) {
        this(0, name, department, phone);
    }

    public int getMemberId() {
        return memberId;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public String getPhone() {
        return phone;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        this.name = name.trim();
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "Member ID: " + memberId
                + " | Name: " + name
                + " | Department: " + department
                + " | Phone: " + phone;
    }
}

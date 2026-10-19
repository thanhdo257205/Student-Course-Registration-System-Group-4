package java03.group4.auth;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer id;

    @Column(nullable = false, length = 50, unique = true)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 20)
    private String role = "ADMIN";

    @Column(name = "student_id")
    private Integer studentId;

    public User() {
    }

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role != null ? role : "ADMIN";
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public boolean isStudent() {
        return "STUDENT".equalsIgnoreCase(role) || "USER".equalsIgnoreCase(role);
    }

    public boolean isStaff() {
        return "ADMIN".equalsIgnoreCase(role)
                || "SCHOOL_STAFF".equalsIgnoreCase(role)
                || "STAFF".equalsIgnoreCase(role);
    }

    public String getLandingPath() {
        return isStudent() ? "/catalog" : "/students";
    }
}

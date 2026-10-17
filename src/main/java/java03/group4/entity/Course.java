package java03.group4.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Integer id;

    @Column(name = "course_name", nullable = false, length = 100)
    private String courseName;

    @Column(name = "instructor_name", nullable = false, length = 100)
    private String instructorName;

    @Column(nullable = false)
    private Integer capacity;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getInstructorName() { return instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
}

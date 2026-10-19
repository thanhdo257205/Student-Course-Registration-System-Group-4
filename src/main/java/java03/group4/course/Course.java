package java03.group4.course;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Integer courseId;

    @Column(name = "course_name", nullable = false, length = 100)
    private String courseName;

    @Column(name = "instructor_name", nullable = false, length = 100)
    private String instructorName;

    @Column(nullable = false)
    private Integer capacity;

    protected Course() {
    }

    public Course(String courseName, String instructorName, Integer capacity) {
        this.courseName = courseName;
        this.instructorName = instructorName;
        this.capacity = capacity;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
}

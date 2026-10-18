package java03.group4;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CourseForm {

    @NotBlank(message = "Enter a course name.")
    @Size(max = 100, message = "Course name must be 100 characters or fewer.")
    private String courseName;

    @NotBlank(message = "Enter an instructor name.")
    @Size(max = 100, message = "Instructor name must be 100 characters or fewer.")
    private String instructorName;

    @NotNull(message = "Enter a course capacity.")
    @Min(value = 1, message = "Capacity must be at least 1.")
    private Integer capacity;

    public CourseForm() {
    }

    public CourseForm(String courseName, String instructorName, Integer capacity) {
        this.courseName = courseName;
        this.instructorName = instructorName;
        this.capacity = capacity;
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

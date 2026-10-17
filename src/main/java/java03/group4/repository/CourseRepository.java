package java03.group4.repository;

import java03.group4.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Integer> {

    List<Course> findByCourseNameContainingIgnoreCaseOrInstructorNameContainingIgnoreCaseOrderByCourseName(
            String courseName, String instructorName);
}

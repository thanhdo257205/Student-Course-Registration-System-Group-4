package java03.group4;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Integer> {

    boolean existsByStudent_StudentIdAndCourse_CourseId(Integer studentId, Integer courseId);

    boolean existsByStudent_StudentIdAndCourse_CourseIdAndEnrollmentIdNot(
            Integer studentId, Integer courseId, Integer enrollmentId);

    boolean existsByStudent_StudentId(Integer studentId);

    boolean existsByCourse_CourseId(Integer courseId);

    long countByCourse_CourseId(Integer courseId);

    @Query("""
            select e.course.courseId as courseId, count(e) as enrollmentCount
            from Enrollment e
            where e.course.courseId in :courseIds
            group by e.course.courseId
            """)
    List<CourseEnrollmentSummary> countPerCourse(@Param("courseIds") List<Integer> courseIds);
}

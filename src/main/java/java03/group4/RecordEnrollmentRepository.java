package java03.group4;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecordEnrollmentRepository extends JpaRepository<Enrollment, Integer> {

    @Query(
            value = """
                    select e from Enrollment e
                    join fetch e.student
                    join fetch e.course
                    where :query = ''
                       or lower(e.student.fullName) like lower(concat('%', :query, '%'))
                       or lower(e.student.email) like lower(concat('%', :query, '%'))
                       or lower(e.course.courseName) like lower(concat('%', :query, '%'))
                       or lower(e.course.instructorName) like lower(concat('%', :query, '%'))
                    """,
            countQuery = """
                    select count(e) from Enrollment e
                    where :query = ''
                       or lower(e.student.fullName) like lower(concat('%', :query, '%'))
                       or lower(e.student.email) like lower(concat('%', :query, '%'))
                       or lower(e.course.courseName) like lower(concat('%', :query, '%'))
                       or lower(e.course.instructorName) like lower(concat('%', :query, '%'))
                    """)
    Page<Enrollment> search(@Param("query") String query, Pageable pageable);

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

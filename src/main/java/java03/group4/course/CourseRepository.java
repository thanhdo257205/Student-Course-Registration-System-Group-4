package java03.group4.course;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Integer> {

    @Query("""
            select c from Course c
            where :query = ''
               or lower(c.courseName) like lower(concat('%', :query, '%'))
               or lower(c.instructorName) like lower(concat('%', :query, '%'))
            """)
    Page<Course> search(@Param("query") String query, Pageable pageable);

    List<Course> findByCourseNameContainingIgnoreCaseOrInstructorNameContainingIgnoreCaseOrderByCourseName(
            String courseName, String instructorName);

    // Khoa row course den het transaction, request dang ky thu 2 vao cung khoa phai cho
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Course c where c.courseId = :id")
    Optional<Course> findByIdForUpdate(@Param("id") Integer id);
}

package java03.group4;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecordCourseRepository extends JpaRepository<Course, Integer> {

    @Query("""
            select c from RecordCourse c
            where :query = ''
               or lower(c.courseName) like lower(concat('%', :query, '%'))
               or lower(c.instructorName) like lower(concat('%', :query, '%'))
            """)
    Page<Course> search(@Param("query") String query, Pageable pageable);
}

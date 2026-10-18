package java03.group4;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecordStudentRepository extends JpaRepository<Student, Integer> {

    @Query("""
            select s from Student s
            where :query = ''
               or lower(s.fullName) like lower(concat('%', :query, '%'))
               or lower(s.email) like lower(concat('%', :query, '%'))
            """)
    Page<Student> search(@Param("query") String query, Pageable pageable);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndStudentIdNot(String email, Integer studentId);
}

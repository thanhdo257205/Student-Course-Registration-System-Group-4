package java03.group4.repository;

import java03.group4.entity.Enrollment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Integer> {

    long countByCourse_Id(Integer courseId);

    // Lay kem student trong 1 query, tranh N+1 khi hien danh sach
    @EntityGraph(attributePaths = "student")
    List<Enrollment> findByCourse_IdOrderByEnrollmentDateDescIdDesc(Integer courseId);

    boolean existsByStudent_IdAndCourse_Id(Integer studentId, Integer courseId);

    // Moi phan tu: [courseId, so luong da dang ky]
    @Query("select e.course.id, count(e) from Enrollment e group by e.course.id")
    List<Object[]> countPerCourse();
}

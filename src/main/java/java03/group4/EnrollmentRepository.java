package java03.group4;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Integer> {

    boolean existsByStudent_StudentIdAndCourse_CourseId(Integer studentId, Integer courseId);

    boolean existsByStudent_StudentIdAndCourse_CourseIdAndEnrollmentIdNot(
            Integer studentId, Integer courseId, Integer enrollmentId);

    boolean existsByStudent_StudentId(Integer studentId);

    boolean existsByCourse_CourseId(Integer courseId);

    long countByCourse_CourseId(Integer courseId);
}

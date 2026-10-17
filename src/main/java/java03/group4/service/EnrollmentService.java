package java03.group4.service;

import java03.group4.entity.Course;
import java03.group4.entity.Enrollment;
import java03.group4.entity.Student;
import java03.group4.repository.CourseRepository;
import java03.group4.repository.EnrollmentRepository;
import java03.group4.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class EnrollmentService {

    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentService(CourseRepository courseRepository, StudentRepository studentRepository,
                             EnrollmentRepository enrollmentRepository) {
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    /** courseId -> so sinh vien da dang ky (khoa chua ai dang ky thi khong co trong map). */
    public Map<Integer, Long> enrolledCounts() {
        Map<Integer, Long> counts = new HashMap<>();
        for (Object[] row : enrollmentRepository.countPerCourse()) {
            counts.put((Integer) row[0], (Long) row[1]);
        }
        return counts;
    }

    /** Dang ky sinh vien vao khoa hoc. Loi nghiep vu nem IllegalArgumentException kem thong bao cho nguoi dung. */
    @Transactional
    public void register(Integer studentId, Integer courseId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sinh viên."));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khóa học."));
        if (enrollmentRepository.existsByStudent_IdAndCourse_Id(studentId, courseId)) {
            throw new IllegalArgumentException("Sinh viên đã đăng ký khóa học này.");
        }
        // shortcut: dem roi moi luu, 2 request dong thoi vao cho cuoi co the vuot capacity; can lock row course neu nhieu nguoi dung that
        if (enrollmentRepository.countByCourse_Id(courseId) >= course.getCapacity()) {
            throw new IllegalArgumentException("Khóa học đã đủ chỗ.");
        }
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollmentRepository.save(enrollment);
    }
}

package java03.group4.enrollment;

import java03.group4.course.Course;
import java03.group4.course.CourseRepository;
import java03.group4.student.Student;
import java03.group4.student.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

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
        return enrollmentRepository.countAllPerCourse().stream()
                .collect(Collectors.toMap(
                        CourseEnrollmentSummary::getCourseId,
                        CourseEnrollmentSummary::getEnrollmentCount));
    }

    /** Dang ky sinh vien vao khoa hoc. Loi nghiep vu nem IllegalArgumentException kem thong bao cho nguoi dung. */
    @Transactional
    public void register(Integer studentId, Integer courseId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sinh viên."));
        // Chan ca session da dang nhap truoc khi sinh vien bi chuyen sang inactive
        if (!student.isActive()) {
            throw new IllegalArgumentException("Tài khoản sinh viên đã bị khóa.");
        }
        // Lock course truoc khi dem, de 2 request dong thoi khong cung lay duoc cho cuoi
        Course course = courseRepository.findByIdForUpdate(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khóa học."));
        if (enrollmentRepository.existsByStudent_StudentIdAndCourse_CourseId(studentId, courseId)) {
            throw new IllegalArgumentException("Sinh viên đã đăng ký khóa học này.");
        }
        if (enrollmentRepository.countByCourse_CourseId(courseId) >= course.getCapacity()) {
            throw new IllegalArgumentException("Khóa học đã đủ chỗ.");
        }
        enrollmentRepository.save(new Enrollment(student, course, LocalDate.now()));
    }
}

package wildtrack.example.wildtrackbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wildtrack.example.wildtrackbackend.entity.LibraryRequirementProgress;
import wildtrack.example.wildtrackbackend.entity.User;
import wildtrack.example.wildtrackbackend.repository.LibraryRequirementProgressRepository;
import wildtrack.example.wildtrackbackend.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/students")

public class TeacherStudentController {
    private static final Logger logger = Logger.getLogger(TeacherStudentController.class.getName());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LibraryRequirementProgressRepository progressRepository;

    /**
     * Get students filtered by role, grade level, and subject
     */
    @GetMapping
    public ResponseEntity<?> getStudents(
            @RequestParam(defaultValue = "Student") String role,
            @RequestParam(required = false) String gradeLevel,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String quarter) {
        try {
            logger.info("Getting students with role=" + role +
                    ", gradeLevel=" + gradeLevel +
                    ", subject=" + subject +
                    ", quarter=" + quarter);

            // First, get students by role and grade level
            List<User> students;
            if (gradeLevel != null && !gradeLevel.isEmpty()) {
                students = userRepository.findByRoleAndGrade(role, gradeLevel);
            } else {
                students = userRepository.findByRole(role);
            }

            List<Map<String, Object>> result = new ArrayList<>();

            // For each student, check if they have progress for the specified subject and
            // quarter
            for (User student : students) {
                List<LibraryRequirementProgress> progressRecords;

                // Filter progress records based on subject and quarter
                if (subject != null && !subject.isEmpty() && quarter != null && !quarter.isEmpty()) {
                    progressRecords = progressRepository.findByStudentIdAndSubjectAndQuarter(
                            student.getIdNumber(), subject, quarter);
                } else if (subject != null && !subject.isEmpty()) {
                    progressRecords = progressRepository.findByStudentIdAndSubject(
                            student.getIdNumber(), subject);
                } else if (quarter != null && !quarter.isEmpty()) {
                    progressRecords = progressRepository.findByStudentIdAndQuarter(
                            student.getIdNumber(), quarter);
                } else {
                    // If no subject or quarter specified, get all progress records
                    progressRecords = progressRepository.findByStudentId(student.getIdNumber());
                }

                // If the student has no matching progress records, skip them
                if (progressRecords.isEmpty() && (subject != null || quarter != null)) {
                    continue;
                }

                // For each progress record, create a student entry
                if (progressRecords.isEmpty()) {
                    // If no progress records but no filters applied, include student with basic
                    // info
                    Map<String, Object> studentData = new HashMap<>();
                    studentData.put("idNumber", student.getIdNumber());
                    studentData.put("firstName", student.getFirstName());
                    studentData.put("lastName", student.getLastName());
                    studentData.put("grade", student.getGrade());
                    studentData.put("section", student.getSection());
                    studentData.put("gradeSection", student.getGrade() + " " + student.getSection());
                    studentData.put("progress", "Not started");

                    if (subject != null && !subject.isEmpty()) {
                        studentData.put("subject", subject);
                    }

                    result.add(studentData);
                } else {
                    // For each progress record, create a student entry
                    for (LibraryRequirementProgress progress : progressRecords) {
                        Map<String, Object> studentData = new HashMap<>();
                        studentData.put("idNumber", student.getIdNumber());
                        studentData.put("firstName", student.getFirstName());
                        studentData.put("lastName", student.getLastName());
                        studentData.put("grade", student.getGrade());
                        studentData.put("section", student.getSection());
                        studentData.put("gradeSection", student.getGrade() + " " + student.getSection());

                        // Add progress details
                        studentData.put("subject", progress.getSubject());
                        studentData.put("quarter", progress.getQuarter());
                        studentData.put("minutesRendered", progress.getMinutesRendered());
                        studentData.put("requiredMinutes", progress.getRequiredMinutes());

                        // Determine progress status
                        if (progress.getIsCompleted()) {
                            studentData.put("progress", "Completed");
                        } else if (progress.getMinutesRendered() > 0) {
                            studentData.put("progress", "In-progress");
                        } else {
                            studentData.put("progress", "Not started");
                        }

                        result.add(studentData);
                    }
                }
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.severe("Error getting students: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch students: " + e.getMessage()));
        }
    }
}
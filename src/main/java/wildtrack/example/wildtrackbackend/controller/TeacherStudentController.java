package wildtrack.example.wildtrackbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wildtrack.example.wildtrackbackend.entity.LibraryRequirementProgress;
import wildtrack.example.wildtrackbackend.entity.User;
import wildtrack.example.wildtrackbackend.repository.LibraryRequirementProgressRepository;
import wildtrack.example.wildtrackbackend.repository.TimeInRepository;
import wildtrack.example.wildtrackbackend.repository.UserRepository;
import wildtrack.example.wildtrackbackend.service.LibraryRequirementProgressService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/students")
public class TeacherStudentController {
    private static final Logger logger = Logger.getLogger(TeacherStudentController.class.getName());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LibraryRequirementProgressRepository progressRepository;

    @Autowired
    private TimeInRepository timeInRepository; // Add TimeInRepository to check active sessions
    @Autowired
    private LibraryRequirementProgressService libraryRequirementProgressService; // Add this

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
                // Initialize requirements for this student
                libraryRequirementProgressService.initializeRequirements(student.getIdNumber());
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

                // Check if student has an active time-in session
                boolean hasActiveSession = timeInRepository.existsByIdNumberAndTimeOutIsNull(student.getIdNumber());

                // Always include students matching the basic filters, even if they have no
                // progress records
                if (progressRecords.isEmpty()) {
                    // If no progress records, include student with basic info
                    Map<String, Object> studentData = new HashMap<>();
                    studentData.put("idNumber", student.getIdNumber());
                    studentData.put("firstName", student.getFirstName());
                    studentData.put("lastName", student.getLastName());
                    studentData.put("grade", student.getGrade());
                    studentData.put("section", student.getSection());
                    studentData.put("gradeSection", student.getGrade() + " " + student.getSection());
                    studentData.put("minutesRendered", 0); // Added for frontend reference

                    // Set progress status - mark as "In-progress" if they have an active session
                    studentData.put("progress", hasActiveSession ? "In-progress" : "Not started");

                    if (subject != null && !subject.isEmpty()) {
                        studentData.put("subject", subject);
                    }

                    if (quarter != null && !quarter.isEmpty()) {
                        studentData.put("quarter", quarter);
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

                        // Determine progress status with corrected logic
                        if (progress.getIsCompleted()) {
                            studentData.put("progress", "Completed");
                        } else if (progress.getMinutesRendered() > 0 || hasActiveSession) {
                            // Mark as "In-progress" if either:
                            // 1. They have any minutes recorded, OR
                            // 2. They have an active time-in session
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
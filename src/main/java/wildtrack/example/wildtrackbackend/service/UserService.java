package wildtrack.example.wildtrackbackend.service;

import java.util.List;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import wildtrack.example.wildtrackbackend.entity.User;
import wildtrack.example.wildtrackbackend.repository.UserRepository;
import wildtrack.example.wildtrackbackend.service.PasswordValidationService.PasswordValidationResult;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private PasswordValidationService passwordValidationService;

    public List<User> getTeachersByGradeLevel(String gradeLevel) {
        // Find teachers who have the specified grade level assigned
        return userRepository.findByRoleAndGrade("Teacher", gradeLevel);
    }

    public boolean isIdNumberExists(String idNumber) {
        return userRepository.existsByIdNumber(idNumber);
    }

    // Add validation methods
    public boolean isValidName(String name) {
        return name != null && name.matches("^[A-Za-z\\s]+$");
    }

    public boolean isValidIdNumber(String idNumber) {
        return idNumber != null && idNumber.matches("^[0-9-]+$");
    }

    // New method to generate a default password
    public String generateDefaultPassword(User user) {
        // Create a default password pattern (ID + first letter of first name + first
        // letter of last name + fixed string)
        String defaultPassword = user.getIdNumber().substring(0, Math.min(4, user.getIdNumber().length()))
                + user.getFirstName().substring(0, 1).toUpperCase()
                + user.getLastName().substring(0, 1).toUpperCase()
                + "@CITLib!";

        // Validate the password meets requirements
        PasswordValidationResult validationResult = passwordValidationService.validatePassword(defaultPassword);
        if (!validationResult.isValid()) {
            // Fallback to a secure default if the pattern doesn't meet requirements
            defaultPassword = "Change@" + user.getIdNumber().substring(0, Math.min(4, user.getIdNumber().length()))
                    + "!";
        }

        return defaultPassword;
    }

    public void updateProfilePicture(Long userId, String profilePictureUrl) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        // If user already has a profile picture, delete the old one
        if (user.getProfilePictureUrl() != null) {
            String oldFileName = user.getProfilePictureUrl().substring(
                    user.getProfilePictureUrl().lastIndexOf('/') + 1);
            fileStorageService.deleteFile(oldFileName);
        }

        user.setProfilePictureUrl(profilePictureUrl);
        userRepository.save(user);
    }

    public void removeProfilePicture(Long userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        if (user.getProfilePictureUrl() != null) {
            String fileName = user.getProfilePictureUrl().substring(
                    user.getProfilePictureUrl().lastIndexOf('/') + 1);
            fileStorageService.deleteFile(fileName);
            user.setProfilePictureUrl(null);
            userRepository.save(user);
        }
    }

    public void changePassword(Long id, String currentPassword, String newPassword) throws Exception {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new Exception("User not found with id: " + id));

        // Verify the current password (skip for reset flow)
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!user.isPasswordResetRequired() && !encoder.matches(currentPassword, user.getPassword())) {
            throw new Exception("Current password is incorrect.");
        }

        // Validate the new password
        PasswordValidationResult validationResult = passwordValidationService.validatePassword(newPassword);
        if (!validationResult.isValid()) {
            throw new Exception(validationResult.getErrorMessage());
        }

        // Update the password
        user.setPassword(encoder.encode(newPassword));

        // Clear the reset flag
        user.setPasswordResetRequired(false);

        userRepository.save(user);
    }

    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role); // Fetch users filtered by role
    }

    /**
     * Returns a filtered list of students based on grade level and section
     */
    public List<User> getStudentsByGradeAndSection(String gradeLevel, String section) {
        List<User> allStudents = userRepository.findByRole("Student");

        // Apply filters if provided
        return allStudents.stream()
                .filter(student -> (gradeLevel == null || gradeLevel.isEmpty() || gradeLevel.equals(student.getGrade()))
                        &&
                        (section == null || section.isEmpty() || section.equals(student.getSection())))
                .toList();
    }

    /**
     * Returns the count of students based on grade level and section
     */
    public long getStudentsCountByGradeAndSection(String gradeLevel, String section) {
        List<User> allStudents = userRepository.findByRole("Student");

        // If no filters, return all students
        if ((gradeLevel == null || gradeLevel.isEmpty()) && (section == null || section.isEmpty())) {
            return allStudents.size();
        }

        // Apply filters
        return allStudents.stream()
                .filter(student -> (gradeLevel == null || gradeLevel.isEmpty() || gradeLevel.equals(student.getGrade()))
                        &&
                        (section == null || section.isEmpty() || section.equals(student.getSection())))
                .count();
    }

    public User saveUser(User user) throws Exception {
        // Validate names
        if (!isValidName(user.getFirstName())) {
            throw new Exception("First name should contain letters only.");
        }

        if (user.getMiddleName() != null && !user.getMiddleName().isEmpty() && !isValidName(user.getMiddleName())) {
            throw new Exception("Middle name should contain letters only.");
        }

        if (!isValidName(user.getLastName())) {
            throw new Exception("Last name should contain letters only.");
        }

        // Validate ID number
        if (!isValidIdNumber(user.getIdNumber())) {
            throw new Exception("ID Number should contain only numbers and dashes.");
        }

        // Set creation timestamp
        user.setCreatedAt(LocalDateTime.now());

        // Generate password if none provided or empty
        String clearTextPassword;
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            clearTextPassword = generateDefaultPassword(user);
            user.setPassword(clearTextPassword);
            // Set flag requiring password change on first login
            user.setPasswordResetRequired(true);
        } else {
            clearTextPassword = user.getPassword();
            // Validate password if manually provided
            PasswordValidationResult validationResult = passwordValidationService.validatePassword(user.getPassword());
            if (!validationResult.isValid()) {
                throw new Exception(validationResult.getErrorMessage());
            }
        }

        // Encrypt the password
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(user.getPassword()));

        // Save user to database
        User savedUser = userRepository.save(user);

        // Set back the clear text password for the return value
        // This doesn't affect what's saved in the database
        savedUser.setPassword(clearTextPassword);

        return savedUser;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User findByIdNumber(String idNumber) {
        return userRepository.findByIdNumber(idNumber).orElse(null);
    }

    public User updateUser(Long id, User updatedUserDetails) throws Exception {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new Exception("User not found with id: " + id));

        // Validate names
        if (!isValidName(updatedUserDetails.getFirstName())) {
            throw new Exception("First name should contain letters only.");
        }

        if (updatedUserDetails.getMiddleName() != null && !updatedUserDetails.getMiddleName().isEmpty()
                && !isValidName(updatedUserDetails.getMiddleName())) {
            throw new Exception("Middle name should contain letters only.");
        }

        if (!isValidName(updatedUserDetails.getLastName())) {
            throw new Exception("Last name should contain letters only.");
        }

        // Validate ID number
        if (!isValidIdNumber(updatedUserDetails.getIdNumber())) {
            throw new Exception("ID Number should contain only numbers and dashes.");
        }

        existingUser.setFirstName(updatedUserDetails.getFirstName());
        existingUser.setLastName(updatedUserDetails.getLastName());
        existingUser.setMiddleName(updatedUserDetails.getMiddleName());

        existingUser.setRole(updatedUserDetails.getRole());
        existingUser.setIdNumber(updatedUserDetails.getIdNumber());
        existingUser.setGrade(updatedUserDetails.getGrade());
        existingUser.setSection(updatedUserDetails.getSection());
        existingUser.setQuarter(updatedUserDetails.getQuarter());
        existingUser.setSubject(updatedUserDetails.getSubject());
        existingUser.setWorkPeriod(updatedUserDetails.getWorkPeriod());
        existingUser.setAssignedTask(updatedUserDetails.getAssignedTask());
        existingUser.setAcademicYear(updatedUserDetails.getAcademicYear());

        // Add position and department updates
        existingUser.setPosition(updatedUserDetails.getPosition());
        existingUser.setDepartment(updatedUserDetails.getDepartment());

        return userRepository.save(existingUser);
    }

    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    public User getUserByIdNumber(String idNumber) {
        return userRepository.findByIdNumber(idNumber).orElse(null);
    }

    public long getStudentsCount() {
        // Return count of all registered students
        return userRepository.countByRole("Student");
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public void resetPassword(Long userId, String tempPassword) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("User not found with id: " + userId));

        // Set new temporary password
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(tempPassword));

        // Set the flag to indicate password reset is required
        user.setPasswordResetRequired(true);

        userRepository.save(user);
    }
}
package wildtrack.example.wildtrackbackend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import wildtrack.example.wildtrackbackend.entity.User;
import wildtrack.example.wildtrackbackend.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

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

        // Verify the current password
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(currentPassword, user.getPassword())) {
            throw new Exception("Current password is incorrect.");
        }

        // Update the password
        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);
    }

    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role); // Fetch users filtered by role
    }

    /**
     * Returns a filtered list of students based on grade level and section
     * 
     * @param gradeLevel The grade level to filter by, or null for all grades
     * @param section    The section to filter by, or null for all sections
     * @return Filtered list of students
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
     * 
     * @param gradeLevel The grade level to filter by, or null for all grades
     * @param section    The section to filter by, or null for all sections
     * @return Number of students matching the criteria
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

    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email); // This must return true if the email exists
    }

    public void saveUser(User user) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(user.getPassword()));
        userRepository.save(user);
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
}
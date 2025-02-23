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
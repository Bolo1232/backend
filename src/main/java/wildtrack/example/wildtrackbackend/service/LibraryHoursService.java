package wildtrack.example.wildtrackbackend.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import wildtrack.example.wildtrackbackend.dto.StudentLibrarySummary;
import wildtrack.example.wildtrackbackend.entity.LibraryHours;
import wildtrack.example.wildtrackbackend.entity.User;
import wildtrack.example.wildtrackbackend.repository.LibraryHoursRepository;
import wildtrack.example.wildtrackbackend.repository.UserRepository;

@Service
public class LibraryHoursService {

    @Autowired
    private LibraryHoursRepository libraryHoursRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LibraryRequirementProgressService libraryRequirementProgressService;

    public List<Map<String, Object>> getTotalMinutesSpentByUser(
            String idNumber, String dateFrom, String dateTo, String academicYear) {

        List<LibraryHours> libraryHoursList = libraryHoursRepository.findByIdNumber(idNumber);

        // Apply filters if provided
        if (dateFrom != null || dateTo != null || academicYear != null) {
            LocalDateTime fromDateTime = dateFrom != null ? LocalDate.parse(dateFrom).atStartOfDay() : null;
            LocalDateTime toDateTime = dateTo != null ? LocalDate.parse(dateTo).atTime(23, 59, 59) : null;

            // Parse academic year if provided (format: "2025-2026")
            Integer academicYearStart = null;
            Integer academicYearEnd = null;

            if (academicYear != null && !academicYear.isEmpty()) {
                String[] years = academicYear.split("-");
                if (years.length == 2) {
                    try {
                        academicYearStart = Integer.parseInt(years[0]);
                        academicYearEnd = Integer.parseInt(years[1]);
                    } catch (NumberFormatException e) {
                        // Invalid format, ignore academic year filter
                        System.err.println("Invalid academic year format: " + academicYear);
                    }
                }
            }

            final Integer startYear = academicYearStart;
            final Integer endYear = academicYearEnd;

            libraryHoursList = libraryHoursList.stream()
                    .filter(hours -> {
                        boolean includeHours = true;

                        // Filter by date range
                        if (fromDateTime != null && hours.getTimeIn().isBefore(fromDateTime)) {
                            includeHours = false;
                        }
                        if (toDateTime != null && hours.getTimeIn().isAfter(toDateTime)) {
                            includeHours = false;
                        }

                        // Filter by academic year years if provided
                        if (startYear != null && endYear != null) {
                            int logYear = hours.getTimeIn().getYear();
                            if (logYear != startYear && logYear != endYear) {
                                includeHours = false;
                            }
                        }

                        return includeHours;
                    })
                    .collect(Collectors.toList());
        }

        Map<String, Integer> minutesSpentByMonth = new HashMap<>();

        for (LibraryHours libraryHours : libraryHoursList) {
            LocalDateTime dateTime = libraryHours.getTimeIn();
            String monthYear = dateTime.format(DateTimeFormatter.ofPattern("MMM yyyy"));

            LocalDateTime timeIn = libraryHours.getTimeIn();
            LocalDateTime timeOut = libraryHours.getTimeOut();

            if (timeIn != null && timeOut != null) {
                int minutesSpent = (int) Duration.between(timeIn, timeOut).toMinutes();
                minutesSpentByMonth.put(monthYear, minutesSpentByMonth.getOrDefault(monthYear, 0) + minutesSpent);
            }
        }

        List<Map<String, Object>> minutesSpentData = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : minutesSpentByMonth.entrySet()) {
            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("month", entry.getKey());
            dataPoint.put("minutesSpent", entry.getValue());
            minutesSpentData.add(dataPoint);
        }

        return minutesSpentData;
    }

    public List<StudentLibrarySummary> getLibraryHoursSummary() {
        List<User> users = userRepository.findAll();
        List<StudentLibrarySummary> summaries = new ArrayList<>();

        // Formatter for 12-hour time with AM/PM
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a");

        for (User user : users) {
            List<LibraryHours> libraryHoursList = libraryHoursRepository.findByIdNumber(user.getIdNumber());

            if (!libraryHoursList.isEmpty()) {
                LibraryHours latestRecord = libraryHoursList.get(libraryHoursList.size() - 1);

                // Calculate total minutes
                long totalMinutes = libraryHoursList.stream()
                        .mapToLong(hours -> {
                            if (hours.getTimeIn() != null && hours.getTimeOut() != null) {
                                return Duration.between(hours.getTimeIn(), hours.getTimeOut()).toMinutes();
                            }
                            return 0;
                        }).sum();

                // Create summary
                StudentLibrarySummary summary = new StudentLibrarySummary();
                summary.setIdNumber(user.getIdNumber());
                summary.setFirstName(user.getFirstName());
                summary.setLastName(user.getLastName());
                summary.setLatestLibraryHourDate(latestRecord.getTimeIn().toLocalDate().toString());
                summary.setLatestTimeIn(latestRecord.getTimeIn().format(timeFormatter)); // Format as 12-hour time
                summary.setLatestTimeOut(latestRecord.getTimeOut() != null
                        ? latestRecord.getTimeOut().format(timeFormatter)
                        : "N/A");
                summary.setTotalMinutes(String.valueOf(totalMinutes));

                summaries.add(summary);
            }
        }

        return summaries;
    }

    // Record a time-in entry
    public void recordTimeIn(String idNumber) {
        // Check for an open time-in record
        LibraryHours openTimeIn = libraryHoursRepository.findLatestByIdNumber(idNumber)
                .filter(libraryHours -> libraryHours.getTimeOut() == null)
                .orElse(null);

        if (openTimeIn != null) {
            throw new RuntimeException(
                    "You already have a time-in recorded without a time-out. Please record a time-out before clocking in again.");
        }

        // Create a new time-in record
        LibraryHours libraryHours = new LibraryHours();
        libraryHours.setIdNumber(idNumber);
        libraryHours.setTimeIn(LocalDateTime.now());
        libraryHoursRepository.save(libraryHours);
    }

    // Record a time-out entry with automatic requirement crediting
    @Transactional
    public void recordTimeOut(String idNumber) {
        LibraryHours libraryHours = libraryHoursRepository.findLatestByIdNumber(idNumber)
                .orElseThrow(() -> new RuntimeException("No open time-in record found for this student."));

        if (libraryHours.getTimeOut() != null) {
            throw new RuntimeException("Time-out has already been recorded for the latest time-in.");
        }

        // Record time-out
        libraryHours.setTimeOut(LocalDateTime.now());

        // Calculate minutes
        LocalDateTime timeIn = libraryHours.getTimeIn();
        LocalDateTime timeOut = libraryHours.getTimeOut();
        int minutes = (int) Duration.between(timeIn, timeOut).toMinutes();

        // Save the calculated minutes to the record
        libraryHours.setMinutesCounted(minutes);

        // Set isCounted flag to true
        libraryHours.setIsCounted(true);

        // Save the record
        LibraryHours savedHours = libraryHoursRepository.save(libraryHours);

        // Automatically call the requirement progress service to allocate these minutes
        // This will handle crediting the minutes to the appropriate requirement
        libraryRequirementProgressService.recordLibraryTime(savedHours.getId());
    }

    // Fetch all library hours
    public List<LibraryHours> getAllLibraryHours() {
        return libraryHoursRepository.findAll();
    }

    // Fetch library hours by user ID number
    public List<LibraryHours> getLibraryHoursByIdNumber(String idNumber) {
        return libraryHoursRepository.findByIdNumber(idNumber);
    }

    // Fetch all library hours with user details
    public List<Map<String, Object>> getAllLibraryHoursWithUserDetails() {
        List<LibraryHours> libraryHoursList = libraryHoursRepository.findAll();
        List<Map<String, Object>> response = new ArrayList<>();

        for (LibraryHours libraryHours : libraryHoursList) {
            User user = userService.findByIdNumber(libraryHours.getIdNumber());
            Map<String, Object> entry = new HashMap<>();
            entry.put("id", libraryHours.getId());
            entry.put("idNumber", libraryHours.getIdNumber());
            entry.put("firstName", user != null ? user.getFirstName() : "Unknown");
            entry.put("lastName", user != null ? user.getLastName() : "Unknown");
            entry.put("gradeSection", user != null ? (user.getGrade() + " - " + user.getSection()) : "N/A");
            entry.put("timeIn", libraryHours.getTimeIn());
            entry.put("timeOut", libraryHours.getTimeOut());
            entry.put("status", libraryHours.getTimeOut() != null ? "Present" : "Incomplete");
            response.add(entry);
        }
        return response;
    }

    public List<Map<String, Object>> getCompletedLibraryHours() {
        List<LibraryHours> libraryHoursList = libraryHoursRepository.findAll();
        List<Map<String, Object>> response = new ArrayList<>();

        for (LibraryHours libraryHours : libraryHoursList) {
            if (libraryHours.getTimeIn() != null && libraryHours.getTimeOut() != null) {
                User user = userService.findByIdNumber(libraryHours.getIdNumber());
                Map<String, Object> entry = new HashMap<>();
                entry.put("idNumber", libraryHours.getIdNumber());
                entry.put("name", user != null ? user.getFirstName() + " " + user.getLastName() : "Unknown");
                entry.put("timeIn", libraryHours.getTimeIn());
                entry.put("timeOut", libraryHours.getTimeOut());
                response.add(entry);
            }
        }
        return response;
    }

    // Fetch a specific library hours record by ID
    public Optional<LibraryHours> getLibraryHoursById(Long id) {
        return libraryHoursRepository.findById(id);
    }

    // Save or update a library hours record
    public LibraryHours saveLibraryHours(LibraryHours libraryHours) {
        return libraryHoursRepository.save(libraryHours);
    }

    public StudentLibrarySummary updateStudentLibrarySummary(String idNumber, StudentLibrarySummary summaryUpdate) {
        // Fetch the latest record by ID Number
        LibraryHours libraryHours = libraryHoursRepository.findLatestByIdNumber(idNumber)
                .orElseThrow(() -> new RuntimeException("No record found for the provided ID number."));

        // Update the entity `LibraryHours` with the new data
        if (summaryUpdate.getLatestLibraryHourDate() != null && !summaryUpdate.getLatestLibraryHourDate().isEmpty()) {
            LocalDateTime newTimeIn = LocalDateTime.parse(
                    summaryUpdate.getLatestLibraryHourDate() + "T" + libraryHours.getTimeIn().toLocalTime().toString());
            libraryHours.setTimeIn(newTimeIn);
        }

        if (summaryUpdate.getLatestTimeIn() != null && !summaryUpdate.getLatestTimeIn().isEmpty()) {
            libraryHours.setTimeIn(LocalDateTime.parse(
                    libraryHours.getTimeIn().toLocalDate() + "T" + summaryUpdate.getLatestTimeIn(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        }

        if (summaryUpdate.getLatestTimeOut() != null && !summaryUpdate.getLatestTimeOut().isEmpty()) {
            libraryHours.setTimeOut(LocalDateTime.parse(
                    libraryHours.getTimeIn().toLocalDate() + "T" + summaryUpdate.getLatestTimeOut(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        }

        // Save the updated entity
        libraryHoursRepository.save(libraryHours);

        // Calculate total minutes and update the DTO
        long completedMinutes = libraryHours.getTimeIn() != null && libraryHours.getTimeOut() != null
                ? Duration.between(libraryHours.getTimeIn(), libraryHours.getTimeOut()).toMinutes()
                : 0;

        StudentLibrarySummary updatedSummary = new StudentLibrarySummary();
        updatedSummary.setIdNumber(idNumber);
        updatedSummary.setLatestLibraryHourDate(libraryHours.getTimeIn().toLocalDate().toString());
        updatedSummary.setLatestTimeIn(libraryHours.getTimeIn().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        updatedSummary.setLatestTimeOut(libraryHours.getTimeOut() != null
                ? libraryHours.getTimeOut().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                : null);
        updatedSummary.setTotalMinutes(String.valueOf(completedMinutes));

        return updatedSummary;
    }
}
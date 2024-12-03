package wildtrack.example.wildtrackbackend.dto;

public class StudentLibrarySummary {
    private String idNumber;
    private String firstName;
    private String lastName;
    private String latestLibraryHourDate;
    private String latestTimeIn;
    private String latestTimeOut;
    private String totalMinutes;

    // Getters and setters
    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLatestLibraryHourDate() {
        return latestLibraryHourDate;
    }

    public void setLatestLibraryHourDate(String latestLibraryHourDate) {
        this.latestLibraryHourDate = latestLibraryHourDate;
    }

    public String getLatestTimeIn() {
        return latestTimeIn;
    }

    public void setLatestTimeIn(String latestTimeIn) {
        this.latestTimeIn = latestTimeIn;
    }

    public String getLatestTimeOut() {
        return latestTimeOut;
    }

    public void setLatestTimeOut(String latestTimeOut) {
        this.latestTimeOut = latestTimeOut;
    }

    public String getTotalMinutes() {
        return totalMinutes;
    }

    public void setTotalMinutes(String totalMinutes) {
        this.totalMinutes = totalMinutes;
    }
}

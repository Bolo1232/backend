package wildtrack.example.wildtrackbackend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "academic_years")
public class AcademicYear {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_year")
    private String startYear;

    @Column(name = "end_year")
    private String endYear;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "startDate", column = @Column(name = "first_quarter_start")),
            @AttributeOverride(name = "endDate", column = @Column(name = "first_quarter_end"))
    })
    private Quarter firstQuarter;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "startDate", column = @Column(name = "second_quarter_start")),
            @AttributeOverride(name = "endDate", column = @Column(name = "second_quarter_end"))
    })
    private Quarter secondQuarter;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "startDate", column = @Column(name = "third_quarter_start")),
            @AttributeOverride(name = "endDate", column = @Column(name = "third_quarter_end"))
    })
    private Quarter thirdQuarter;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "startDate", column = @Column(name = "fourth_quarter_start")),
            @AttributeOverride(name = "endDate", column = @Column(name = "fourth_quarter_end"))
    })
    private Quarter fourthQuarter;

    @Column(name = "status")
    private String status = "Active";

    // Embedded class for Quarter
    @Embeddable
    public static class Quarter {
        private LocalDate startDate;
        private LocalDate endDate;

        // Constructors
        public Quarter() {
        }

        public Quarter(LocalDate startDate, LocalDate endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }

        // Getters and Setters
        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }

        // Equals and HashCode for Quarter
        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Quarter quarter = (Quarter) o;
            return Objects.equals(startDate, quarter.startDate) &&
                    Objects.equals(endDate, quarter.endDate);
        }

        @Override
        public int hashCode() {
            return Objects.hash(startDate, endDate);
        }

        @Override
        public String toString() {
            return "Quarter{" +
                    "startDate=" + startDate +
                    ", endDate=" + endDate +
                    '}';
        }
    }

    // Constructors
    public AcademicYear() {
    }

    // Full Constructor
    public AcademicYear(String startYear, String endYear, Quarter firstQuarter,
            Quarter secondQuarter, Quarter thirdQuarter,
            Quarter fourthQuarter, String status) {
        this.startYear = startYear;
        this.endYear = endYear;
        this.firstQuarter = firstQuarter;
        this.secondQuarter = secondQuarter;
        this.thirdQuarter = thirdQuarter;
        this.fourthQuarter = fourthQuarter;
        this.status = status;
    }

    // Comprehensive Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStartYear() {
        return startYear;
    }

    public void setStartYear(String startYear) {
        this.startYear = startYear;
    }

    public String getEndYear() {
        return endYear;
    }

    public void setEndYear(String endYear) {
        this.endYear = endYear;
    }

    public Quarter getFirstQuarter() {
        return firstQuarter;
    }

    public void setFirstQuarter(Quarter firstQuarter) {
        this.firstQuarter = firstQuarter;
    }

    public Quarter getSecondQuarter() {
        return secondQuarter;
    }

    public void setSecondQuarter(Quarter secondQuarter) {
        this.secondQuarter = secondQuarter;
    }

    public Quarter getThirdQuarter() {
        return thirdQuarter;
    }

    public void setThirdQuarter(Quarter thirdQuarter) {
        this.thirdQuarter = thirdQuarter;
    }

    public Quarter getFourthQuarter() {
        return fourthQuarter;
    }

    public void setFourthQuarter(Quarter fourthQuarter) {
        this.fourthQuarter = fourthQuarter;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Equals and HashCode methods
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AcademicYear that = (AcademicYear) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(startYear, that.startYear) &&
                Objects.equals(endYear, that.endYear) &&
                Objects.equals(firstQuarter, that.firstQuarter) &&
                Objects.equals(secondQuarter, that.secondQuarter) &&
                Objects.equals(thirdQuarter, that.thirdQuarter) &&
                Objects.equals(fourthQuarter, that.fourthQuarter) &&
                Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, startYear, endYear, firstQuarter,
                secondQuarter, thirdQuarter, fourthQuarter, status);
    }

    // ToString method for easy debugging and logging
    @Override
    public String toString() {
        return "AcademicYear{" +
                "id=" + id +
                ", startYear='" + startYear + '\'' +
                ", endYear='" + endYear + '\'' +
                ", firstQuarter=" + firstQuarter +
                ", secondQuarter=" + secondQuarter +
                ", thirdQuarter=" + thirdQuarter +
                ", fourthQuarter=" + fourthQuarter +
                ", status='" + status + '\'' +
                '}';
    }
}
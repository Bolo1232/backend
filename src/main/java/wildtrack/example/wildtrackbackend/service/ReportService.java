package wildtrack.example.wildtrackbackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wildtrack.example.wildtrackbackend.entity.Report;
import wildtrack.example.wildtrackbackend.repository.ReportRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ReportNotificationService reportNotificationService;

    public Report saveReport(Report report) {
        // Save the report
        Report savedReport = reportRepository.save(report);

        // Create notification for librarians
        reportNotificationService.createReportSubmissionNotification(savedReport);

        return savedReport;
    }

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public List<Report> getReportsByUserId(Long userId) {
        return reportRepository.findByUserId(userId);
    }

    public List<Report> getReportsByUserIdNumber(String idNumber) {
        return reportRepository.findByUserIdNumber(idNumber);
    }

    public Report getReportById(Long reportId) {
        Optional<Report> reportOpt = reportRepository.findById(reportId);
        return reportOpt.orElse(null);
    }

    public Report updateReport(Long reportId, Report reportDetails) {
        Optional<Report> reportOpt = reportRepository.findById(reportId);
        if (reportOpt.isPresent()) {
            Report existingReport = reportOpt.get();

            // Update fields
            if (reportDetails.getIssue() != null) {
                existingReport.setIssue(reportDetails.getIssue());
            }

            if (reportDetails.getDescription() != null) {
                existingReport.setDescription(reportDetails.getDescription());
            }

            if (reportDetails.getStatus() != null) {
                existingReport.setStatus(reportDetails.getStatus());
            }

            if (reportDetails.getRole() != null) {
                existingReport.setRole(reportDetails.getRole());
            }

            if (reportDetails.getAdminComments() != null) {
                existingReport.setAdminComments(reportDetails.getAdminComments());
            }

            // Update the date resolved if status is changed to "Resolved"
            if ("Resolved".equals(reportDetails.getStatus())) {
                existingReport.setDateResolved(LocalDateTime.now());
            }

            return reportRepository.save(existingReport);
        }
        return null;
    }

    public Report resolveReport(Long reportId, String adminComments) {
        Optional<Report> reportOpt = reportRepository.findById(reportId);
        if (reportOpt.isPresent()) {
            Report report = reportOpt.get();
            report.setStatus("Resolved");
            report.setDateResolved(LocalDateTime.now());

            if (adminComments != null && !adminComments.isEmpty()) {
                report.setAdminComments(adminComments);
            }

            Report resolvedReport = reportRepository.save(report);

            // Create notification for user who submitted the report
            reportNotificationService.createReportResolutionNotification(resolvedReport);

            return resolvedReport;
        }
        return null;
    }

    public boolean deleteReport(Long reportId) {
        if (reportRepository.existsById(reportId)) {
            reportRepository.deleteById(reportId);
            return true;
        }
        return false;
    }
}
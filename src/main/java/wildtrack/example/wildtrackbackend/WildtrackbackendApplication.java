package wildtrack.example.wildtrackbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "wildtrack.example.wildtrackbackend")
@EnableScheduling
public class WildtrackbackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(WildtrackbackendApplication.class, args);
    }
}

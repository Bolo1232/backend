package wildtrack.example.wildtrackbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "wildtrack.example.wildtrackbackend")
public class WildtrackbackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(WildtrackbackendApplication.class, args);
    }
}


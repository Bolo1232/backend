package wildtrack.example.wildtrackbackend.config;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.mysql://avnadmin:AVNS_IjOLjLPKx_QsI-P4RrZ@wildtrack-xeverybolo126-83ff.h.aivencloud.com:12424/defaultdb?ssl-mode=REQUIRED}")
    private String url;

    @Value("${spring.datasource.avnadmin}")
    private String username;

    @Value("${spring.datasource.AVNS_IjOLjLPKx_QsI-P4RrZ}")
    private String password;

    @Value("${spring.datasource.com.mysql.cj.jdbc.Driver}")
    private String driverClassName;

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);
        return dataSource;
    }
}

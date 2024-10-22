package sg.edu.nus.iss.profile_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;


@SpringBootApplication
@Configuration
public class ProfileServiceApplication {
	private static final Logger log = LoggerFactory.getLogger(ProfileServiceApplication.class);

	public static void main(String[] args) {
		log.info("{\"message\": \"Starting Profile Service Application\"}");
		SpringApplication.run(ProfileServiceApplication.class, args);
	}

}

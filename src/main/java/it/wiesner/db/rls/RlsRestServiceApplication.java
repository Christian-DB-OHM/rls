package it.wiesner.db.rls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@ComponentScan("it.wiesner.db.rls")
@SpringBootApplication
public class RlsRestServiceApplication {

	private static final Logger log = LoggerFactory.getLogger(RlsRestServiceApplication.class);

	public static void main(String[] args) {
		try {
			log.info("Starting RLS REST Service Application...");
			SpringApplication.run(RlsRestServiceApplication.class, args);
			log.info("RLS REST Service Application started successfully");
		} catch (Throwable e) {
			log.error("Failed to start RLS REST Service Application", e);
			e.printStackTrace();
		}
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		log.info("Configuring CORS for /rls/** endpoints");
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/rls/**").allowedOrigins("http://localhost:8080");
				log.info("CORS configuration applied: /rls/** -> http://localhost:8080");
			}
		};
	}

}

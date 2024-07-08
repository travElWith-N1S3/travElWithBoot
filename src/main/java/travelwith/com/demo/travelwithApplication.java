package travelwith.com.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:/app.properties")
public class travelwithApplication {

	public static void main(String[] args) {
		SpringApplication.run(travelwithApplication.class, args);
	}

}
package coolBank.coolBank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CoolBankApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoolBankApplication.class, args);
	}

}

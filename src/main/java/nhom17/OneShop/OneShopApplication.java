package nhom17.OneShop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class OneShopApplication {

	public static void main(String[] args) {
		SpringApplication.run(OneShopApplication.class, args);
	}

}

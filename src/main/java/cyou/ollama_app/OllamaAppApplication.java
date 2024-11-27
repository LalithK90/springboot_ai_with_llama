package cyou.ollama_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class OllamaAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(OllamaAppApplication.class, args);
	}

}

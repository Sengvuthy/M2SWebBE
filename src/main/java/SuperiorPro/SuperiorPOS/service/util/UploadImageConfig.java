package SuperiorPro.SuperiorPOS.service.util;

import java.io.File;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UploadImageConfig {

    @Bean
    public CommandLineRunner createUploadDir() {
        return args -> {
            File dir = new File("/m2sweb-img/uploads/products/");
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                System.out.println("Upload dir created: " + created);
            }
        };
    }
}

package SuperiorPro.SuperiorPOS.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private Environment env;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        System.out.println(">>> Resource handler registered for /uploads/products/**");

        String[] profiles = env.getActiveProfiles();
        boolean isDocker = false;
        for (String profile : profiles) {
            if ("docker".equalsIgnoreCase(profile)) {
                isDocker = true;
                break;
            }
        }

        if (isDocker) {
            // ✅ Docker/Linux path
            registry.addResourceHandler("/uploads/products/**")
                    .addResourceLocations("file:/uploads/products/");
        } else {
            // ✅ Local Windows dev path
            registry.addResourceHandler("/uploads/products/**")
                    .addResourceLocations("file:///C:/uploads/products/");
        }
    }
}

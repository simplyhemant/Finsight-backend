package simply.Finsight_backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import simply.Finsight_backend.entity.User;
import simply.Finsight_backend.enums.Role;
import simply.Finsight_backend.enums.UserStatus;
import simply.Finsight_backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AdminInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner createAdmin() {
        return args -> {

            String adminEmail = "admin@finsight.com";

            if (!userRepository.existsByEmail(adminEmail)) {

                User admin = User.builder()
                        .name("Super Admin")
                        .email(adminEmail)
                        .password(passwordEncoder.encode("admin123"))
                        .role(Role.ADMIN)
                        .status(UserStatus.ACTIVE)
                        .build();

                userRepository.save(admin);

                System.out.println("----- Admin created: admin@finsight.com / admin123");
            }else {
                System.out.println("✅ Admin already exists. Skipping seeding.");
            }
        };
    }
}
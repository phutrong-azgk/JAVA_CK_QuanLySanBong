package HUIT.football;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // THÊM LẠI DÒNG NÀY ĐỂ CÁC NÚT THÊM/SỬA/XÓA (AJAX POST) HOẠT ĐỘNG ĐƯỢC
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register", "/css/**", "/js/**","/error").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}

    /*
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Tắt CSRF để test API post/put/delete
                .csrf(csrf -> csrf.disable())

                // Ép tất cả khách vãng lai (chưa đăng nhập) đều được mặc định mang quyền ADMIN
                .anonymous(anon -> anon
                        .principal("Admin_Debug") // Tên user ảo
                        .authorities("ROLE_ADMIN") // Gắn quyền ADMIN (Thymeleaf sẽ tự hiểu)
                )

                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Ai cũng vào được
                );

        return http.build();
    }
}
     */
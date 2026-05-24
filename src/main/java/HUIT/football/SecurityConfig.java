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
                // Tắt CSRF để các nút Thêm/Sửa/Xóa hoạt động
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register", "/css/**", "/js/**", "/error").permitAll()

                        // Cả ADMIN và NHÂN VIÊN đều vào được trang Quản lý Sân và Gọi Đồ
                        .requestMatchers("/san/**", "/order/**").hasAnyRole("ADMIN", "NHAN_VIEN")

                        // CHỈ ADMIN mới vào được các trang Cấu hình, Thống kê, Quản lý
                        .requestMatchers("/taikhoan/**", "/khuyenmai/**", "/baocao/**", "/khach/**", "/kho/**").hasRole("ADMIN")

                        // Trang cá nhân thì ai đã đăng nhập cũng xem được
                        .requestMatchers("/profile/**").authenticated()

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
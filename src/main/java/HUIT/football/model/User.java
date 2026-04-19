package HUIT.football.model;

import HUIT.football.Role; // Importing your new Enum
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;

    @Column(unique = true)
    private String email;

    // This saves the Role as a String ("ADMIN" or "USER") in the database
    @Enumerated(EnumType.STRING)
    private Role role;
}

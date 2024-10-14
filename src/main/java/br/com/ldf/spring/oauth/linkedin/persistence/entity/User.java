package br.com.ldf.spring.oauth.linkedin.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Table(name = "user")
@Entity(name = "User")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = PRIVATE)
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    Long id;
    @Column(name = "des_name", nullable = false)
    String name;
    @Column(name = "des_email", nullable = false)
    String email;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL) // para fins didaticos utilizamos EAGER, mas o recomendado é LAZY
    List<UserRole> roles;
}

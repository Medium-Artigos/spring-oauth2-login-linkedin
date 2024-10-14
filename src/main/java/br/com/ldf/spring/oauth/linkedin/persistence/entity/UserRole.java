package br.com.ldf.spring.oauth.linkedin.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Table(name = "user_role")
@Entity(name = "UserRole")
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user_role")
    Long id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_user", nullable = false, referencedColumnName = "id_user")
    User user;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_role", nullable = false, referencedColumnName = "id_role")
    Role role;
}

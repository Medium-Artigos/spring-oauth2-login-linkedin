package br.com.ldf.spring.oauth.linkedin.persistence.repository;

import br.com.ldf.spring.oauth.linkedin.persistence.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}

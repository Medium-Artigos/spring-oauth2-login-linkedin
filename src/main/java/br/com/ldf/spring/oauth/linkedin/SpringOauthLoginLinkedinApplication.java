package br.com.ldf.spring.oauth.linkedin;

import br.com.ldf.spring.oauth.linkedin.persistence.entity.Role;
import br.com.ldf.spring.oauth.linkedin.persistence.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringOauthLoginLinkedinApplication implements CommandLineRunner {

	@Autowired
	private RoleRepository roleRepository;

	public static void main(String[] args) {
		SpringApplication.run(SpringOauthLoginLinkedinApplication.class, args);
	}


	// para fins didaticos ao subir a app salvamos manuamente os roles
	@Override
	public void run(String... args) throws Exception {
		roleRepository.save(Role.builder().name(Role.ROLE_USER).build());
		roleRepository.save(Role.builder().name(Role.ROLE_ADMIN).build());
	}
}

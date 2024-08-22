package org.network.backend;

import org.network.backend.book.Book;
import org.network.backend.book.BookRepository;
import org.network.backend.role.Role;
import org.network.backend.role.RoleRepository;
import org.network.backend.user.Token;
import org.network.backend.user.TokenRepository;
import org.network.backend.user.User;
import org.network.backend.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "customAuditor")
@EnableAsync
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	public CommandLineRunner runner(RoleRepository roleRepository, TokenRepository tokenRepository, UserRepository userRepository, PasswordEncoder encoder, BookRepository bookRepository){
		return args -> {
			if(roleRepository.findByName("USER").isEmpty()){
				roleRepository.save(Role.builder().name("USER").build());
			}

			if(userRepository.findAll().isEmpty()){
				User user1 = User.builder()
						.firstName("test")
						.lastName("1")
						.email("test1@email.com")
						.password(encoder.encode("password"))
						.roles(List.of(roleRepository.findByName("USER").get()))
						.build();
				user1 = userRepository.save(user1);
				Token token1 = Token.builder()
						.token("12344556")
						.createdAt(LocalDateTime.now())
						.expiresAt(LocalDateTime.now().plusMinutes(15))
						.validatedAt(LocalDateTime.now())
						.user(user1)
						.build();
				tokenRepository.save(token1);
				user1.setEnabled(true);
				userRepository.save(user1);
			}
		};
	}

}

package de.ollie.cube.gui.security;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.vaadin.flow.spring.security.VaadinWebSecurity;

import de.ollie.cube.core.service.UserService;
import de.ollie.cube.core.service.impl.PasswordEncoder;
import de.ollie.cube.gui.vaadin.LoginView;
import lombok.RequiredArgsConstructor;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration extends VaadinWebSecurity {

	private static final Logger LOGGER = LogManager.getLogger(SecurityConfiguration.class);

	private final PasswordEncoder passwordEncoder;
	private final UserService userService;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests(
						auth -> auth
								.requestMatchers(AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/images/*.png"))
								.permitAll());
		super.configure(http);
		setLoginView(http, LoginView.class);
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		// Customize your WebSecurity configuration.
		super.configure(web);
	}

	@Bean
	public UserDetailsManager userDetailsService() {
		List<UserDetails> userDetails =
				userService
						.findAll()
						.stream()
						.filter(u -> getEncodedPassword(u) != null)
						.map(
								u -> User
										.withUsername(u.getName())
										.password("{noop}" + getEncodedPassword(u))
										.roles("USER")
										.build())
						.toList();
		return new InMemoryUserDetailsManager(userDetails);
	}

	private String getEncodedPassword(de.ollie.cube.core.model.User user) {
		try {
			return passwordEncoder.decode(user.getPassword());
		} catch (Exception e) {
			LOGGER.warn("User ignored with name: " + user.getName());
			return null;
		}
	}
}
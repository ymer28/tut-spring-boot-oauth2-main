/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
// To enable HTTP Security in Spring, we need to extend the WebSecurityConfigurerAdapter to provide a
// default configuration in the configure(HttpSecurity http) method
public class SocialApplication extends WebSecurityConfigurerAdapter {

	private static final String EMAIL_KEY = "email";
	private static final String NAME_KEY = "name";
	private static final String RESPONSE_KEY = "response";
	private static final String KAKAO_ACCOUNT_KEY = "kakao_account";
	@Autowired
	private UserRepository repo;
	@Autowired
	private UserService userService;
	@RequestMapping("/user")
	// Interface OAuth2User :
	//A representation of a user Principal that is registered with an OAuth 2.0 Provider.
	//An OAuth 2.0 user is composed of one or more attributes, for example, first name, middle name,
	// last name, email, phone number, address, etc. Each user attribute has a "name" and "value"
	// and is keyed by the "name" in OAuth2AuthenticatedPrincipal.getAttributes().

	// @AuthenticationPrincipal
	// Annotation that binds a method parameter or method return value to the
	// Authentication.getPrincipal(). This is necessary to signal that the
	// argument should be resolved to the current user rather than a user that might
	// be edited on a form.

	public Map<String, Object> user(@AuthenticationPrincipal OAuth2User principal) {
		String name = "";
		String email = "";
		UserRegistrationDto userRegistrationDto = new UserRegistrationDto(name,email);
		// Case Google login
		email = principal.getAttribute(EMAIL_KEY);
		name = principal.getAttribute(NAME_KEY);
		if (email == null && name == null) {
			// Case Naver login
			if (principal.getAttribute(RESPONSE_KEY) != null) {
				Map<String, Object> response = principal.getAttribute(RESPONSE_KEY);
				// Assign value to email and name only if response is not null
				// It allows us to avoid a nullPointer Exception which would return an error
				if (null != response) {
					email = (String) response.get(EMAIL_KEY);
					name = (String) response.get(NAME_KEY);
				}
			// Case Kakao login
			} else if(principal.getAttribute(KAKAO_ACCOUNT_KEY) != null) {
				Map<String, Object> kakaoResponseEmail = principal.getAttribute(KAKAO_ACCOUNT_KEY);
				Map<String, Object> kakaoResponseName = principal.getAttribute("properties");

				// Assign value to email and name only if response is not null
				// It allows us to avoid a nullPointer Exception which would return an error
				if (null != kakaoResponseName) {
					name = (String) kakaoResponseName.get("nickname");
				}
				if (null != kakaoResponseEmail) {
					email = (String) kakaoResponseEmail.get(EMAIL_KEY);
				}
			} else {
				System.out.println("Unknown method of authentication.");
			}

		}
		// StringUtils.hasLength() is a method checking if a value is empty or null
		if (!StringUtils.hasLength(name) || !StringUtils.hasLength(email)) {
			System.out.println("Unknown method of authentication.");
			return Collections.singletonMap("result", false);
		}
		User user = repo.findByEmail(email);


		if (user == null) {
			userRegistrationDto.setName(name);
			userRegistrationDto.setEmail(email);
			userService.save(userRegistrationDto);
		}

		return Collections.singletonMap(NAME_KEY, name);

	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// @formatter:off
		http
				.authorizeRequests(a -> a
						.antMatchers("/","/register/**","/process_register/**", "/error", "/webjars/**").permitAll()
						.anyRequest().authenticated()
				)
				.exceptionHandling(e -> e
						// HttpStatusEntryPoint : An AuthenticationEntryPoint that sends a generic
						// HttpStatus as a response. Useful for JavaScript clients which cannot use
						// Basic authentication since the browser intercepts the response.
						//HttpStatus.UNAUTHORIZED: The 401 (Unauthorized) status code indicates that
						// the request has not been applied because it lacks valid authentication credentials
						// for the target resource...
						.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
				)
				//Cross-Site Request Forgery (CSRF) is an attack that forces authenticated users to
				//submit a request to a Web application against which they are currently authenticated.
				//CSRF attacks exploit the trust a Web application has in an authenticated user.
				.csrf(c -> c
						// An API to allow changing the method in which the expected CsrfToken is associated to the HttpServletRequest.
						// For example, it may be stored in HttpSession.
						.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
				)
				.logout(l -> l
								.logoutSuccessUrl("/").permitAll()
						//.logoutSuccessUrl("/?logout")
				)
				// HttpSecurity.oauth2Login() provides a number of configuration options for customizing OAuth 2.0 Login. The main configuration options are grouped into their protocol endpoint counterparts.
				.oauth2Login();
		// @formatter:on
	}

	public static void main(String[] args) {
		SpringApplication.run(SocialApplication.class, args);
	}

}
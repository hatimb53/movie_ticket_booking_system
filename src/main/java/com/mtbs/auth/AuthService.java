package com.mtbs.auth;

import com.mtbs.auth.domain.Role;
import com.mtbs.auth.domain.User;
import com.mtbs.auth.dto.AuthResponse;
import com.mtbs.auth.dto.LoginRequest;
import com.mtbs.auth.dto.RegisterRequest;
import com.mtbs.auth.dto.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registration and login. Registration always creates a {@code CUSTOMER}; admin accounts are
 * provisioned via seed data. Login verifies the BCrypt hash and issues a JWT.
 */
@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthService(
      UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  @Transactional
  public UserResponse register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new DuplicateEmailException(request.email());
    }
    User user = new User(
        request.email(), passwordEncoder.encode(request.password()), Role.CUSTOMER);
    User saved = userRepository.save(user);
    return new UserResponse(saved.getId(), saved.getEmail(), saved.getRole().name());
  }

  @Transactional(readOnly = true)
  public AuthResponse login(LoginRequest request) {
    User user = userRepository.findByEmail(request.email())
        .orElseThrow(InvalidCredentialsException::new);
    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw new InvalidCredentialsException();
    }
    String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
    return new AuthResponse(token, user.getRole().name());
  }
}

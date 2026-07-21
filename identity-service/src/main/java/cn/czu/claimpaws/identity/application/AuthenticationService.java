package cn.czu.claimpaws.identity.application;

import cn.czu.claimpaws.identity.domain.User;
import cn.czu.claimpaws.identity.persistence.UserMapper;
import cn.czu.claimpaws.identity.security.JwtTokenService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AuthenticationService {

    private final UserMapper userMapper;
    private final JwtTokenService jwtTokenService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthenticationService(UserMapper userMapper,
                                  JwtTokenService jwtTokenService,
                                  BCryptPasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.jwtTokenService = jwtTokenService;
        this.passwordEncoder = passwordEncoder;
    }

    public String login(String username, String password) {
        // TODO: migrate to BusinessException with stable error codes
        User user = userMapper.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!Boolean.TRUE.equals(user.enabled()) || Boolean.TRUE.equals(user.deleted())) {
            throw new IllegalArgumentException("User account is disabled");
        }

        if (!passwordEncoder.matches(password, user.passwordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        Instant expiresAt = Instant.now().plus(1, ChronoUnit.HOURS);
        return jwtTokenService.createAccessToken(user.id(), user.username(), expiresAt);
    }

    @Transactional
    public User register(RegisterCommand command) {
        if (userMapper.findByUsername(command.username()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        String hashedPassword = passwordEncoder.encode(command.password());
        User user = new User(
                null,
                command.username(),
                hashedPassword,
                command.displayName(),
                command.email(),
                command.phone(),
                command.departmentId(),
                true,
                null,
                null,
                false
        );
        userMapper.insert(user);
        return userMapper.findByUsername(command.username())
                .orElseThrow(() -> new IllegalStateException("User not found after insert"));
    }

    public record RegisterCommand(
            String username,
            String password,
            String displayName,
            String email,
            String phone,
            Long departmentId
    ) {
    }
}

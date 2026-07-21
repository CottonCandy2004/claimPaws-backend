package cn.czu.claimpaws.identity;

import cn.czu.claimpaws.identity.application.AuthenticationService;
import cn.czu.claimpaws.identity.application.AuthenticationService.RegisterCommand;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
class AuthenticationServiceIT {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Autowired
    private AuthenticationService authenticationService;

    @Test
    void registerAndLoginSuccessfully() {
        RegisterCommand command = new RegisterCommand(
                "testuser", "password123", "Test User",
                "test@example.com", "13800138000", null
        );

        var user = authenticationService.register(command);
        assertThat(user.id()).isPositive();
        assertThat(user.username()).isEqualTo("testuser");

        String token = authenticationService.login("testuser", "password123");
        assertThat(token).isNotBlank();

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        var claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        assertThat(claims.getSubject()).isEqualTo(String.valueOf(user.id()));
        assertThat(claims.get("username", String.class)).isEqualTo("testuser");
    }

    @Test
    void registerWithDuplicateUsernameThrowsException() {
        RegisterCommand command = new RegisterCommand(
                "dupeuser", "password123", "Duplicate User",
                "dupe@example.com", "13700137000", null
        );
        authenticationService.register(command);

        assertThatThrownBy(() -> authenticationService.register(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username already exists");
    }

    @Test
    void loginWithInvalidPasswordThrowsException() {
        RegisterCommand command = new RegisterCommand(
                "validuser", "correctpass", "Valid User",
                "valid@example.com", "13900139000", null
        );
        authenticationService.register(command);

        assertThatThrownBy(() -> authenticationService.login("validuser", "wrongpass"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid username or password");
    }
}

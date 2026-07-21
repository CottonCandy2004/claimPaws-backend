package cn.czu.claimpaws.identity.security;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class JwtTokenServiceTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withBean(JwtTokenService.class);

    @Test
    void failsApplicationContextStartupWhenJwtSecretIsMissing() {
        contextRunner.run(context -> assertThat(context).hasFailed());
    }

    @Test
    void rejectsBlankJwtSecretDuringStartup() {
        assertThatThrownBy(() -> new JwtTokenService(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JWT secret");
    }
}

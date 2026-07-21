package cn.czu.claimpaws.identity;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ProductionConfigurationContractTest {

    @Test
    void requiresDatabaseAndJwtSecretsOutsideLocalProfile() throws IOException {
        String configuration = Files.readString(Path.of("src/main/resources/application.yaml"));
        String localConfiguration = Files.readString(Path.of("src/main/resources/application-local.yaml"));

        assertThat(configuration)
                .contains("username: ${MYSQL_USER}")
                .contains("password: ${MYSQL_PASSWORD}")
                .contains("secret: ${JWT_SECRET}")
                .doesNotContain("local-dev-only")
                .doesNotContain("changeit-changeit");
        assertThat(localConfiguration)
                .contains("on-profile: local")
                .contains("password: ${MYSQL_PASSWORD:local-dev-only}")
                .contains("secret: ${JWT_SECRET:local-dev-jwt-secret-change-me-32-bytes}");
    }
}

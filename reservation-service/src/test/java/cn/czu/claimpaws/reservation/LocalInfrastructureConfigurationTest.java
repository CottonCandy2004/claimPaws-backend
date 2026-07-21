package cn.czu.claimpaws.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class LocalInfrastructureConfigurationTest {

    @Test
    void keepsProductionCredentialsRequiredAndLocalDefaultsProfileScoped() throws IOException {
        String configuration = Files.readString(Path.of("src/main/resources/application.yaml"));
        String localConfiguration = Files.readString(Path.of("src/main/resources/application-local.yaml"));

        assertThat(configuration)
                .contains("username: ${MYSQL_USER}")
                .contains("password: ${MYSQL_PASSWORD}")
                .contains("username: ${RABBITMQ_USERNAME}")
                .contains("password: ${RABBITMQ_PASSWORD}")
                .doesNotContain("local-dev-only");
        assertThat(localConfiguration)
                .contains("on-profile: local")
                .contains("username: ${MYSQL_USER:claimpaws}")
                .contains("password: ${MYSQL_PASSWORD:local-dev-only}")
                .contains("username: ${RABBITMQ_USERNAME:claimpaws}")
                .contains("password: ${RABBITMQ_PASSWORD:local-dev-only}");
    }
}

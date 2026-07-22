package cn.czu.claimpaws.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.WebFilter;

@Configuration
public class WebFluxConfig {

    @Bean
    public WebFilter spaFallbackFilter() {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();
            if (!path.startsWith("/api/") && !path.contains(".")) {
                return chain.filter(exchange.mutate()
                        .request(r -> r.path("/index.html").contextPath(""))
                        .build());
            }
            return chain.filter(exchange);
        };
    }
}

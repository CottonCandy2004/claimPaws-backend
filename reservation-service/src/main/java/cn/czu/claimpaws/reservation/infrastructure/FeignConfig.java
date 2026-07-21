package cn.czu.claimpaws.reservation.infrastructure;

import cn.czu.claimpaws.common.api.ApiResponse;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.Decoder;
import org.springframework.context.annotation.Bean;

public class FeignConfig {
    @Bean
    public Decoder feignDecoder(ObjectMapper mapper) {
        return (response, type) -> {
            JavaType wrapperType = mapper.getTypeFactory()
                    .constructParametricType(ApiResponse.class, mapper.constructType(type));
            ApiResponse<?> apiResponse = mapper.readValue(
                    response.body().asInputStream(), wrapperType);
            if (apiResponse.success() && apiResponse.data() != null) {
                return apiResponse.data();
            }
            throw new RuntimeException("Resource service returned error: " + apiResponse.message());
        };
    }
}

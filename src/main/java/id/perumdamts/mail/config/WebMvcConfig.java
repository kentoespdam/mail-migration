package id.perumdamts.mail.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter() {
            @Override
            public boolean canWrite(Class<?> clazz, MediaType mediaType) {
                return false;
            }
        };
        converter.setSupportedMediaTypes(List.of(MediaType.APPLICATION_OCTET_STREAM));
        converters.addFirst(converter);
    }
}

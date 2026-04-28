package id.perumdamts.mail.config;

import id.perumdamts.mail.dto.core.publication.PublicationResponse;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CacheSerializationTest {
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = JsonMapper.builder()
                .addModule(new CacheConfig.PageJacksonModule())
                .build();
    }

    @Test
    void shouldSerializeAndDeserializePublicationResponse() {
        PublicationResponse response = new PublicationResponse(
                "pbl_1", "Title", "Desc", null, "PUBLISHED",
                LocalDateTime.now(), "file.pdf", 100, "User", "Title",
                LocalDateTime.now(), LocalDateTime.now()
        );

        JacksonJsonRedisSerializer<PublicationResponse> serializer =
                new JacksonJsonRedisSerializer<>(mapper, PublicationResponse.class);

        byte[] serialized = serializer.serialize(response);
        assertThat(serialized).isNotEmpty();

        PublicationResponse deserialized = serializer.deserialize(serialized);
        assertThat(deserialized).isNotNull();
        assertThat(deserialized).isEqualTo(response);
    }

    @Test
    void shouldSerializeAndDeserializePage() {
        PublicationResponse p1 = new PublicationResponse(
                "pbl_1", "Title 1", "Desc 1", null, "PUBLISHED",
                LocalDateTime.now(), "file1.pdf", 100, "User", "Title",
                LocalDateTime.now(), LocalDateTime.now()
        );
        Page<PublicationResponse> page = new PageImpl<>(List.of(p1), PageRequest.of(0, 10), 1);

        JavaType pageType = mapper.getTypeFactory().constructParametricType(PageImpl.class, PublicationResponse.class);
        JacksonJsonRedisSerializer<PageImpl<PublicationResponse>> serializer =
                new JacksonJsonRedisSerializer<>(mapper, pageType);

        byte[] serialized = serializer.serialize((PageImpl<PublicationResponse>) page);
        assertThat(serialized).isNotEmpty();

        PageImpl<PublicationResponse> result = serializer.deserialize(serialized);
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst()).isInstanceOf(PublicationResponse.class);
        assertThat(result.getContent().getFirst()).isEqualTo(p1);
    }
}

package id.perumdamts.mail.infrastructure.sqids;

import id.perumdamts.mail.config.SqidsProperties;
import org.sqids.Sqids;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class SqidsHelper {

    private final Map<SqidPrefix, Sqids> instances = new EnumMap<>(SqidPrefix.class);

    public SqidsHelper(SqidsProperties props) {
        for (SqidPrefix prefix : SqidPrefix.values()) {
            String shuffled = shuffleAlphabet(props.alphabet(), prefix.name() + props.shuffleKey());
            instances.put(prefix, Sqids.builder()
                    .alphabet(shuffled)
                    .minLength(props.minLength())
                    .build());
        }
    }

    public String encode(SqidPrefix prefix, Number id) {
        return prefix.getPrefix() + "_" + instances.get(prefix).encode(List.of(id.longValue()));
    }

    public int decode(String sqid) {
        int underscoreIdx = sqid.indexOf('_');
        if (underscoreIdx < 0) {
            throw new IllegalArgumentException("Invalid sqid format: " + sqid);
        }
        String prefixStr = sqid.substring(0, underscoreIdx);
        String encoded = sqid.substring(underscoreIdx + 1);

        for (SqidPrefix prefix : SqidPrefix.values()) {
            if (prefix.getPrefix().equals(prefixStr)) {
                List<Long> decoded = instances.get(prefix).decode(encoded);
                if (decoded.isEmpty()) {
                    throw new IllegalArgumentException("Cannot decode sqid: " + sqid);
                }
                return decoded.getFirst().intValue();
            }
        }
        throw new IllegalArgumentException("Unknown sqid prefix: " + prefixStr);
    }

    /** Fisher-Yates shuffle — same as laravel-sqids alphabetForModel */
    private String shuffleAlphabet(String alphabet, String key) {
        char[] chars = alphabet.toCharArray();
        char[] keyChars = key.toCharArray();
        int keyLen = keyChars.length;
        for (int i = chars.length - 1, v = 0, p = 0; i > 0; i--, v++) {
            v %= keyLen;
            p += keyChars[v];
            int j = (keyChars[v] + v + p) % i;
            char tmp = chars[j];
            chars[j] = chars[i];
            chars[i] = tmp;
        }
        return new String(chars);
    }
}

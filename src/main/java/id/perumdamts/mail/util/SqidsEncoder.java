package id.perumdamts.mail.util;

import id.perumdamts.mail.config.SqidsProperties;
import org.springframework.stereotype.Component;
import org.sqids.Sqids;

import java.util.List;

@Component
public class SqidsEncoder {

    private final SqidsProperties props;

    public SqidsEncoder(SqidsProperties props) {
        this.props = props;
    }

    public String encode(Class<?> modelClass, long id) {
        String prefix = consonantPrefix(modelClass);
        String encoded = encodeId(modelClass, id);
        return prefix + props.separator() + encoded;
    }

    public long decode(Class<?> modelClass, String sqid) {
        String expectedPrefix = consonantPrefix(modelClass) + props.separator();

        if (!sqid.startsWith(expectedPrefix)) {
            throw new IllegalArgumentException(
                    "Invalid sqid prefix for %s: %s".formatted(modelClass.getSimpleName(), sqid));
        }

        String encoded = sqid.substring(expectedPrefix.length());

        if (encoded.length() < props.minLength()) {
            throw new IllegalArgumentException("Sqid too short: " + sqid);
        }

        List<Long> numbers = sqidsFor(modelClass).decode(encoded);
        if (numbers.isEmpty()) {
            throw new IllegalArgumentException("Cannot decode sqid: " + sqid);
        }

        long id = numbers.getFirst();

        // Canonical check
        String reEncoded = encodeId(modelClass, id);
        if (!encoded.equals(reEncoded)) {
            throw new IllegalArgumentException("Non-canonical sqid: " + sqid);
        }

        return id;
    }

    public static String consonantPrefix(Class<?> clazz) {
        String name = clazz.getSimpleName();
        String consonants = name.replaceAll("(?i)[aeiou]", "");
        return consonants.substring(0, Math.min(3, consonants.length())).toLowerCase();
    }

    private Sqids sqidsFor(Class<?> modelClass) {
        String shuffledAlphabet = alphabetForModel(
                modelClass.getSimpleName().toLowerCase()
        );
        return Sqids.builder()
                .alphabet(shuffledAlphabet)
                .minLength(props.minLength())
                .build();
    }

    private String encodeId(Class<?> modelClass, long id) {
        return sqidsFor(modelClass).encode(List.of(id));
    }

    String alphabetForModel(String modelName) {
        String alphabet = props.alphabet();
        String shuffleSeed = modelName + (props.shuffleKey() != null ? props.shuffleKey() : "");

        if (shuffleSeed.isEmpty()) return alphabet;

        char[] arr = alphabet.toCharArray();
        char[] seed = shuffleSeed.toCharArray();
        int seedLen = seed.length;
        int alphabetLen = arr.length;

        for (int i = alphabetLen - 1, v = 0, p = 0; i > 0; i--, v++) {
            v %= seedLen;
            p += seed[v];
            int j = (seed[v] + v + p) % i;
            char tmp = arr[j];
            arr[j] = arr[i];
            arr[i] = tmp;
        }

        return new String(arr);
    }
}

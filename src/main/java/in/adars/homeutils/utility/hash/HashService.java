package in.adars.homeutils.utility.hash;

import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class HashService {

    private static final List<String> ALGORITHMS = List.of("MD5", "SHA-1", "SHA-256", "SHA-512");

    public Map<String, String> hashAll(byte[] data) {
        Map<String, String> out = new LinkedHashMap<>();
        for (String algo : ALGORITHMS) {
            try {
                MessageDigest md = MessageDigest.getInstance(algo);
                out.put(algo, toHex(md.digest(data)));
            } catch (NoSuchAlgorithmException e) {
                out.put(algo, "unavailable");
            }
        }
        return out;
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}

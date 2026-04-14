package in.adars.homeutils.utility.keygen;

import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class KeyGenService {

    private final SecureRandom random = new SecureRandom();

    public String generateRandomHex(int bytes) {
        byte[] buf = new byte[bytes];
        random.nextBytes(buf);
        return HexFormat.of().formatHex(buf);
    }

    public String generateRandomBase64(int bytes) {
        byte[] buf = new byte[bytes];
        random.nextBytes(buf);
        return Base64.getEncoder().encodeToString(buf);
    }

    public String generateRandomBase64Url(int bytes) {
        byte[] buf = new byte[bytes];
        random.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    public String generateHmacKey(String algorithm) throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(algorithm);
        byte[] key = keyGen.generateKey().getEncoded();
        return Base64.getEncoder().encodeToString(key);
    }

    public String generateUuid() {
        return UUID.randomUUID().toString();
    }

    public String generateUuidNoDashes() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

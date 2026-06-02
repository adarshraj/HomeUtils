package in.adars.homeutils.utility.keygen;

import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
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

    public Map<String, String> generateSshKeyPair(String algorithm) throws Exception {
        return switch (algorithm) {
            case "ed25519" -> generateEd25519Pair();
            case "rsa2048" -> generateRsaPair(2048);
            case "rsa4096" -> generateRsaPair(4096);
            default -> throw new IllegalArgumentException("Unknown SSH algorithm: " + algorithm);
        };
    }

    private Map<String, String> generateEd25519Pair() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519");
        KeyPair kp = kpg.generateKeyPair();

        // Extract raw 32-byte public key and 32-byte private seed
        byte[] pubEncoded = kp.getPublic().getEncoded(); // X.509 SubjectPublicKeyInfo
        byte[] privEncoded = kp.getPrivate().getEncoded(); // PKCS#8

        // Raw pub key is last 32 bytes of X.509 encoding
        byte[] pubRaw = new byte[32];
        System.arraycopy(pubEncoded, pubEncoded.length - 32, pubRaw, 0, 32);

        // Raw seed is last 32 bytes of PKCS#8 encoding (after the OCTET STRING header)
        byte[] seedRaw = new byte[32];
        System.arraycopy(privEncoded, privEncoded.length - 32, seedRaw, 0, 32);

        // OpenSSH private key (64-byte = seed || pubkey)
        byte[] privRaw = new byte[64];
        System.arraycopy(seedRaw, 0, privRaw, 0, 32);
        System.arraycopy(pubRaw, 0, privRaw, 32, 32);

        byte[] pubWire = buildEd25519PublicKeyWire(pubRaw);
        String publicKey = "ssh-ed25519 " + Base64.getEncoder().encodeToString(pubWire);
        String privateKey = buildOpenSshPrivateKey("ssh-ed25519", pubWire, baos -> {
            writeString(baos, "ssh-ed25519");
            writeBytes(baos, pubRaw);
            writeBytes(baos, privRaw);
        });

        return Map.of("publicKey", publicKey, "privateKey", privateKey, "algorithm", "Ed25519");
    }

    private Map<String, String> generateRsaPair(int bits) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(bits, random);
        KeyPair kp = kpg.generateKeyPair();

        RSAPublicKey pub = (RSAPublicKey) kp.getPublic();
        RSAPrivateCrtKey priv = (RSAPrivateCrtKey) kp.getPrivate();

        byte[] pubWire = buildRsaPublicKeyWire(pub.getPublicExponent(), pub.getModulus());
        String publicKey = "ssh-rsa " + Base64.getEncoder().encodeToString(pubWire);
        String privateKey = buildOpenSshPrivateKey("ssh-rsa", pubWire, baos -> {
            writeString(baos, "ssh-rsa");
            writeMpInt(baos, pub.getModulus());
            writeMpInt(baos, pub.getPublicExponent());
            writeMpInt(baos, priv.getPrivateExponent());
            writeMpInt(baos, priv.getCrtCoefficient());
            writeMpInt(baos, priv.getPrimeP());
            writeMpInt(baos, priv.getPrimeQ());
        });

        return Map.of("publicKey", publicKey, "privateKey", privateKey, "algorithm", "RSA-" + bits);
    }

    @FunctionalInterface
    private interface PrivKeyWriter {
        void write(ByteArrayOutputStream baos) throws Exception;
    }

    private String buildOpenSshPrivateKey(String algName, byte[] pubWire, PrivKeyWriter privWriter) throws Exception {
        ByteArrayOutputStream privSection = new ByteArrayOutputStream();
        int checkInt = random.nextInt();
        writeUint32(privSection, checkInt);
        writeUint32(privSection, checkInt);
        privWriter.write(privSection);
        writeString(privSection, ""); // comment
        // Padding: 1, 2, 3, ... until length is multiple of 8
        int pad = 1;
        while (privSection.size() % 8 != 0) privSection.write(pad++);

        ByteArrayOutputStream body = new ByteArrayOutputStream();
        body.write("openssh-key-v1\0".getBytes());
        writeString(body, "none");   // cipher
        writeString(body, "none");   // kdf
        writeString(body, "");       // kdf options (empty)
        writeUint32(body, 1);        // num keys
        writeBytes(body, pubWire);   // public key
        writeBytes(body, privSection.toByteArray()); // private section

        String b64 = Base64.getMimeEncoder(70, new byte[]{'\n'}).encodeToString(body.toByteArray());
        return "-----BEGIN OPENSSH PRIVATE KEY-----\n" + b64 + "\n-----END OPENSSH PRIVATE KEY-----\n";
    }

    private byte[] buildEd25519PublicKeyWire(byte[] pubRaw) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeString(baos, "ssh-ed25519");
        writeBytes(baos, pubRaw);
        return baos.toByteArray();
    }

    private byte[] buildRsaPublicKeyWire(BigInteger e, BigInteger n) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeString(baos, "ssh-rsa");
        writeMpInt(baos, e);
        writeMpInt(baos, n);
        return baos.toByteArray();
    }

    private void writeUint32(ByteArrayOutputStream baos, int value) {
        baos.write((value >> 24) & 0xff);
        baos.write((value >> 16) & 0xff);
        baos.write((value >> 8) & 0xff);
        baos.write(value & 0xff);
    }

    private void writeBytes(ByteArrayOutputStream baos, byte[] data) {
        writeUint32(baos, data.length);
        baos.writeBytes(data);
    }

    private void writeString(ByteArrayOutputStream baos, String s) {
        writeBytes(baos, s.getBytes());
    }

    private void writeMpInt(ByteArrayOutputStream baos, BigInteger n) {
        byte[] raw = n.toByteArray(); // already in two's complement, big-endian
        writeBytes(baos, raw);
    }
}

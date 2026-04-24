package in.adars.homeutils.utility.hash;

import in.adars.homeutils.utility.Utility;
import org.springframework.stereotype.Component;

@Component
public class HashUtility implements Utility {
    public String getId() { return "hash"; }
    public String getName() { return "Hash Generator"; }
    public String getDescription() { return "Generate MD5, SHA-1, SHA-256, and SHA-512 hashes from text or files."; }
    public String getRoute() { return "/utils/hash"; }
    public String getIconClass() { return "bi-fingerprint"; }
}

package in.adars.homeutils.utility.keygen;

import in.adars.homeutils.utility.Utility;
import org.springframework.stereotype.Component;

@Component
public class KeyGenUtility implements Utility {
    public String getId() { return "keygen"; }
    public String getName() { return "Key Generator"; }
    public String getDescription() { return "Generate HMAC secrets, JWT signing keys, session keys, app keys, and UUIDs."; }
    public String getRoute() { return "/utils/keygen"; }
    public String getIconClass() { return "bi-key-fill"; }
}

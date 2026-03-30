package in.adars.homeutils.utility.jwt;

import in.adars.homeutils.utility.Utility;
import org.springframework.stereotype.Component;

@Component
public class JwtUtility implements Utility {
    public String getId() { return "jwt"; }
    public String getName() { return "JWT Decoder"; }
    public String getDescription() { return "Decode and inspect JWT tokens — header, payload, expiry."; }
    public String getRoute() { return "/utils/jwt"; }
    public String getIconClass() { return "bi-key"; }
}

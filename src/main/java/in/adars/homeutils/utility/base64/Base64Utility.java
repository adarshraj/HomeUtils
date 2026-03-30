package in.adars.homeutils.utility.base64;

import in.adars.homeutils.utility.Utility;
import org.springframework.stereotype.Component;

@Component
public class Base64Utility implements Utility {
    public String getId() { return "base64"; }
    public String getName() { return "Base64"; }
    public String getDescription() { return "Encode or decode text and files using Base64."; }
    public String getRoute() { return "/utils/base64"; }
    public String getIconClass() { return "bi-braces"; }
}

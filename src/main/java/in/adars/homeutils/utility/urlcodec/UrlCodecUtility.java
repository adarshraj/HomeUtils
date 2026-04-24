package in.adars.homeutils.utility.urlcodec;

import in.adars.homeutils.utility.Utility;
import org.springframework.stereotype.Component;

@Component
public class UrlCodecUtility implements Utility {
    public String getId() { return "urlcodec"; }
    public String getName() { return "URL Encode / Decode"; }
    public String getDescription() { return "Percent-encode or decode text for safe use in URLs."; }
    public String getRoute() { return "/utils/urlcodec"; }
    public String getIconClass() { return "bi-link-45deg"; }
}

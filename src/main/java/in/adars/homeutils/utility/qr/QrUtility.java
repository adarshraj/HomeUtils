package in.adars.homeutils.utility.qr;

import in.adars.homeutils.utility.Utility;
import org.springframework.stereotype.Component;

@Component
public class QrUtility implements Utility {
    public String getId() { return "qr"; }
    public String getName() { return "QR Code Generator"; }
    public String getDescription() { return "Generate QR codes from any text or URL."; }
    public String getRoute() { return "/utils/qr"; }
    public String getIconClass() { return "bi-qr-code"; }
}

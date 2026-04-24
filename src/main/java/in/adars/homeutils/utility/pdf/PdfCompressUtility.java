package in.adars.homeutils.utility.pdf;

import in.adars.homeutils.utility.Utility;
import org.springframework.stereotype.Component;

@Component
public class PdfCompressUtility implements Utility {
    public String getId() { return "pdfcompress"; }
    public String getName() { return "PDF Compress"; }
    public String getDescription() { return "Shrink a PDF by rasterizing pages at lower DPI and JPEG quality."; }
    public String getRoute() { return "/utils/pdfcompress"; }
    public String getIconClass() { return "bi-file-earmark-zip"; }
}

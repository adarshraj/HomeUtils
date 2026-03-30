package in.adars.homeutils.utility.pdf;

import in.adars.homeutils.utility.Utility;
import org.springframework.stereotype.Component;

@Component
public class PdfToImagesUtility implements Utility {
    public String getId() { return "pdf2img"; }
    public String getName() { return "PDF to Images"; }
    public String getDescription() { return "Extract each page of a PDF as a PNG image."; }
    public String getRoute() { return "/utils/pdf2img"; }
    public String getIconClass() { return "bi-file-earmark-image"; }
}

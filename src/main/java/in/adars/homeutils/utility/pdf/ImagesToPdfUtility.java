package in.adars.homeutils.utility.pdf;

import in.adars.homeutils.utility.Utility;
import org.springframework.stereotype.Component;

@Component
public class ImagesToPdfUtility implements Utility {
    public String getId() { return "img2pdf"; }
    public String getName() { return "Images to PDF"; }
    public String getDescription() { return "Combine multiple images (JPEG, PNG, WebP, HEIC…) into a single PDF."; }
    public String getRoute() { return "/utils/img2pdf"; }
    public String getIconClass() { return "bi-images"; }
}

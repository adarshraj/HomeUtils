package in.adars.homeutils.utility.image;

import in.adars.homeutils.utility.Utility;
import org.springframework.stereotype.Component;

@Component
public class ImageUtility implements Utility {
    public String getId() { return "image"; }
    public String getName() { return "Image Converter"; }
    public String getDescription() { return "Convert and compress images between JPG, PNG, and WebP with optional resizing."; }
    public String getRoute() { return "/utils/image"; }
    public String getIconClass() { return "bi-image"; }
}

package in.adars.homeutils.utility.heic;

import in.adars.homeutils.utility.Utility;
import org.springframework.stereotype.Component;

@Component
public class HeicUtility implements Utility {

    @Override
    public String getId() {
        return "heic";
    }

    @Override
    public String getName() {
        return "HEIC Converter";
    }

    @Override
    public String getDescription() {
        return "Convert HEIC/HEIF images (iPhone photos) to JPEG, PNG, PDF, WebP and more.";
    }

    @Override
    public String getRoute() {
        return "/utils/heic";
    }

    @Override
    public String getIconClass() {
        return "bi-image";
    }
}

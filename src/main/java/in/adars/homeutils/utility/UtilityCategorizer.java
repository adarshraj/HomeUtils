package in.adars.homeutils.utility;

import java.util.Map;

public final class UtilityCategorizer {
    private static final Map<String, String> ID_TO_CATEGORY = Map.ofEntries(
            Map.entry("base64", "Text & Code"),
            Map.entry("urlcodec", "Text & Code"),
            Map.entry("jsonformat", "Text & Code"),
            Map.entry("diff", "Text & Code"),
            Map.entry("regex", "Text & Code"),
            Map.entry("jwt", "Text & Code"),
            Map.entry("docconvert", "Text & Code"),

            Map.entry("hash", "Crypto & Keys"),
            Map.entry("keygen", "Crypto & Keys"),

            Map.entry("image", "Images"),
            Map.entry("heic", "Images"),
            Map.entry("qr", "Images"),
            Map.entry("color", "Images"),

            Map.entry("img2pdf", "PDF"),
            Map.entry("pdfmerge", "PDF"),
            Map.entry("pdfsplit", "PDF"),
            Map.entry("pdf2img", "PDF"),
            Map.entry("pdfcompress", "PDF"),

            Map.entry("timestamp", "Time & Schedule"),
            Map.entry("worldclock", "Time & Schedule"),
            Map.entry("timecalc", "Time & Schedule"),
            Map.entry("cron", "Time & Schedule")
    );

    private static final Map<String, Integer> CATEGORY_ORDER = Map.of(
            "Text & Code", 1,
            "Crypto & Keys", 2,
            "Images", 3,
            "PDF", 4,
            "Time & Schedule", 5,
            "Other", 99
    );

    private UtilityCategorizer() {}

    public static String categoryOf(Utility u) {
        return ID_TO_CATEGORY.getOrDefault(u.getId(), "Other");
    }

    public static int orderOf(String category) {
        return CATEGORY_ORDER.getOrDefault(category, 50);
    }
}

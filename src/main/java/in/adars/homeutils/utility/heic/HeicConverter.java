package in.adars.homeutils.utility.heic;

import openize.heic.decoder.HeicImage;
import openize.heic.decoder.PixelFormat;
import openize.io.IOFileStream;
import openize.io.IOMode;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Converts HEIC files to other image formats.
 * Uses Openize.HEIC for decoding and ImageIO (JDK built-in) for encoding.
 * PDF uses PDFBox; SVG embeds base64-encoded PNG (JDK Base64).
 */
@Component
public class HeicConverter {

    private static final java.util.Set<String> HEIC_EXTENSIONS =
            java.util.Set.of("heic", "heif", "HEIC", "HEIF");

    public boolean isHeicFile(File file) {
        String name = file.getName();
        String ext = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : "";
        return HEIC_EXTENSIONS.contains(ext);
    }

    private BufferedImage toRgb(BufferedImage source) {
        BufferedImage rgb = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        try {
            g.drawImage(source, 0, 0, null);
        } finally {
            g.dispose();
        }
        return rgb;
    }

    private BufferedImage prepareForFormat(BufferedImage source, String formatName) {
        String upper = formatName.toUpperCase();
        switch (upper) {
            case "WBMP" -> {
                BufferedImage bin = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
                BufferedImage rgb = toRgb(source);
                for (int y = 0; y < source.getHeight(); y++) {
                    for (int x = 0; x < source.getWidth(); x++) {
                        int p = rgb.getRGB(x, y);
                        int r = (p >> 16) & 0xFF;
                        int g = (p >> 8) & 0xFF;
                        int b = p & 0xFF;
                        int lum = (r * 299 + g * 587 + b * 114) / 1000;
                        bin.getRaster().setSample(x, y, 0, lum >= 128 ? 1 : 0);
                    }
                }
                return bin;
            }
            case "JPEG", "JPG", "BMP", "WEBP" -> {
                return toRgb(source);
            }
            default -> {
                return source;
            }
        }
    }

    private byte[] writeSvgWithEmbeddedImage(BufferedImage image) throws IOException {
        ByteArrayOutputStream pngBaos = new ByteArrayOutputStream();
        ImageIO.write(toRgb(image), "PNG", pngBaos);
        String base64 = Base64.getEncoder().encodeToString(pngBaos.toByteArray());
        int w = image.getWidth();
        int h = image.getHeight();
        String svg = """
                <?xml version="1.0" encoding="UTF-8"?>
                <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="%d" height="%d" viewBox="0 0 %d %d">
                  <image width="%d" height="%d" xlink:href="data:image/png;base64,%s"/>
                </svg>""".formatted(w, h, w, h, w, h, base64);
        return svg.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * Result of converting a single HEIC file: a list of named output blobs.
     */
    public record ConversionResult(String fileName, byte[] data) {}

    /**
     * Convert a HEIC file to the target format, returning in-memory results.
     * Multi-frame HEIC produces one result per frame (except PDF: single file).
     */
    public List<ConversionResult> convert(File heicFile, String formatName, String baseName, String suffix) throws Exception {
        String upper = formatName.toUpperCase();
        String ext = ConversionService.FORMAT_TO_EXTENSION.getOrDefault(upper, formatName.toLowerCase());

        List<ConversionResult> results = new ArrayList<>();

        try (IOFileStream fs = new IOFileStream(heicFile.getAbsolutePath(), IOMode.READ)) {
            HeicImage heicImage = HeicImage.load(fs);
            Map<?, ?> frames = heicImage.getFrames();
            if (frames.isEmpty()) return results;

            List<?> frameValues = new ArrayList<>(frames.values());

            if ("PDF".equals(upper)) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try (PDDocument doc = new PDDocument()) {
                    for (Object frameObj : frameValues) {
                        var frame = (openize.heic.decoder.HeicImageFrame) frameObj;
                        int w = (int) frame.getWidth();
                        int h = (int) frame.getHeight();
                        int[] pixels = frame.getInt32Array(PixelFormat.Argb32);
                        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                        img.setRGB(0, 0, w, h, pixels, 0, w);
                        BufferedImage rgb = toRgb(img);
                        PDPage page = new PDPage(new PDRectangle(w, h));
                        doc.addPage(page);
                        try (PDPageContentStream content = new PDPageContentStream(doc, page)) {
                            var pdImg = LosslessFactory.createFromImage(doc, rgb);
                            content.drawImage(pdImg, 0, 0, w, h);
                        }
                    }
                    doc.save(baos);
                }
                results.add(new ConversionResult(baseName + suffix + ".pdf", baos.toByteArray()));

            } else if ("SVG".equals(upper)) {
                for (int i = 0; i < frameValues.size(); i++) {
                    var frame = (openize.heic.decoder.HeicImageFrame) frameValues.get(i);
                    int w = (int) frame.getWidth();
                    int h = (int) frame.getHeight();
                    int[] pixels = frame.getInt32Array(PixelFormat.Argb32);
                    BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                    img.setRGB(0, 0, w, h, pixels, 0, w);
                    String name = frameValues.size() > 1
                            ? baseName + suffix + "_" + i + ".svg"
                            : baseName + suffix + ".svg";
                    results.add(new ConversionResult(name, writeSvgWithEmbeddedImage(img)));
                }

            } else {
                String ioFormat = "WEBP".equals(upper) ? "webp" : formatName;
                for (int i = 0; i < frameValues.size(); i++) {
                    var frame = (openize.heic.decoder.HeicImageFrame) frameValues.get(i);
                    int w = (int) frame.getWidth();
                    int h = (int) frame.getHeight();
                    int[] pixels = frame.getInt32Array(PixelFormat.Argb32);
                    BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                    img.setRGB(0, 0, w, h, pixels, 0, w);
                    BufferedImage toWrite = prepareForFormat(img, formatName);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(toWrite, ioFormat, baos);
                    String name = frameValues.size() > 1
                            ? baseName + suffix + "_" + i + "." + ext
                            : baseName + suffix + "." + ext;
                    results.add(new ConversionResult(name, baos.toByteArray()));
                }
            }
        }
        return results;
    }
}

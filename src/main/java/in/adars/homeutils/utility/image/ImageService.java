package in.adars.homeutils.utility.image;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;

@Service
public class ImageService {

    private static final Set<String> LOSSY = Set.of("jpg", "jpeg", "webp");

    public record ConversionResult(String fileName, byte[] data, String mimeType) {}

    public ConversionResult convert(MultipartFile file, String targetFormat, float quality, Integer maxWidth) throws IOException {
        String fmt = targetFormat.toLowerCase();
        BufferedImage source;
        try (InputStream in = file.getInputStream()) {
            source = ImageIO.read(in);
        }
        if (source == null) {
            throw new IOException("Unsupported or unreadable image file.");
        }

        BufferedImage prepared = maxWidth != null && maxWidth > 0 && source.getWidth() > maxWidth
                ? resize(source, maxWidth)
                : source;

        if ("jpg".equals(fmt) || "jpeg".equals(fmt) || "webp".equals(fmt)) {
            prepared = toRgb(prepared);
        }

        byte[] bytes = LOSSY.contains(fmt)
                ? writeWithQuality(prepared, fmt, quality)
                : writeSimple(prepared, fmt);

        String outName = stripExtension(file.getOriginalFilename()) + "." + ("jpeg".equals(fmt) ? "jpg" : fmt);
        return new ConversionResult(outName, bytes, mimeFor(fmt));
    }

    private BufferedImage resize(BufferedImage src, int targetWidth) {
        int targetHeight = Math.round(src.getHeight() * (targetWidth / (float) src.getWidth()));
        BufferedImage out = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(src, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return out;
    }

    private BufferedImage toRgb(BufferedImage src) {
        BufferedImage rgb = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return rgb;
    }

    private byte[] writeSimple(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (!ImageIO.write(image, format, baos)) {
            throw new IOException("No writer available for format: " + format);
        }
        return baos.toByteArray();
    }

    private byte[] writeWithQuality(BufferedImage image, String format, float quality) throws IOException {
        String writerFormat = "jpg".equals(format) ? "jpeg" : format;
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(writerFormat);
        if (!writers.hasNext()) throw new IOException("No writer for format: " + format);
        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            if (param.getCompressionTypes() != null && param.getCompressionTypes().length > 0) {
                param.setCompressionType(param.getCompressionTypes()[0]);
            }
            param.setCompressionQuality(Math.min(1.0f, Math.max(0.1f, quality)));
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (MemoryCacheImageOutputStream ios = new MemoryCacheImageOutputStream(baos)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
        return baos.toByteArray();
    }

    private String mimeFor(String fmt) {
        return switch (fmt) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            case "bmp" -> "image/bmp";
            case "gif" -> "image/gif";
            default -> "application/octet-stream";
        };
    }

    private String stripExtension(String name) {
        if (name == null) return "image";
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }
}

package in.adars.homeutils.utility.heic;

import net.lingala.zip4j.ZipFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ConversionService {

    public static final Map<String, String> FORMAT_TO_EXTENSION = Map.of(
            "JPEG", "jpg",
            "PNG", "png",
            "BMP", "bmp",
            "GIF", "gif",
            "WBMP", "wbmp",
            "TIFF", "tiff",
            "PDF", "pdf",
            "SVG", "svg",
            "WEBP", "webp"
    );

    public static final List<String> SUPPORTED_FORMATS = List.of(
            "JPEG", "PNG", "BMP", "GIF", "WBMP", "TIFF", "PDF", "SVG", "WEBP"
    );

    private final HeicConverter heicConverter;

    public ConversionService(HeicConverter heicConverter) {
        this.heicConverter = heicConverter;
    }

    /**
     * Accept uploaded files (HEIC files or ZIPs), convert all HEIC images to the target format,
     * and return the results. Single output → returned as-is; multiple outputs → zipped.
     */
    public ConversionResponse convert(List<MultipartFile> uploads, String formatName, String suffix, String zipPassword) throws Exception {
        String effectiveSuffix = (suffix == null || suffix.isBlank()) ? "_conv" : suffix;
        Path tempDir = Files.createTempDirectory("homeutils-heic-");
        try {
            List<File> heicFiles = resolveHeicFiles(uploads, tempDir, zipPassword);
            List<HeicConverter.ConversionResult> allResults = convertAll(heicFiles, formatName, effectiveSuffix);
            if (allResults.isEmpty()) throw new IllegalStateException("No HEIC files found in upload.");
            if (allResults.size() == 1) {
                HeicConverter.ConversionResult r = allResults.get(0);
                return new ConversionResponse(r.fileName(), r.data(), false);
            }
            return new ConversionResponse("converted_images.zip", packageAsZip(allResults), true);
        } finally {
            deleteRecursive(tempDir.toFile());
        }
    }

    public record ConversionResponse(String fileName, byte[] data, boolean isZip) {}

    private List<File> resolveHeicFiles(List<MultipartFile> uploads, Path tempDir, String zipPassword) throws IOException {
        List<File> heicFiles = new ArrayList<>();
        for (MultipartFile upload : uploads) {
            String originalName = upload.getOriginalFilename() != null ? upload.getOriginalFilename() : "upload";
            String lower = originalName.toLowerCase();

            if (lower.endsWith(".zip")) {
                heicFiles.addAll(extractHeicFromZip(upload, tempDir, zipPassword));
            } else if (heicConverter.isHeicFile(new File(originalName))) {
                File dest = tempDir.resolve(sanitize(originalName)).toFile();
                upload.transferTo(dest);
                heicFiles.add(dest);
            }
        }
        return heicFiles;
    }

    private List<File> extractHeicFromZip(MultipartFile upload, Path tempDir, String zipPassword) throws IOException {
        List<File> heicFiles = new ArrayList<>();
        boolean hasPassword = zipPassword != null && !zipPassword.isBlank();

        File zipTemp = tempDir.resolve("upload.zip").toFile();
        upload.transferTo(zipTemp);
        Path extractDir = tempDir.resolve("extracted");
        Files.createDirectories(extractDir);

        try (ZipFile zf = hasPassword
                ? new ZipFile(zipTemp, zipPassword.toCharArray())
                : new ZipFile(zipTemp)) {
            zf.extractAll(extractDir.toString());
        } catch (Exception e) {
            String msg = hasPassword ? "Failed to extract ZIP (wrong password?): " : "Failed to extract ZIP: ";
            throw new IOException(msg + e.getMessage(), e);
        }
        collectHeicFromDir(extractDir.toFile(), heicFiles);
        return heicFiles;
    }

    private void collectHeicFromDir(File dir, List<File> result) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                collectHeicFromDir(f, result);
            } else if (heicConverter.isHeicFile(f)) {
                result.add(f);
            }
        }
    }

    private List<HeicConverter.ConversionResult> convertAll(List<File> heicFiles, String formatName, String suffix) throws Exception {
        if (heicFiles.isEmpty()) return Collections.emptyList();

        if (heicFiles.size() == 1) {
            File f = heicFiles.get(0);
            String baseName = stripExtension(f.getName());
            return heicConverter.convert(f, formatName, baseName, suffix);
        }

        int poolSize = Math.min(heicFiles.size(), Math.max(1, Runtime.getRuntime().availableProcessors()));
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        List<HeicConverter.ConversionResult> allResults = Collections.synchronizedList(new ArrayList<>());
        try {
            List<Callable<Void>> tasks = heicFiles.stream().map(f -> (Callable<Void>) () -> {
                String baseName = stripExtension(f.getName());
                List<HeicConverter.ConversionResult> results = heicConverter.convert(f, formatName, baseName, suffix);
                allResults.addAll(results);
                return null;
            }).toList();
            executor.invokeAll(tasks).forEach(future -> {
                try { future.get(); } catch (Exception ignored) {}
            });
        } finally {
            executor.shutdown();
        }
        return allResults;
    }

    private byte[] packageAsZip(List<HeicConverter.ConversionResult> results) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (HeicConverter.ConversionResult r : results) {
                zos.putNextEntry(new ZipEntry(r.fileName()));
                zos.write(r.data());
                zos.closeEntry();
            }
        }
        return baos.toByteArray();
    }

    private String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9._\\-]", "_");
    }

    private String stripExtension(String name) {
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    private void deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) deleteRecursive(child);
            }
        }
        file.delete();
    }
}

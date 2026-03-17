package com.kelab.cloud.cloudinary.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Sube una imagen a Cloudinary.
     *
     * @param file   archivo recibido del frontend
     * @param folder carpeta en Cloudinary (ej: "products", "stores", "users")
     * @return URL segura de la imagen subida
     */
    @SuppressWarnings("unchecked")
    public String uploadImage(MultipartFile file, String folder) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new RuntimeException("La imagen no puede superar 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Solo se permiten archivos de imagen");
        }

        try {
            // DESPUÉS — parámetros planos, sin transformation anidado
            Map<String, Object> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "kelab/" + folder,
                            "resource_type", "image",
                            "allowed_formats", new String[] { "jpg", "jpeg", "png", "webp" },
                            "quality", "auto",
                            "fetch_format", "auto"));

            String url = (String) result.get("secure_url");
            log.info("Imagen subida a Cloudinary: {}", url);
            return url;

        } catch (IOException e) {
            log.error("Error subiendo imagen a Cloudinary: {}", e.getMessage());
            throw new RuntimeException("No se pudo subir la imagen. Intenta de nuevo.");
        }
    }

    /**
     * Elimina una imagen de Cloudinary por su public_id.
     * El public_id se extrae de la URL: todo después de /upload/ sin extensión.
     * Ej: https://res.cloudinary.com/demo/image/upload/kelab/products/abc123.jpg
     * → public_id = "kelab/products/abc123"
     *
     * @param imageUrl URL completa de Cloudinary
     */
    public void deleteImage(String imageUrl) {

        if (imageUrl == null || imageUrl.isBlank())
            return;

        try {
            String publicId = extractPublicId(imageUrl);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Imagen eliminada de Cloudinary: {}", publicId);
        } catch (IOException e) {
            log.warn("No se pudo eliminar imagen de Cloudinary: {}", e.getMessage());
            // No lanzamos excepción — si falla el delete en CDN, no bloqueamos la lógica
        }
    }

    // ── PRIVATE ──────────────────────────────────────────────

    private String extractPublicId(String imageUrl) {
        // Ejemplo URL:
        // https://res.cloudinary.com/mi-cloud/image/upload/v1234567890/kelab/products/abc123.jpg
        // Queremos: kelab/products/abc123
        try {
            String marker = "/upload/";
            int idx = imageUrl.indexOf(marker);
            if (idx == -1)
                throw new IllegalArgumentException("URL no es de Cloudinary");

            String path = imageUrl.substring(idx + marker.length());

            // Quitar versión si existe (v1234567890/)
            if (path.startsWith("v") && path.contains("/")) {
                path = path.substring(path.indexOf("/") + 1);
            }

            // Quitar extensión
            int dotIdx = path.lastIndexOf(".");
            if (dotIdx != -1) {
                path = path.substring(0, dotIdx);
            }

            return path;
        } catch (Exception e) {
            throw new IllegalArgumentException("No se pudo extraer el public_id de: " + imageUrl);
        }
    }
}

package cm.kfokam.stock.storage;

import cm.kfokam.stock.exception.FileStorageException;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
class MinioFileStorageServiceImpl implements FileStorageService {

    private static final int PRESIGNED_URL_EXPIRY_HOURS = 2;
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    // Le SDK MinIO declare ~9 exceptions verifiees (IOException, NoSuchAlgorithmException,
    // ErrorResponseException, ...) sur chacun de ses appels : on les capture toutes ici pour
    // les re-empaqueter dans une seule FileStorageException exploitable par les appelants.
    @PostConstruct
    void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            throw new FileStorageException("Impossible d'initialiser le bucket MinIO '%s'".formatted(bucketName), e);
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        String objectName = buildObjectName(folder, file.getOriginalFilename());
        String contentType = file.getContentType() != null ? file.getContentType() : DEFAULT_CONTENT_TYPE;
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(contentType)
                    .build());
            return objectName;
        } catch (Exception e) {
            throw new FileStorageException("Échec de l'upload du fichier '%s'".formatted(file.getOriginalFilename()), e);
        }
    }

    @Override
    public byte[] downloadFile(String fileName) {
        try (InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(fileName)
                .build())) {
            return inputStream.readAllBytes();
        } catch (Exception e) {
            throw new FileStorageException("Échec du téléchargement du fichier '%s'".formatted(fileName), e);
        }
    }

    @Override
    public String getPresignedUrl(String fileName) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(fileName)
                    .expiry(PRESIGNED_URL_EXPIRY_HOURS, TimeUnit.HOURS)
                    .build());
        } catch (Exception e) {
            throw new FileStorageException("Échec de la génération de l'URL pour '%s'".formatted(fileName), e);
        }
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build());
        } catch (Exception e) {
            throw new FileStorageException("Échec de la suppression du fichier '%s'".formatted(fileName), e);
        }
    }

    private String buildObjectName(String folder, String originalFilename) {
        String extension = "";
        if (originalFilename != null) {
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex >= 0) {
                extension = originalFilename.substring(dotIndex);
            }
        }
        String uniqueName = UUID.randomUUID() + extension;
        return (folder == null || folder.isBlank()) ? uniqueName : folder + "/" + uniqueName;
    }
}

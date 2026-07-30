package cm.kfokam.stock.storage;

import cm.kfokam.stock.exception.FileStorageException;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import okhttp3.Headers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MinioFileStorageServiceImplTest {

    private static final String BUCKET = "gestion-stock-bucket";

    @Mock
    private MinioClient minioClient;

    private MinioFileStorageServiceImpl fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new MinioFileStorageServiceImpl(minioClient);
        ReflectionTestUtils.setField(fileStorageService, "bucketName", BUCKET);
    }

    @Test
    void uploadFile_shouldReturnGeneratedObjectName_withFolderPrefixAndExtension() throws Exception {
        MultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", "content".getBytes());

        String objectName = fileStorageService.uploadFile(file, "clients");

        assertThat(objectName).startsWith("clients/").endsWith(".png");
        verify(minioClient).putObject(any());
    }

    @Test
    void uploadFile_shouldReturnGeneratedObjectName_withoutFolder_whenFolderBlank() {
        MultipartFile file = new MockMultipartFile("file", "logo.jpg", "image/jpeg", "content".getBytes());

        String objectName = fileStorageService.uploadFile(file, " ");

        assertThat(objectName).doesNotContain("/").endsWith(".jpg");
    }

    @Test
    void uploadFile_shouldThrowFileStorageException_whenMinioFails() throws Exception {
        MultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", "content".getBytes());
        doThrow(new IOException("MinIO indisponible")).when(minioClient).putObject(any());

        assertThatThrownBy(() -> fileStorageService.uploadFile(file, "clients"))
                .isInstanceOf(FileStorageException.class)
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void downloadFile_shouldReturnBytes_whenObjectExists() throws Exception {
        byte[] content = "hello".getBytes();
        when(minioClient.getObject(any())).thenReturn(newGetObjectResponse(content));

        byte[] result = fileStorageService.downloadFile("clients/some-uuid.png");

        assertThat(result).isEqualTo(content);
    }

    @Test
    void downloadFile_shouldThrowFileStorageException_whenObjectMissing() throws Exception {
        when(minioClient.getObject(any())).thenThrow(new IOException("Objet introuvable"));

        assertThatThrownBy(() -> fileStorageService.downloadFile("missing.png"))
                .isInstanceOf(FileStorageException.class)
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void deleteFile_shouldCallRemoveObject() throws Exception {
        fileStorageService.deleteFile("clients/some-uuid.png");

        verify(minioClient).removeObject(any());
    }

    @Test
    void deleteFile_shouldThrowFileStorageException_whenMinioFails() throws Exception {
        doThrow(new IOException("MinIO indisponible")).when(minioClient).removeObject(any());

        assertThatThrownBy(() -> fileStorageService.deleteFile("clients/some-uuid.png"))
                .isInstanceOf(FileStorageException.class)
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void getPresignedUrl_shouldReturnUrl() throws Exception {
        when(minioClient.getPresignedObjectUrl(any()))
                .thenReturn("https://minio.local/clients/some-uuid.png?X-Amz-Expires=7200");

        String url = fileStorageService.getPresignedUrl("clients/some-uuid.png");

        assertThat(url).contains("clients/some-uuid.png");
    }

    @Test
    void getPresignedUrl_shouldThrowFileStorageException_whenMinioFails() throws Exception {
        when(minioClient.getPresignedObjectUrl(any())).thenThrow(new IOException("MinIO indisponible"));

        assertThatThrownBy(() -> fileStorageService.getPresignedUrl("clients/some-uuid.png"))
                .isInstanceOf(FileStorageException.class)
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void ensureBucketExists_shouldCreateBucket_whenAbsent() throws Exception {
        when(minioClient.bucketExists(any())).thenReturn(false);

        fileStorageService.ensureBucketExists();

        verify(minioClient).makeBucket(any());
    }

    @Test
    void ensureBucketExists_shouldNotCreateBucket_whenAlreadyPresent() throws Exception {
        when(minioClient.bucketExists(any())).thenReturn(true);

        fileStorageService.ensureBucketExists();

        verify(minioClient, never()).makeBucket(any());
    }

    @Test
    void ensureBucketExists_shouldThrowFileStorageException_whenMinioFails() throws Exception {
        when(minioClient.bucketExists(any())).thenThrow(new IOException("MinIO indisponible"));

        assertThatThrownBy(() -> fileStorageService.ensureBucketExists())
                .isInstanceOf(FileStorageException.class)
                .hasCauseInstanceOf(IOException.class);
    }

    private GetObjectResponse newGetObjectResponse(byte[] content) {
        return new GetObjectResponse(new Headers.Builder().build(), BUCKET, null, "object",
                new ByteArrayInputStream(content));
    }
}

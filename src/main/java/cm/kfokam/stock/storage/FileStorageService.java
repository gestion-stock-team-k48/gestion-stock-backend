package cm.kfokam.stock.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String uploadFile(MultipartFile file, String folder);

    byte[] downloadFile(String fileName);

    String getPresignedUrl(String fileName);

    void deleteFile(String fileName);
}

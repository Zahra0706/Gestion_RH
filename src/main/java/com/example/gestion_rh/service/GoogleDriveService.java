package com.example.gestion_rh.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleDriveService {

    private static final String APPLICATION_NAME = "GestionRH";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);
    private static final String CREDENTIALS_FILE_PATH = "credentials.json";

    public String uploadFile(String fileName, byte[] content, String mimeType, String folderId) throws Exception {
        // 1. Charger les credentials
        GoogleCredentials credentials = GoogleCredentials.fromStream(
                new ClassPathResource(CREDENTIALS_FILE_PATH).getInputStream())
                .createScoped(SCOPES);

        // 2. Créer le service Drive
        Drive service = new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();

        // 3. Metadata avec folder parent
        File fileMetadata = new File();
        fileMetadata.setName(fileName);
        if (folderId != null && !folderId.isEmpty()) {
            fileMetadata.setParents(Collections.singletonList("18LfXpZlFVg_ZMsYAxDXn0py-vaThyzW7"));
        }

        // 4. Upload via Fichier Temporaire (Requis par FileContent si on veut éviter
        // InputStreamContent ou pour suivre la demande)
        java.io.File tempFile = java.io.File.createTempFile("upload_", ".tmp");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(content);
        }

        FileContent mediaContent = new FileContent(mimeType, tempFile);

        File file = service.files().create(fileMetadata, mediaContent)
                .setFields("id, webViewLink, webContentLink")
                .execute();

        // 5. Permissions
        Permission permission = new Permission()
                .setType("anyone")
                .setRole("reader");
        service.permissions().create(file.getId(), permission).execute();

        // Cleanup
        tempFile.delete();

        return file.getWebViewLink();
    }
}

package ru.wallentos.carworker.configuration;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleConfiguration {
    GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
            .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));
    HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(
            credentials);

    // Create the sheets API client
    @Bean
    public Sheets sheets() {
        return new Sheets.Builder(new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                requestInitializer)
                .setApplicationName("Sheets samples")
                .build();
    }

    public GoogleConfiguration() throws IOException {
    }
}

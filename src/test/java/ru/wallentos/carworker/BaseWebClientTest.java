package ru.wallentos.carworker;

import java.io.IOException;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("unit")
@SpringBootTest
public abstract class BaseWebClientTest {
    protected static MockWebServer server;
    protected static final int PORT = 33333;

    @BeforeAll
    public static void beforeAll() throws IOException {
        server = new MockWebServer();
        server.start(PORT);
    }

    @AfterAll
    public static void afterAll() throws IOException {
        server.shutdown();
    }
}

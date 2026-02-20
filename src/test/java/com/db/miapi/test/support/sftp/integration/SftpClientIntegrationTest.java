package com.db.miapi.test.support.sftp.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.db.miapi.test.support.sftp.EmbeddedSftpServer;
import com.db.miapi.test.support.sftp.SftpClientWrapper;
import com.db.miapi.test.support.sftp.util.SftpReadyAwaiter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SftpClientIntegrationTest {

    @Test
    void testUploadAndDownload() throws Exception {
        EmbeddedSftpServer server = EmbeddedSftpServer.create()
                .port(0)
                .rootDirectory("target/test-data")
                .passwordAuthenticator((u, p, s) -> true)
                .enableSftp()
                .inMemoryHostKeys(Map.of("RSA", 2048));

        server.start();
        int port = server.getPort();

        SftpReadyAwaiter.await("localhost", port, "user", "pass", 5000);

        Path localFile = Files.createTempFile("test", ".txt");
        Files.writeString(localFile, "Hello SFTP");

        try (SftpClientWrapper client = new SftpClientWrapper("localhost", port, "user", "pass")) {
            String remotePath = client.put(localFile, "remote.txt");
            assertThat(remotePath).isEqualTo("remote.txt");

            Path downloaded = Files.createTempFile("download", ".txt");
            client.get("remote.txt", downloaded);
            assertThat(Files.readString(downloaded)).isEqualTo("Hello SFTP");
        }

        server.stop();
    }
}

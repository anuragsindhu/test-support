package com.db.miapi.test.support.sftp.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.db.miapi.test.support.sftp.EmbeddedSftpServer;
import com.db.miapi.test.support.sftp.SftpClientWrapper;
import com.db.miapi.test.support.sftp.util.SftpReadyAwaiter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.apache.sshd.sftp.client.SftpClient.DirEntry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SftpClientIntegrationTest {

    private EmbeddedSftpServer server;
    private int port;

    @BeforeEach
    void setUp() throws Exception {
        server = EmbeddedSftpServer.create()
                .port(0)
                .rootDirectory("target/test-data")
                .passwordAuthenticator((u, p, s) -> true)
                .enableSftp()
                .inMemoryHostKeys(Map.of("RSA", 2048));
        server.start();
        port = server.getPort();
        SftpReadyAwaiter.await("localhost", port, "user", "pass", 5000);
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void putStoresFileOnServer() throws Exception {
        Path localFile = createTempFileWithContent("put", "Hello SFTP");

        try (SftpClientWrapper client = new SftpClientWrapper("localhost", port, "user", "pass")) {
            String remotePath = client.put(localFile, "put.txt");
            assertThat(remotePath).isEqualTo("put.txt");

            List<DirEntry> entries = client.ls(".");
            assertThat(entries.stream().map(DirEntry::getFilename)).contains("put.txt");
        }
    }

    @Test
    void getRetrievesFileFromServer() throws Exception {
        Path localFile = createTempFileWithContent("get", "Download me");

        try (SftpClientWrapper client = new SftpClientWrapper("localhost", port, "user", "pass")) {
            client.put(localFile, "get.txt");

            Path downloaded = Files.createTempFile("download", ".txt");
            client.get("get.txt", downloaded);

            assertThat(Files.readString(downloaded)).isEqualTo("Download me");
        }
    }

    @Test
    void lsListsDirectoryContents() throws Exception {
        Path localFile = createTempFileWithContent("ls", "List me");

        try (SftpClientWrapper client = new SftpClientWrapper("localhost", port, "user", "pass")) {
            client.put(localFile, "ls.txt");

            List<DirEntry> entries = client.ls(".");
            assertThat(entries.stream().map(DirEntry::getFilename)).contains("ls.txt");
        }
    }

    @Test
    void rmDeletesFileFromServer() throws Exception {
        Path localFile = createTempFileWithContent("rm", "Remove me");

        try (SftpClientWrapper client = new SftpClientWrapper("localhost", port, "user", "pass")) {
            client.put(localFile, "rm.txt");

            boolean removed = client.rm("rm.txt");
            assertThat(removed).isTrue();

            List<DirEntry> entries = client.ls(".");
            assertThat(entries.stream().map(DirEntry::getFilename)).doesNotContain("rm.txt");
        }
    }

    private Path createTempFileWithContent(String prefix, String content) throws Exception {
        Path file = Files.createTempFile(prefix, ".txt");
        Files.writeString(file, content);
        return file;
    }
}

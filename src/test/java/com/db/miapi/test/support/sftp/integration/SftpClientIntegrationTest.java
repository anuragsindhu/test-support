package com.db.miapi.test.support.sftp.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.db.miapi.test.support.sftp.EmbeddedSftpServer;
import com.db.miapi.test.support.sftp.SftpClientWrapper;
import com.db.miapi.test.support.sftp.util.SftpReadyAwaiter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.apache.sshd.sftp.client.SftpClient.Attributes;
import org.apache.sshd.sftp.client.SftpClient.DirEntry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SftpClientWrapperIntegrationTest {

    private EmbeddedSftpServer server;
    private int port;

    @BeforeEach
    void setUp() throws Exception {
        server = EmbeddedSftpServer.create()
                .port(0)
                .rootDirectory("target/test-data-" + System.nanoTime())
                .passwordAuthenticator((u, p, s) -> true)
                .enableSftp()
                .inMemoryHostKeys(Map.of("RSA", 2048));
        server.start();
        port = server.getPort();
        SftpReadyAwaiter.await("localhost", port, "user", "pass", 5000);
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void putStoresFile() throws Exception {
        Path localFile = createTempFileWithContent("put", "Hello SFTP");
        try (SftpClientWrapper client = new SftpClientWrapper("localhost", port, "user", "pass")) {
            String remotePath = client.put(localFile, "put.txt");
            assertThat(remotePath).isEqualTo("put.txt");
            assertThat(client.ls(".").stream().map(DirEntry::getFilename)).contains("put.txt");
        }
    }

    @Test
    void getRetrievesFile() throws Exception {
        Path localFile = createTempFileWithContent("get", "Download me");
        try (SftpClientWrapper client = new SftpClientWrapper("localhost", port, "user", "pass")) {
            client.put(localFile, "get.txt");
            Path downloaded = Files.createTempFile("download", ".txt");
            client.get("get.txt", downloaded);
            assertThat(Files.readString(downloaded)).isEqualTo("Download me");
        }
    }

    @Test
    void lsListsContents() throws Exception {
        Path localFile = createTempFileWithContent("ls", "List me");
        try (SftpClientWrapper client = new SftpClientWrapper("localhost", port, "user", "pass")) {
            client.put(localFile, "ls.txt");
            List<DirEntry> entries = client.ls(".");
            assertThat(entries.stream().map(DirEntry::getFilename)).contains("ls.txt");
        }
    }

    @Test
    void rmDeletesFile() throws Exception {
        Path localFile = createTempFileWithContent("rm", "Remove me");
        try (SftpClientWrapper client = new SftpClientWrapper("localhost", port, "user", "pass")) {
            client.put(localFile, "rm.txt");
            assertThat(client.rm("rm.txt")).isTrue();
            assertThat(client.ls(".").stream().map(DirEntry::getFilename)).doesNotContain("rm.txt");
        }
    }

    @Test
    void mkdirAndRmdirWork() throws Exception {
        try (SftpClientWrapper client = new SftpClientWrapper("localhost", port, "user", "pass")) {
            client.mkdir("newdir");
            assertThat(client.ls(".").stream().map(DirEntry::getFilename)).contains("newdir");
            assertThat(client.rmdir("newdir")).isTrue();
            assertThat(client.ls(".").stream().map(DirEntry::getFilename)).doesNotContain("newdir");
        }
    }

    @Test
    void statReturnsAttributes() throws Exception {
        Path localFile = createTempFileWithContent("stat", "Attributes test");
        try (SftpClientWrapper client = new SftpClientWrapper("localhost", port, "user", "pass")) {
            client.put(localFile, "stat.txt");
            Attributes attrs = client.stat("stat.txt");
            assertThat(attrs).isNotNull();
            assertThat(attrs.getSize()).isEqualTo(Files.size(localFile));
        }
    }

    @Test
    void isConnectedReportsStatus() throws Exception {
        try (SftpClientWrapper client = new SftpClientWrapper("localhost", port, "user", "pass")) {
            assertThat(client.isConnected()).isTrue();
        }
    }

    private Path createTempFileWithContent(String prefix, String content) throws Exception {
        Path file = Files.createTempFile(prefix, ".txt");
        Files.writeString(file, content);
        return file;
    }
}

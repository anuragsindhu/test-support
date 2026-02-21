package com.db.miapi.test.support.sftp.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.db.miapi.test.support.sftp.EmbeddedSftpServer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SftpServerIntegrationTest {

    @Test
    void serverLifecycleTransitionsCleanly() throws Exception {
        Path rootDir = Path.of("target/test-data-" + System.nanoTime());

        EmbeddedSftpServer server = EmbeddedSftpServer.create()
                .port(0) // dynamic port
                .rootDirectory(rootDir.toString())
                .passwordAuthenticator((u, p, s) -> true)
                .enableSftp()
                .inMemoryHostKeys(Map.of("RSA", 2048, "ED25519", 256));

        // Start should succeed
        assertThatCode(server::start).doesNotThrowAnyException();

        // Verify port assigned
        int port = server.getPort();
        assertThat(port).isGreaterThan(0);

        // Verify root directory exists
        assertThat(Files.exists(rootDir)).isTrue();

        // Stop should succeed
        assertThatCode(server::stop).doesNotThrowAnyException();

        // After stop, getPort should throw
        assertThatCode(server::getPort).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doubleStartThrowsIllegalStateException() throws Exception {
        EmbeddedSftpServer server = EmbeddedSftpServer.create()
                .port(0)
                .rootDirectory("target/test-data-" + System.nanoTime())
                .passwordAuthenticator((u, p, s) -> true)
                .enableSftp()
                .inMemoryHostKeys(Map.of("RSA", 2048));

        server.start();
        assertThatCode(server::start).isInstanceOf(IllegalStateException.class);
        server.stop();
    }

    @Test
    void stopWithoutStartDoesNothing() {
        EmbeddedSftpServer server = EmbeddedSftpServer.create()
                .port(0)
                .rootDirectory("target/test-data-" + System.nanoTime())
                .passwordAuthenticator((u, p, s) -> true)
                .enableSftp();

        // Should not throw even if never started
        assertThatCode(server::stop).doesNotThrowAnyException();
    }
}

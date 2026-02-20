package com.db.miapi.test.support.sftp.integration;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.db.miapi.test.support.sftp.EmbeddedSftpServer;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SftpServerIntegrationTest {

    @Test
    void testServerLifecycle() throws Exception {
        EmbeddedSftpServer server = EmbeddedSftpServer.create()
                .port(2222)
                .rootDirectory("target/test-data")
                .passwordAuthenticator((u, p, s) -> true)
                .enableSftp()
                .inMemoryHostKeys(Map.of("RSA", 2048, "ED25519", 256));

        assertThatCode(server::start).doesNotThrowAnyException();
        assertThatCode(server::stop).doesNotThrowAnyException();
    }
}

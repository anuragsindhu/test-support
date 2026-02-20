package com.db.miapi.test.support.sftp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class SftpClientWrapperTest {

    @Test
    void testClientWrapperConstruction() throws Exception {
        EmbeddedSftpServer server = EmbeddedSftpServer.create()
                .port(0)
                .rootDirectory("target/test-data")
                .passwordAuthenticator((u, p, s) -> true)
                .enableSftp()
                .inMemoryHostKeys(Map.of("RSA", 2048));

        server.start();
        int port = server.getPort();

        try (SftpClientWrapper client = new SftpClientWrapper("localhost", port, "user", "pass")) {
            assertThat(client).isNotNull();
        }

        server.stop();
    }
}

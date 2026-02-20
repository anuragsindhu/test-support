package com.db.miapi.test.support.sftp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class EmbeddedSftpServerTest {

    @Test
    void testFluentApiConfiguration() throws Exception {
        EmbeddedSftpServer server = EmbeddedSftpServer.create()
                .port(2222)
                .rootDirectory("target/test-data")
                .passwordAuthenticator((u, p, s) -> true)
                .enableSftp()
                .inMemoryHostKeys(Map.of("RSA", 2048, "ED25519", 256));

        assertThat(server).isNotNull();
    }
}

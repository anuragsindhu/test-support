package com.db.miapi.test.support.sftp;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.KeyPair;
import java.util.Map;
import org.apache.sshd.common.session.SessionContext;
import org.junit.jupiter.api.Test;

class InMemoryMultiKeyPairProviderTest {

    @Test
    void testGeneratesMultipleAlgorithms() throws Exception {
        InMemoryMultiKeyPairProvider provider = new InMemoryMultiKeyPairProvider(Map.of("RSA", 2048, "ED25519", 256));

        Iterable<KeyPair> keys = provider.loadKeys((SessionContext) null);

        assertThat(keys).isNotEmpty().allSatisfy(kp -> assertThat(kp.getPrivate())
                .isNotNull());
    }
}

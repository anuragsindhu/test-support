package com.db.miapi.test.support.sftp;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.*;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.session.SessionContext;

public class InMemoryMultiKeyPairProvider implements KeyPairProvider {
    private final List<KeyPair> keyPairs = new ArrayList<>();

    public InMemoryMultiKeyPairProvider(Map<String, Integer> algorithmKeySizes) throws Exception {
        for (Map.Entry<String, Integer> entry : algorithmKeySizes.entrySet()) {
            String algo = entry.getKey();
            Integer size = entry.getValue();

            KeyPairGenerator generator = KeyPairGenerator.getInstance(algo);

            // ED25519 and similar algorithms ignore key size
            if ("Ed25519".equalsIgnoreCase(algo) || "Ed448".equalsIgnoreCase(algo)) {
                keyPairs.add(generator.generateKeyPair());
            } else {
                generator.initialize(size);
                keyPairs.add(generator.generateKeyPair());
            }
        }
    }

    @Override
    public Iterable<KeyPair> loadKeys(SessionContext session) {
        return Collections.unmodifiableList(keyPairs);
    }
}

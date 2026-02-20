package com.db.miapi.test.support.sftp.util;

import java.util.concurrent.TimeUnit;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.sftp.client.SftpClientFactory;

public final class SftpReadyAwaiter {

    private SftpReadyAwaiter() {}

    public static void await(String host, int port, String user, String password, long timeoutMillis) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutMillis;
        Exception lastException = null;

        while (System.currentTimeMillis() < deadline) {
            try (SshClient client = SshClient.setUpDefaultClient()) {
                client.setServerKeyVerifier((s, r, k) -> true);
                client.start();

                try (ClientSession session = client.connect(user, host, port)
                        .verify(5, TimeUnit.SECONDS)
                        .getSession()) {
                    session.addPasswordIdentity(password);
                    session.auth().verify(5, TimeUnit.SECONDS);

                    // Try to open SFTP subsystem
                    SftpClientFactory.instance().createSftpClient(session).close();
                    return; // success
                } finally {
                    client.stop();
                }
            } catch (Exception e) {
                lastException = e;
                Thread.sleep(200);
            }
        }
        throw new IllegalStateException("SFTP subsystem not ready after " + timeoutMillis + "ms", lastException);
    }
}

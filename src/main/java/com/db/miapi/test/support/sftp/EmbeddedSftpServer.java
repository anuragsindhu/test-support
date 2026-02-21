package com.db.miapi.test.support.sftp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;

@Slf4j
public class EmbeddedSftpServer {
    private final SshServer sshd;

    private EmbeddedSftpServer() {
        this.sshd = SshServer.setUpDefaultServer();
    }

    public static EmbeddedSftpServer create() {
        return new EmbeddedSftpServer();
    }

    /**
     * Set the port. Use 0 for dynamic assignment (OS picks free port).
     */
    public EmbeddedSftpServer port(int port) {
        sshd.setPort(port);
        return this;
    }

    /**
     * Root directory is auto-created if missing.
     */
    public EmbeddedSftpServer rootDirectory(String rootDir) {
        Path rootPath = Paths.get(rootDir).toAbsolutePath();
        try {
            Files.createDirectories(rootPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create root directory: " + rootDir, e);
        }
        sshd.setFileSystemFactory(new VirtualFileSystemFactory(rootPath));
        return this;
    }

    public EmbeddedSftpServer passwordAuthenticator(PasswordAuthenticator authenticator) {
        sshd.setPasswordAuthenticator(authenticator);
        return this;
    }

    public EmbeddedSftpServer enableSftp() {
        sshd.setSubsystemFactories(Collections.singletonList(new SftpSubsystemFactory()));
        return this;
    }

    public EmbeddedSftpServer inMemoryHostKeys(Map<String, Integer> algorithmKeySizes) throws Exception {
        sshd.setKeyPairProvider(new InMemoryMultiKeyPairProvider(algorithmKeySizes));
        return this;
    }

    public void start() throws Exception {
        try {
            sshd.start();
            log.info("SFTP Server started on port {}", sshd.getPort());
        } catch (Exception e) {
            log.error("Failed to start SFTP Server", e);
            throw e;
        }
    }

    public void stop() {
        try {
            sshd.stop();
            log.info("SFTP Server stopped");
        } catch (Exception e) {
            log.error("Failed to stop SFTP Server", e);
        }
    }

    public int getPort() {
        return sshd.getPort();
    }
}

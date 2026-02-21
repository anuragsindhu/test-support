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

    public EmbeddedSftpServer port(int port) {
        if (port < 0) {
            throw new IllegalArgumentException("Port must be non-negative");
        }
        sshd.setPort(port);
        return this;
    }

    public EmbeddedSftpServer rootDirectory(String rootDir) {
        Path rootPath = Paths.get(rootDir).toAbsolutePath();
        try {
            Files.createDirectories(rootPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create root directory: " + rootPath, e);
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
        if (algorithmKeySizes == null || algorithmKeySizes.isEmpty()) {
            algorithmKeySizes = Map.of("RSA", 2048);
        }
        sshd.setKeyPairProvider(new InMemoryMultiKeyPairProvider(algorithmKeySizes));
        return this;
    }

    public void start() throws Exception {
        if (sshd.isStarted()) {
            throw new IllegalStateException("SFTP Server already started");
        }
        sshd.start();
        log.info(
                "SFTP Server started on port {} with root {}",
                sshd.getPort(),
                ((VirtualFileSystemFactory) sshd.getFileSystemFactory()).getDefaultHomeDir());
    }

    public void stop() {
        try {
            if (sshd.isOpen()) {
                sshd.close(true);
                log.info("SFTP Server stopped");
            }
        } catch (Exception e) {
            log.error("Failed to stop SFTP Server", e);
        }
    }

    public int getPort() {
        if (!sshd.isOpen()) {
            throw new IllegalStateException("SFTP Server not started yet");
        }
        return sshd.getPort();
    }
}

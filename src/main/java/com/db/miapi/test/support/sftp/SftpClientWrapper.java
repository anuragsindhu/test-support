package com.db.miapi.test.support.sftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.client.SftpClientFactory;

@Slf4j
public class SftpClientWrapper implements AutoCloseable {
    private final SshClient client;
    private final ClientSession session;

    @Getter
    private final SftpClient sftp;

    public SftpClientWrapper(String host, int port, String user, String password) throws Exception {
        client = SshClient.setUpDefaultClient();
        client.setServerKeyVerifier((s, r, k) -> true);
        client.start();

        session = client.connect(user, host, port).verify(10, TimeUnit.SECONDS).getSession();
        session.addPasswordIdentity(password);
        session.auth().verify(10, TimeUnit.SECONDS);

        sftp = SftpClientFactory.instance().createSftpClient(session);
    }

    public Path get(String remote, Path local) throws IOException {
        try (InputStream in = sftp.read(remote)) {
            Files.copy(in, local, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("Failed to download file: " + remote, e);
        }
        return local;
    }

    public List<SftpClient.DirEntry> ls(String remoteDir) throws IOException {
        List<SftpClient.DirEntry> files = new ArrayList<>();
        try {
            for (SftpClient.DirEntry entry : sftp.readDir(remoteDir)) {
                files.add(entry);
            }
        } catch (IOException e) {
            throw new IOException("Failed to list directory: " + remoteDir, e);
        }
        return files;
    }

    public String put(Path local, String remote) throws IOException {
        try (OutputStream out = sftp.write(remote)) {
            Files.copy(local, out);
        } catch (IOException e) {
            throw new IOException("Failed to upload file: " + local, e);
        }
        return remote;
    }

    public boolean rm(String remoteFile) {
        try {
            sftp.remove(remoteFile);
            return true;
        } catch (IOException e) {
            log.warn("Failed to remove file: {}", remoteFile, e);
            return false;
        }
    }

    @Override
    public void close() throws Exception {
        try {
            sftp.close();
        } finally {
            session.close(false);
            client.stop();
        }
    }
}

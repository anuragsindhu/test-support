# Embedded SFTP Test Library

## Usage

### Server Setup

#### Fixed port 

```java
EmbeddedSftpServer server = EmbeddedSftpServer.create()
    .port(2222)
    .rootDirectory("target/test-data")
    .passwordAuthenticator((u, p, s) -> true)
    .enableSftp()
    .inMemoryHostKeys(Map.of("RSA", 2048, "ED25519", 0));

server.start();
```

#### Dynamic port

```java
EmbeddedSftpServer server = EmbeddedSftpServer.create()
    .port(0)
    .rootDirectory("target/test-data")
    .passwordAuthenticator((u, p, s) -> true)
    .enableSftp()
    .inMemoryHostKeys(Map.of("RSA", 2048, "ED25519", 0));

server.start();
```

### Client Usage
```java
try (SftpClientWrapper client = new SftpClientWrapper("localhost", 2222, "user", "pass")) {
    client.put(Path.of("local.txt"), "remote.txt");
    client.get("remote.txt", Path.of("downloaded.txt"));
    client.ls(".");
}
```

## Notes

- Host keys are generated in memory and discarded after shutdown.
- For test purposes, the client accepts all host keys (setServerKeyVerifier((s,r,k) -> true)).
- Use port(0) for dynamic port assignment in automated environments.
- Not intended for production use.
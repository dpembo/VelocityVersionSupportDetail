# VelocityVersionSupportDetail

Velocity plugin that tells an incompatible Minecraft Java client which versions this proxy accepts, and refuses the connection.

Compatible clients still see the player count and ping bars on the multiplayer screen. A client outside the configured range sees `MIN - MAX Supported` in red in that spot. The ping bars stay. If they try to join, the proxy disconnects them with the same text.

`MIN` and `MAX` come from `plugins/velocityversionsupport/config.yml`. Leave `maximum-version` out, or set it to `""`, and only the minimum is enforced. Clients older than that see `MIN+ Supported` instead.

## Build

Requires Java 25 and Maven.

```bash
mvn package
```

The jar is `target/velocityversionsupport-1.0.0.jar`. Copy it into the proxy `plugins` folder and restart Velocity.

## Configuration

```yaml
minimum-version: "1.21.4"
maximum-version: "26.3"
```

Both values are inclusive release names that this copy of Velocity understands. `maximum-version` is optional. Versions that share a protocol cannot be separated: `1.21.7` and `1.21.8` are both protocol 772, so a minimum of `1.21.8` still accepts `1.21.7`.

Velocity itself drops clients it cannot speak to, before this plugin runs. The configured range has to sit inside the versions that proxy build supports.

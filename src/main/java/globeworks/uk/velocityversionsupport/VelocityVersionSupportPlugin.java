/*
 * Copyright (C) 2026 GlobeWorks
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package globeworks.uk.velocityversionsupport;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import java.io.IOException;
import java.nio.file.Path;
import org.slf4j.Logger;

@Plugin(
        id = "velocityversionsupport",
        name = "VelocityVersionSupportDetail",
        version = "1.0.0",
        description = "Shows the supported Minecraft version range to incompatible clients and refuses their connection.",
        authors = {"GlobeWorks"})
public final class VelocityVersionSupportPlugin {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    @Inject
    public VelocityVersionSupportPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        VersionRange range;
        try {
            range = ConfigFile.load(dataDirectory);
        } catch (IOException | RuntimeException e) {
            logger.error("Could not load config.yml: {}", e.getMessage());
            throw new IllegalStateException("Could not load config.yml", e);
        }
        server.getEventManager().register(this, new VersionGateListener(range));
        if (range.maximum() == null) {
            logger.info("Accepting Minecraft {} and newer.", range.minimumVersion());
        } else {
            logger.info("Accepting Minecraft {} through {}.", range.minimumVersion(), range.maximumVersion());
        }
    }
}

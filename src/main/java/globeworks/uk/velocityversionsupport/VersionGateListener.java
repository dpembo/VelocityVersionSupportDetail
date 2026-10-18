/*
 * Copyright (C) 2026 GlobeWorks
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package globeworks.uk.velocityversionsupport;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Compatible clients keep the normal player count and ping bars. Everyone else sees the configured
 * range in red on the server list, and the proxy closes the connection if they try to join.
 */
public final class VersionGateListener {

    private final VersionRange range;

    public VersionGateListener(VersionRange range) {
        this.range = range;
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPing(ProxyPingEvent event) {
        int protocol = event.getConnection().getProtocolVersion().getProtocol();
        if (range.allows(protocol)) {
            return;
        }
        ServerPing ping = event.getPing();
        event.setPing(ping.asBuilder()
                .version(new ServerPing.Version(range.mismatchedProtocol(protocol), range.label()))
                .build());
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPreLogin(PreLoginEvent event) {
        int protocol = event.getConnection().getProtocolVersion().getProtocol();
        if (range.allows(protocol)) {
            return;
        }
        event.setResult(PreLoginEvent.PreLoginComponentResult.denied(
                Component.text(range.label(), NamedTextColor.RED)));
    }
}

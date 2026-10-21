/*
 * Copyright (C) 2026 GlobeWorks
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package globeworks.uk.velocityversionsupport;

import com.velocitypowered.api.network.ProtocolVersion;

/**
 * Inclusive version window. A null maximum leaves the upper end open.
 *
 * <p>Comparison uses Velocity's version order, not the raw protocol integer. {@code getProtocol()}
 * can return a snapshot id that does not sort with release protocols, which made an open upper
 * end treat older clients as new enough.
 */
public record VersionRange(
        String minimumVersion,
        String maximumVersion,
        ProtocolVersion minimum,
        ProtocolVersion maximum) {

    public VersionRange {
        if (minimumVersion == null || minimumVersion.isBlank()) {
            throw new IllegalArgumentException("minimum-version is blank");
        }
        if (minimum == null || !minimum.isSupported()) {
            throw new IllegalArgumentException("minimum-version is not a supported Minecraft version");
        }
        if (maximumVersion != null && maximumVersion.isBlank()) {
            maximumVersion = null;
        }
        if (maximumVersion == null) {
            maximum = null;
        } else if (maximum == null || !maximum.isSupported()) {
            throw new IllegalArgumentException("maximum-version " + maximumVersion + " has no protocol");
        } else if (maximum.lessThan(minimum)) {
            throw new IllegalArgumentException("minimum-version " + minimumVersion
                    + " is newer than maximum-version " + maximumVersion);
        }
    }

    public boolean allows(ProtocolVersion client) {
        if (client == null || !client.isSupported()) {
            return false;
        }
        if (client.lessThan(minimum)) {
            return false;
        }
        return maximum == null || client.noGreaterThan(maximum);
    }

    /**
     * Text the multiplayer screen shows in red when the client version does not match.
     * An open upper end is shown as {@code 1.21.1+ Supported}.
     */
    public String label() {
        if (maximumVersion == null) {
            return minimumVersion + "+ Supported";
        }
        return minimumVersion + " - " + maximumVersion + " Supported";
    }

    /**
     * A protocol number that cannot equal the client's, so Minecraft paints {@link #label()} red
     * in place of the player count. The ping bars are measured separately and stay visible.
     */
    public int mismatchedProtocol(int clientProtocol) {
        return clientProtocol == -1 ? -2 : -1;
    }
}

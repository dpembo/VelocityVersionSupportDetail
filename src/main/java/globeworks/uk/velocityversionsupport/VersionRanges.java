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
 * Turns the version names in {@code config.yml} into the protocol numbers Velocity sees.
 */
public final class VersionRanges {

    private VersionRanges() {
    }

    public static VersionRange fromNames(String minimumVersion, String maximumVersion) {
        ProtocolVersion minimum = requireKnown(minimumVersion);
        ProtocolVersion maximum = requireKnown(maximumVersion);
        return new VersionRange(
                minimumVersion.trim(),
                maximumVersion.trim(),
                minimum.getProtocol(),
                maximum.getProtocol());
    }

    private static ProtocolVersion requireKnown(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Version name is blank");
        }
        String trimmed = name.trim();
        for (ProtocolVersion version : ProtocolVersion.values()) {
            if (!version.isSupported()) {
                continue;
            }
            for (String supported : version.getVersionsSupportedBy()) {
                if (supported.equals(trimmed)) {
                    return version;
                }
            }
        }
        throw new IllegalArgumentException("Unknown Minecraft version \"" + trimmed
                + "\". Use a release name this Velocity build understands, such as 1.21.4 or 26.1. "
                + "Versions that share a protocol, such as 1.21.7 and 1.21.8, cannot be split.");
    }
}

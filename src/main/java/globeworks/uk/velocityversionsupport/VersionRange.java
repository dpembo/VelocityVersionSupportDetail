/*
 * Copyright (C) 2026 GlobeWorks
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package globeworks.uk.velocityversionsupport;

/**
 * Inclusive protocol window, plus the version names shown to everyone outside it.
 */
public record VersionRange(
        String minimumVersion,
        String maximumVersion,
        int minimumProtocol,
        int maximumProtocol) {

    public VersionRange {
        if (minimumVersion == null || minimumVersion.isBlank()) {
            throw new IllegalArgumentException("minimum-version is blank");
        }
        if (maximumVersion == null || maximumVersion.isBlank()) {
            throw new IllegalArgumentException("maximum-version is blank");
        }
        if (minimumProtocol > maximumProtocol) {
            throw new IllegalArgumentException("minimum-version " + minimumVersion
                    + " is newer than maximum-version " + maximumVersion);
        }
    }

    public boolean allows(int protocol) {
        return protocol >= minimumProtocol && protocol <= maximumProtocol;
    }

    /**
     * Text the multiplayer screen shows in red when the client protocol does not match.
     */
    public String label() {
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

/*
 * Copyright (C) 2026 GlobeWorks
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package globeworks.uk.velocityversionsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.velocitypowered.api.network.ProtocolVersion;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class VersionRangeTest {

    @Test
    void labelUsesConfiguredNames() {
        VersionRange range = new VersionRange("1.21.4", "26.3", 769, 777);
        assertEquals("1.21.4 - 26.3 Supported", range.label());
    }

    @Test
    void rangeIsInclusive() {
        VersionRange range = new VersionRange("1.21.4", "26.3", 769, 777);
        assertTrue(range.allows(769));
        assertTrue(range.allows(773));
        assertTrue(range.allows(777));
        assertFalse(range.allows(768));
        assertFalse(range.allows(778));
        assertFalse(range.allows(-1));
        assertFalse(range.allows(-2));
    }

    @Test
    void mismatchedProtocolNeverEqualsTheClient() {
        VersionRange range = new VersionRange("1.21.4", "26.3", 769, 777);
        assertEquals(-1, range.mismatchedProtocol(763));
        assertEquals(-2, range.mismatchedProtocol(-1));
        assertFalse(range.allows(range.mismatchedProtocol(763)));
    }

    @Test
    void rejectsReversedProtocols() {
        assertThrows(IllegalArgumentException.class, () -> new VersionRange("26.3", "1.21.4", 777, 769));
    }

    @Test
    void resolvesNamesKnownToThisVelocityBuild() {
        ProtocolVersion minimum = firstSupported();
        ProtocolVersion maximum = lastSupported();
        VersionRange range = VersionRanges.fromNames(
                minimum.getVersionIntroducedIn(),
                maximum.getMostRecentSupportedVersion());
        assertTrue(range.allows(minimum.getProtocol()));
        assertTrue(range.allows(maximum.getProtocol()));
        assertEquals(
                minimum.getVersionIntroducedIn() + " - " + maximum.getMostRecentSupportedVersion() + " Supported",
                range.label());
    }

    @Test
    void rejectsUnknownVersionName() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> VersionRanges.fromNames("not-a-version", "not-a-version-either"));
        assertTrue(error.getMessage().contains("not-a-version"));
    }

    @Test
    void rejectsRangeWrittenBackwards() {
        ProtocolVersion minimum = firstSupported();
        ProtocolVersion maximum = lastSupported();
        if (minimum.getProtocol() == maximum.getProtocol()) {
            return;
        }
        assertThrows(
                IllegalArgumentException.class,
                () -> VersionRanges.fromNames(
                        maximum.getMostRecentSupportedVersion(),
                        minimum.getVersionIntroducedIn()));
    }

    private static ProtocolVersion firstSupported() {
        return Arrays.stream(ProtocolVersion.values())
                .filter(version -> version.isSupported())
                .findFirst()
                .orElseThrow();
    }

    private static ProtocolVersion lastSupported() {
        ProtocolVersion[] versions = ProtocolVersion.values();
        for (int i = versions.length - 1; i >= 0; i--) {
            if (versions[i].isSupported()) {
                return versions[i];
            }
        }
        throw new IllegalStateException("No supported protocol versions");
    }
}

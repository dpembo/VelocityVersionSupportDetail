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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.velocitypowered.api.network.ProtocolVersion;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class VersionRangeTest {

    @Test
    void labelUsesConfiguredNames() {
        VersionRange range = VersionRanges.fromNames("1.21.4", "26.3");
        assertEquals("1.21.4 - 26.3 Supported", range.label());
    }

    @Test
    void rangeIsInclusive() {
        VersionRange range = VersionRanges.fromNames("1.21.4", "26.3");
        assertTrue(range.allows(ProtocolVersion.MINECRAFT_1_21_4));
        assertTrue(range.allows(ProtocolVersion.MINECRAFT_1_21_9));
        assertTrue(range.allows(ProtocolVersion.MINECRAFT_26_3));
        assertFalse(range.allows(ProtocolVersion.MINECRAFT_1_21_2));
        assertFalse(range.allows(ProtocolVersion.UNKNOWN));
        assertFalse(range.allows(ProtocolVersion.LEGACY));
    }

    @Test
    void mismatchedProtocolNeverEqualsTheClient() {
        VersionRange range = VersionRanges.fromNames("1.21.4", "26.3");
        assertEquals(-1, range.mismatchedProtocol(763));
        assertEquals(-2, range.mismatchedProtocol(-1));
    }

    @Test
    void minimumAloneRejectsOlderClientsAndAcceptsNewerOnes() {
        VersionRange range = VersionRanges.fromNames("1.21.1", "");
        assertNull(range.maximum());
        assertEquals("1.21.1+ Supported", range.label());
        assertFalse(range.allows(ProtocolVersion.MINECRAFT_1_20_5));
        assertTrue(range.allows(ProtocolVersion.MINECRAFT_1_21));
        assertTrue(range.allows(ProtocolVersion.MINECRAFT_1_21_7));
        assertTrue(range.allows(ProtocolVersion.MINECRAFT_26_3));
    }

    @Test
    void rejectsReversedProtocols() {
        assertThrows(IllegalArgumentException.class, () -> new VersionRange(
                "26.3",
                "1.21.4",
                ProtocolVersion.MINECRAFT_26_3,
                ProtocolVersion.MINECRAFT_1_21_4));
    }

    @Test
    void resolvesNamesKnownToThisVelocityBuild() {
        ProtocolVersion minimum = firstSupported();
        ProtocolVersion maximum = lastSupported();
        VersionRange range = VersionRanges.fromNames(
                minimum.getVersionIntroducedIn(),
                maximum.getMostRecentSupportedVersion());
        assertTrue(range.allows(minimum));
        assertTrue(range.allows(maximum));
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
        if (minimum == maximum) {
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

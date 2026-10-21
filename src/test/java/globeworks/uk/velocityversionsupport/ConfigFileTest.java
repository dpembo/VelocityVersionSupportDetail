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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConfigFileTest {

    @Test
    void shippedDefaultConfigResolves() throws IOException {
        try (InputStream in = ConfigFile.class.getResourceAsStream("/config.yml")) {
            String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            VersionRange range = ConfigFile.fromLines(List.of(text.split("\\R", -1)));
            assertTrue(range.allows(range.minimum()));
            if (range.maximumVersion() == null) {
                assertEquals(range.minimumVersion() + "+ Supported", range.label());
            } else {
                assertEquals(range.minimumVersion() + " - " + range.maximumVersion() + " Supported", range.label());
                assertTrue(range.allows(range.maximum()));
            }
        }
    }

    @Test
    void readsQuotedValuesAndIgnoresComments() {
        VersionRange range = ConfigFile.fromLines(List.of(
                "# comment",
                "",
                "minimum-version: '1.21.4'",
                "maximum-version: \"1.21.5\""));
        assertEquals("1.21.4", range.minimumVersion());
        assertEquals("1.21.5", range.maximumVersion());
    }

    @Test
    void missingMaximumAcceptsAnythingAtOrAboveTheMinimum() {
        VersionRange range = ConfigFile.fromLines(List.of("minimum-version: \"1.21.1\""));
        assertNull(range.maximum());
        assertEquals("1.21.1+ Supported", range.label());
        assertFalse(range.allows(ProtocolVersion.MINECRAFT_1_20_5));
        assertTrue(range.allows(ProtocolVersion.MINECRAFT_1_21));
        assertTrue(range.allows(ProtocolVersion.MINECRAFT_1_21_7));
    }

    @Test
    void blankMaximumIsTheSameAsLeavingItOut() {
        VersionRange range = ConfigFile.fromLines(List.of(
                "minimum-version: \"1.21.4\"",
                "maximum-version: \"\""));
        assertNull(range.maximumVersion());
        assertEquals("1.21.4+ Supported", range.label());
    }

    @Test
    void rejectsAMissingMinimum() {
        assertThrows(IllegalArgumentException.class, () -> ConfigFile.fromLines(List.of(
                "maximum-version: \"26.3\"")));
    }
}

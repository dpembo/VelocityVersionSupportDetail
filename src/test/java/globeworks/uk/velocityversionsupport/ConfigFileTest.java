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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
            assertEquals("1.21.4 - 26.3 Supported", range.label());
            assertTrue(range.allows(range.minimumProtocol()));
            assertTrue(range.allows(range.maximumProtocol()));
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
    void rejectsAMissingKey() {
        assertThrows(IllegalArgumentException.class, () -> ConfigFile.fromLines(List.of(
                "minimum-version: \"1.21.4\"")));
    }
}

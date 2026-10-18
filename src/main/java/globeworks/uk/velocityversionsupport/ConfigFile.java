/*
 * Copyright (C) 2026 GlobeWorks
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package globeworks.uk.velocityversionsupport;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads the two version names from {@code config.yml}. The file is small enough that a dedicated
 * parser keeps the plugin off a configuration library.
 */
public final class ConfigFile {

    static final String MINIMUM_KEY = "minimum-version";
    static final String MAXIMUM_KEY = "maximum-version";

    private ConfigFile() {
    }

    public static VersionRange load(Path dataDirectory) throws IOException {
        Path file = dataDirectory.resolve("config.yml");
        if (Files.notExists(file)) {
            Files.createDirectories(dataDirectory);
            try (InputStream in = ConfigFile.class.getResourceAsStream("/config.yml")) {
                if (in == null) {
                    throw new IllegalStateException("Plugin is missing its default config.yml");
                }
                Files.copy(in, file);
            }
        }
        return fromLines(Files.readAllLines(file));
    }

    static VersionRange fromLines(List<String> lines) {
        Map<String, String> values = new HashMap<>();
        for (int i = 0; i < lines.size(); i++) {
            String raw = lines.get(i);
            String line = stripBom(raw).trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            int colon = line.indexOf(':');
            if (colon <= 0) {
                throw new IllegalArgumentException("Invalid config line " + (i + 1) + ": " + raw);
            }
            String key = line.substring(0, colon).trim();
            String value = unquote(line.substring(colon + 1).trim());
            if (values.put(key, value) != null) {
                throw new IllegalArgumentException("Duplicate config key \"" + key + "\"");
            }
        }
        String minimum = values.get(MINIMUM_KEY);
        String maximum = values.get(MAXIMUM_KEY);
        if (minimum == null || maximum == null) {
            throw new IllegalArgumentException("config.yml must set " + MINIMUM_KEY + " and " + MAXIMUM_KEY);
        }
        return VersionRanges.fromNames(minimum, maximum);
    }

    private static String stripBom(String line) {
        if (!line.isEmpty() && line.charAt(0) == '\uFEFF') {
            return line.substring(1);
        }
        return line;
    }

    private static String unquote(String value) {
        if (value.length() >= 2) {
            char open = value.charAt(0);
            char close = value.charAt(value.length() - 1);
            if ((open == '"' && close == '"') || (open == '\'' && close == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}

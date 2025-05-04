import java.util.regex.Pattern;

/**
 * NbVersionFormatter class for handling version number formatting.
 * Converts integer-encoded version numbers into string representations
 * and vice versa.
 */
public final class NbVersionUtils {
    // Prevent instantiation
    private NbVersionUtils() { }

    /**
     * Decodes the given integer into a version string.  
     * If the high nibble of the 16-bit value is zero, treats it as a 3-part version;
     * otherwise, as a 4-part version.
     *
     * @param encodedVersion the raw integer-encoded version
     * @return the reconstructed version string (e.g. "1.2.3" or "1.2.3.4")
     */
    public static String versionToString(int encodedVersion) {
        // Extract the top 4 bits of the high byte
        int highNibble = (encodedVersion >> 12) & 0xF;
        // Choose 3 or 4 parts based on high nibble
        int parts = (highNibble == 0) ? 3 : 4;
        return versionToString(parts, encodedVersion, getVersionSeparator());
    }

    /**
     * Overload for explicitly specifying number of parts.
     *
     * @param parts number of version components (3 or 4)
     * @param encodedVersion the raw integer-encoded version
     * @return the reconstructed version string
     */
    private static String versionToString(int parts, int encodedVersion) {
        return versionToString(parts, encodedVersion, getVersionSeparator());
    }

    /**
     * Determines the proper separator between version components.
     * Currently always returns a dot (".").
     *
     * @return the version separator string
     */
    private static String getVersionSeparator() {
        return ".";
    }

    /**
     * Core method to reconstruct the version string.
     * Shifts out each 4-bit nibble, from most significant to least,
     * and appends it, separated by the given delimiter.  
     * Also trims any leading/trailing ASCII whitespace characters.
     *
     * @param parts number of nibbles to decode
     * @param encodedVersion the raw integer-encoded version
     * @param sep the desired separator between parts
     * @return the trimmed version string
     */
    public static String versionToString(int parts, int encodedVersion, String sep) {
        StringBuilder sb = new StringBuilder();
        for (int i = parts - 1; i >= 0; i--) {
            // Extract 4-bit segment: if encodedVersion is 0, yields 0
            int partValue = (encodedVersion != 0)
                    ? (encodedVersion >> (i * 4)) & 0xF
                    : 0;
            sb.append(partValue);
            if (i != 0) {
                sb.append(sep);
            }
        }
        // Trim any leading/trailing whitespace (ASCII <= 32)
        return sb.toString().trim();
    }
    
    /**
     * Encodes a version string back into its integer representation, using the provided separator.
     *
     * @param version the version string to encode
     * @param sep the separator to split components
     * @return the integer-encoded version
     * @throws IllegalArgumentException if the format is invalid or components are out of range
     */
    public static int stringToVersion(String version, String sep) {
        if (version == null || version.trim().isEmpty()) {
            throw new IllegalArgumentException("version string is null or empty");
        }
        String[] parts = version.trim().split(Pattern.quote(sep));
        if (parts.length < 3 || parts.length > 4) {
            throw new IllegalArgumentException("invalid version format: " + version);
        }
        int encoded = 0;
        for (int i = 0; i < parts.length; i++) {
            int partValue;
            try {
                partValue = Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("invalid version component: " + parts[i], e);
            }
            if (partValue < 0 || partValue > 0xF) {
                throw new IllegalArgumentException(
                    "version part out of range (0-15): " + partValue);
            }
            int shift = (parts.length - 1 - i) * 4;
            encoded |= (partValue & 0xF) << shift;
        }
        return encoded;
    }
    
    /** 
     * Encodes a version string back into its integer representation.
     * Supports both 3-part (major.minor.patch) and 4-part (major.minor.patch.build).
     * Uses the locale-aware separator by default.
     *
     * @param version the version string to encode
     * @return the integer-encoded version
     * @throws IllegalArgumentException if the format is invalid or components are out of range
     */
    public static int stringToVersion(String version) {
        return stringToVersion(version, getVersionSeparator());
    }
}


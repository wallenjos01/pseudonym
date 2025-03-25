package org.wallentines.pseudonym.text;

import org.wallentines.mdcfg.serializer.InlineSerializer;
import org.wallentines.mdcfg.serializer.Serializer;

import java.util.Arrays;
import java.util.function.Function;

public enum TextColor implements Color {

    BLACK(0),
    DARK_BLUE(1),
    DARK_GREEN(2),
    DARK_AQUA(3),
    DARK_RED(4),
    DARK_PURPLE(5),
    GOLD(6),
    GRAY(7),
    DARK_GRAY(8),
    BLUE(9),
    GREEN(10),
    AQUA(11),
    RED(12),
    LIGHT_PURPLE(13),
    YELLOW(14),
    WHITE(15);


    private static final String[] LEGACY_COLOR_NAMES = { "black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple", "gold", "gray", "dark_gray", "blue", "green", "aqua", "red", "light_purple", "yellow", "white"};
    private static final String[] DYE_COLOR_NAMES = { "black", "blue", "green", "cyan", "red", "purple", "orange", "light_gray", "gray", "blue", "lime", "light_blue", "red", "pink", "yellow", "white"};
    private static final byte[] LEGACY_DATA_VALUES = { 15, 11, 13, 9, 14, 10, 1, 8, 7, 11, 5, 3, 14, 6, 4, 0 };
    private static final Color[] LEGACY_COLORS = new Color.RGB[] { new Color.RGB(0, 0, 0), new Color.RGB(0, 0, 170), new Color.RGB(0, 170, 0), new Color.RGB(0, 170, 170), new Color.RGB(170, 0, 0), new Color.RGB(170, 0, 170), new Color.RGB(255, 170, 0), new Color.RGB(170, 170, 170), new Color.RGB(85, 85, 85), new Color.RGB(85, 85, 255), new Color.RGB(85, 255, 85), new Color.RGB(85, 255, 255), new Color.RGB(255, 85, 85), new Color.RGB(255, 85, 255), new Color.RGB(255, 255, 85), new Color.RGB(255, 255, 255)};

    private final int index;

    TextColor(int index) {
        this.index = index;
    }

    @Override
    public int red() {
        return LEGACY_COLORS[index].red();
    }

    @Override
    public int green() {
        return LEGACY_COLORS[index].green();
    }

    @Override
    public int blue() {
        return LEGACY_COLORS[index].blue();
    }

    @Override
    public int alpha() {
        return 255;
    }

    @Override
    public int value() {
        return LEGACY_COLORS[index].value();
    }

    @Override
    public String toHex() {
        return LEGACY_COLORS[index].toHex();
    }

    public char getCharacter() {
        if(ordinal() < 10) {
            return (char) ('0' + ordinal());
        }
        if(ordinal() < 15) {
            return (char) ('a' + ordinal());
        }
        return 'f';
    }

    public byte dataValue() {
        return LEGACY_DATA_VALUES[index];
    }

    public String colorName() {
        return LEGACY_COLOR_NAMES[index];
    }

    public String dyeColorName() {
        return DYE_COLOR_NAMES[index];
    }


    /**
     * Gets an RGB color value based on the given name of a legacy Minecraft color (i.e. green/gold/light_purple)
     * @param name The name of the color to lookup
     * @return A color corresponding to the given name, or null
     */
    public static TextColor fromLegacyName(String name) {
        for (int i = 0; i < LEGACY_COLOR_NAMES.length; i++) {
            if (LEGACY_COLOR_NAMES[i].equals(name)) {
                return values()[i];
            }
        }
        return null;
    }

    /**
     * Gets an RGB color value based on the given dye name of a Minecraft dye name (i.e. line/orange/pink)
     * @param name The name of the dye color to lookup
     * @return A color corresponding to the given name, or null
     */
    public static TextColor fromDyeColor(String name) {
        for (int i = 0; i < DYE_COLOR_NAMES.length; i++) {
            if (DYE_COLOR_NAMES[i].equals(name)) {
                return values()[i];
            }
        }
        return null;
    }

    public static TextColor fromLegacyDataValue(int value) {
        for (int i = 0; i < LEGACY_DATA_VALUES.length; i++) {
            if (LEGACY_DATA_VALUES[i] == value) {
                return values()[i];
            }
        }
        return null;
    }

    public static TextColor getClosest(Color color) {
        return color.closest(Arrays.stream(values())).orElse(WHITE);
    }

    public static final Serializer<Color> LEGACY_SERIALIZER = InlineSerializer.of(TextColor::colorName, TextColor::fromLegacyName).flatMap(TextColor::getClosest, Function.identity());
    public static final Serializer<Color> NAME_SERIALIZER = InlineSerializer.<Color>of(color -> color.toRGB().toHex(), RGB::parseHex).or(LEGACY_SERIALIZER);
    public static final Serializer<Color> ARGB_SERIALIZER = Serializer.INT.flatMap(Color::value, ARGB::new);


//
//    /**
//     * Determines the legacy item data/damage value for a given color. (i.e pink_wool == wool:6 in versions pre-1.13)
//     * @param color The color to lookup
//     * @return The legacy item damage value closest to that color
//     */
//    public static byte getLegacyDataValue(Color color) {
//
//        return LEGACY_DATA_VALUES[color.closest(LEGACY_COLORS.)];
//    }
//
//    public static Color byLegacyDataValue(int data) {
//        return LEGACY_COLORS[data];
//    }
//
//    /**
//     * Parses a String into a color, either from a hex code, or from a legacy color name.
//     * @param value The value to parse
//     * @return The parsed color
//     */
//    public static Color parse(String value) {
//
//        if (value.startsWith("#")) return Color.parse(value).getOrThrow();
//        return fromLegacyName(value);
//    }
//
//    /**
//     * Gets the legacy color name from a given color (i.e. green/gold/light_purple)
//     * @param color The color to convert
//     * @return The legacy color closest to the given color
//     */
//    public static String toLegacyColor(Color color) {
//        return LEGACY_COLOR_NAMES[color.toRGBI()];
//    }
//
//    /**
//     * Gets the dye color name from a given color (i.e. line/orange/pink)
//     * @param color The color to convert
//     * @return The dye color closest to the given color
//     */
//    public static String toDyeColor(Color color) {
//        return DYE_COLOR_NAMES[color.toRGBI()];
//    }
//
//    /**
//     * Serializes the color into either an RGB hex value if the game version supports it, or a legacy color name otherwise
//     * @param color The color to serialize
//     * @return A color usable in components
//     */
//    public static String serialize(Color color, GameVersion version) {
//        return version.hasFeature(GameVersion.Feature.RGB_TEXT) ? color.toHex() : toLegacyColor(color);
//    }
//
//    public static final Serializer<Color> SERIALIZER = new Serializer<>() {
//        @Override
//        public <O> SerializeResult<O> serialize(SerializeContext<O> context, Color value) {
//            GameVersion ver = GameVersion.getVersion(context);
//            return SerializeResult.success(context.toString(TextColor.serialize(value, ver)));
//        }
//
//        @Override
//        public <O> SerializeResult<Color> deserialize(SerializeContext<O> context, O value) {
//            return context.asString(value).flatMap(TextColor::parse);
//        }
//    };
//
//    private TextColor() { }

}

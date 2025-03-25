package org.wallentines.pseudonym.text;

import java.util.Comparator;
import java.util.HexFormat;
import java.util.Optional;
import java.util.stream.Stream;

public interface Color {

    int red();
    int green();
    int blue();
    int alpha();

    int value();
    String toHex();

    default RGB toRGB() {
        return new RGB(red(), green(), blue());
    }

    default int distanceSquaredTo(Color other) {
        return (other.red() - red()) * (other.red() - red())
                + (other.green() - green()) * (other.green() - green())
                + (other.blue() - blue()) * (other.blue() - blue())
                + (other.alpha() - alpha()) * (other.alpha() - alpha());
    }

    default double distanceTo(Color other) {
        return Math.sqrt(distanceSquaredTo(other));
    }

    default <C extends Color> Optional<C> closest(Stream<C> colors) {
        return colors.min(Comparator.comparingInt(this::distanceSquaredTo));
    }

    record RGB(int value) implements Color {

        public RGB(int red, int green, int blue) {
            this((red & 0xFF) << 16 | (green & 0xFF) << 8 | (blue & 0xFF));
        }

        public static RGB parseHex(String hex) {
            if(hex.startsWith("#")) {
                hex = hex.substring(1);
            }
            if(hex.length() != 6) return null;
            return new RGB(HexFormat.fromHexDigits(hex));
        }


        @Override
        public int red() {
            return value >> 16 & 0xFF;
        }

        @Override
        public int green() {
            return value >> 8 & 0xFF;
        }

        @Override
        public int blue() {
            return value & 0xFF;
        }

        @Override
        public int alpha() {
            return 255;
        }

        @Override
        public String toHex() {
            return String.format("#%06X", value);
        }
    }

    record ARGB(int value) implements Color {

        public ARGB(int alpha, int red, int green, int blue) {
            this((alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | (blue & 0xFF));
        }

        public static ARGB parseHex(String hex) {
            if(hex.startsWith("#")) {
                hex = hex.substring(1);
            }
            if(hex.length() != 8) return null;
            return new ARGB(HexFormat.fromHexDigits(hex));
        }

        @Override
        public int red() {
            return value >> 16 & 0xFF;
        }

        @Override
        public int green() {
            return value >> 8 & 0xFF;
        }

        @Override
        public int blue() {
            return value & 0xFF;
        }

        @Override
        public int alpha() {
            return value >> 24 & 0xFF;
        }

        @Override
        public String toHex() {
            return String.format("#%08X", value);
        }
    }


}

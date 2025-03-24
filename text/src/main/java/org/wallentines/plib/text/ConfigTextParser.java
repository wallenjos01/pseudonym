package org.wallentines.plib.text;

import org.wallentines.mdcfg.Tuples;
import org.wallentines.plib.Color;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public record ConfigTextParser(char colorChar, boolean hexSupport, boolean shadowSupport) {

    public static final ConfigTextParser LEGACY = new ConfigTextParser('\u00A7', false, false);
    public static final ConfigTextParser INSTANCE = new ConfigTextParser('&', true, true);

    public String serialize(Component component) {
        return serialize(component, new StringBuilder());
    }

    private String serialize(Component component, StringBuilder out) {

        Style style = component.style();
        if(style.color() != null) {
            if(hexSupport) {
                out.append(colorChar).append(style.color().toHex());
            } else {
                TextColor color = TextColor.getClosest(style.color());
                out.append(colorChar).append(color.ordinal());
            }
        }
        if(shadowSupport && style.shadowColor() != null) {
            if(style.color() == null) out.append(colorChar);
            out.append(":").append(style.shadowColor().toHex());
        }
        if(style.isBold()) out.append(colorChar).append("l");
        if(style.isItalic()) out.append(colorChar).append("o");
        if(style.isUnderlined()) out.append(colorChar).append("n");
        if(style.isStrikethrough()) out.append(colorChar).append("m");
        if(style.isObfuscated()) out.append(colorChar).append("k");

        Content contents = component.content();
        if(contents.type() == Content.Type.TEXT) {
            out.append(((Content.Text) contents).text());
        }

        for(Component c : component.children()) {
            serialize(c, out);
        }

        return out.toString();
    }

    public Component parse(String text) {
        List<Component> split = parseSplit(text);
        if(split.isEmpty()) return Component.empty();

        Component first = split.getFirst();
        if(split.size() == 1) return first;

        MutableComponent out;
        int index = 0;
        if(first.style() == Style.EMPTY) {
            out = (MutableComponent) first;
            index = 1;
        } else {
            out = Component.empty();
        }

        for( ; index < split.size(); index++) {
            out.append(split.get(index));
        }
        return out;
    }

    public List<Component> parseSplit(String text) {

        List<Component> out = new ArrayList<>();

        AtomicReference<MutableComponent> currentRef = new AtomicReference<>();
        Consumer<MutableComponent> append = (mc) -> {
            if (currentRef.get() == null) {
                currentRef.set(mc);
            } else {
                currentRef.get().append(mc);
            }
        };

        StringBuilder currentString = new StringBuilder();
        Style currentStyle = Style.EMPTY;

        boolean justColored = false;

        int i;
        for (i = 0; i < text.length() - 1; i++) {

            char c = text.charAt(i);
            if (c == colorChar && i < text.length() - 1) {
                char next = text.charAt(i + 1);

                if (next == colorChar) {
                    currentString.append(colorChar);
                    i += 1;
                } else if ((next >= '0' && next <= '9') || (next >= 'a' && next <= 'f')) {

                    if (!currentString.isEmpty()) {
                        append.accept(Component.text(currentString.toString()).withStyle(currentStyle));
                        currentString = new StringBuilder();
                    }

                    i += 1;
                    currentStyle = Style.EMPTY.withColor(TextColor.values()[HexFormat.fromHexDigit(next)]);
                    justColored = true;

                } else if (next == 'r') {

                    if (!currentString.isEmpty()) {
                        append.accept(Component.text(currentString.toString()).withStyle(currentStyle));
                        currentString = new StringBuilder();
                    }
                    i += 1;

                    MutableComponent current = currentRef.get();
                    out.add(Objects.requireNonNullElseGet(current, Component::empty));
                    currentRef.set(null);

                    currentStyle = Style.EMPTY;
                    justColored = false;

                } else if (hexSupport && next == '#' && i < text.length() - 8) {

                    String hex = text.substring(i + 2, i + 8);
                    if (!currentString.isEmpty()) {
                        append.accept(Component.text(currentString.toString()).withStyle(currentStyle));
                        currentString = new StringBuilder();
                    }

                    i += 7;
                    currentStyle = Style.EMPTY.withColor(new Color.RGB(HexFormat.fromHexDigits(hex)));
                    justColored = true;

                } else if(shadowSupport && next == ':') {

                    Tuples.T2<Style, Integer> res = applyShadowColor(text, i + 1, currentStyle, currentString);
                    i = res.p2;
                    currentStyle = res.p1;

                    justColored = false;

                } else {
                    if (!currentString.isEmpty()) {
                        append.accept(Component.text(currentString.toString()).withStyle(currentStyle));
                        currentString = new StringBuilder();
                        currentStyle = Style.EMPTY;
                    }

                    currentStyle = switch (next) {
                        case 'l' -> { i++; yield currentStyle.withBold(true); }
                        case 'm' -> { i++; yield currentStyle.withItalic(true); }
                        case 'n' -> { i++; yield currentStyle.withUnderlined(true); }
                        case 'o' -> { i++; yield currentStyle.withStrikethrough(true); }
                        case 'k' -> { i++; yield currentStyle.withObfuscated(true); }
                        default -> currentStyle;
                    };

                    justColored = false;
                }

            } else if (justColored && shadowSupport && c == ':') {

                Tuples.T2<Style, Integer> res = applyShadowColor(text, i, currentStyle, currentString);
                i = res.p2;
                currentStyle = res.p1;

                justColored = false;

            } else {
                currentString.append(c);
                justColored = false;
            }
        }

        if (i < text.length()) {
            currentString.append(text.charAt(i));
        }

        if (!currentString.isEmpty()) {
            append.accept(Component.text(currentString.toString()).withStyle(currentStyle));
        }

        MutableComponent current = currentRef.get();
        out.add(Objects.requireNonNullElseGet(current, Component::empty));

        return out;
    }

    private Tuples.T2<Style, Integer> applyShadowColor(String text, int i, Style currentStyle, StringBuilder currentString) {

        char next = text.charAt(i + 1);
        if (next == ':') {
            currentString.append(':');
            return new Tuples.T2<>(currentStyle, i + 1);
        } else if ((next >= '0' && next <= '9') || (next >= 'a' && next <= 'f')) {
            return new Tuples.T2<>(currentStyle.withShadowColor(TextColor.values()[HexFormat.fromHexDigit(next)]), i + 1);
        } else if (hexSupport && next == '#' && i < text.length() - 10) {
            String hex = text.substring(i + 2, i + 10);
            return new Tuples.T2<>(currentStyle.withShadowColor(Color.ARGB.parseHex(hex)), i + 9);
        }
        return new Tuples.T2<>(currentStyle, i);
    }

}
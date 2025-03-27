package org.wallentines.pseudonym.mc.impl;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.PlainTextContents;
import org.wallentines.mdcfg.Tuples;
import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.pseudonym.mc.api.ConfigTextParser;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public record ConfigTextParserImpl(char colorChar, boolean hexSupport, boolean shadowSupport) implements ConfigTextParser {

    @Override
    public <O> SerializeResult<Component> deserialize(SerializeContext<O> ctx, O value) {
        return ctx.asString(value).flatMap(this::parse);
    }

    @Override
    public <O> SerializeResult<O> serialize(SerializeContext<O> ctx, Component component) {
        return SerializeResult.success(ctx.toString(serialize(component)));
    }

    @Override
    public String serialize(Component component) {
        return serialize(component, new StringBuilder());
    }

    private String serialize(Component component, StringBuilder out) {

        Style style = component.getStyle();
        if(style.getColor() != null) {
            if(hexSupport) {
                out.append(colorChar).append(String.format(Locale.ROOT, "#%06X", style.getColor().getValue()));
            } else {
                ChatFormatting fmt = ChatFormatting.getByName(style.getColor().serialize());
                if(fmt != null) {
                    out.append(colorChar).append(fmt.getChar());
                }
            }
        }
        if(shadowSupport && style.getShadowColor() != null) {
            if(style.getColor() == null) out.append(colorChar);
            out.append(":").append(String.format(Locale.ROOT, "#%08X", style.getShadowColor()));
        }
        if(style.isBold()) out.append(colorChar).append("l");
        if(style.isItalic()) out.append(colorChar).append("o");
        if(style.isUnderlined()) out.append(colorChar).append("n");
        if(style.isStrikethrough()) out.append(colorChar).append("m");
        if(style.isObfuscated()) out.append(colorChar).append("k");

        ComponentContents contents = component.getContents();
        if(contents.type() == PlainTextContents.TYPE) {
            out.append(((PlainTextContents) contents).text());
        }

        for(Component c : component.getSiblings()) {
            serialize(c, out);
        }

        return out.toString();
    }

    @Override
    public Component parse(String text) {
        List<Component> split = parseSplit(text);
        if(split.isEmpty()) return Component.empty();

        Component first = split.getFirst();
        if(split.size() == 1) return first;

        MutableComponent out;
        int index = 0;
        if(first.getStyle() == Style.EMPTY) {
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

    @Override
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
                        append.accept(Component.literal(currentString.toString()).withStyle(currentStyle));
                        currentString = new StringBuilder();
                    }

                    i += 1;
                    currentStyle = Style.EMPTY.withColor(TextColor.fromLegacyFormat(ChatFormatting.getByCode(next)));
                    justColored = true;

                } else if (next == 'r') {

                    if (!currentString.isEmpty()) {
                        append.accept(Component.literal(currentString.toString()).withStyle(currentStyle));
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
                        append.accept(Component.literal(currentString.toString()).withStyle(currentStyle));
                        currentString = new StringBuilder();
                    }

                    i += 7;
                    currentStyle = Style.EMPTY.withColor(TextColor.fromRgb(HexFormat.fromHexDigits(hex)));
                    justColored = true;

                } else if(shadowSupport && next == ':') {

                    Tuples.T2<Style, Integer> res = applyShadowColor(text, i + 1, currentStyle, currentString);
                    i = res.p2;
                    currentStyle = res.p1;

                    justColored = false;

                } else {
                    if (!currentString.isEmpty()) {
                        append.accept(Component.literal(currentString.toString()).withStyle(currentStyle));
                        currentString = new StringBuilder();
                        currentStyle = Style.EMPTY;
                    }

                    ChatFormatting fmt = ChatFormatting.getByCode(next);
                    if(fmt != null) {
                        i++;
                        currentStyle = currentStyle.applyLegacyFormat(fmt);
                    }
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
            append.accept(Component.literal(currentString.toString()).withStyle(currentStyle));
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
            int color = TextColor.fromLegacyFormat(ChatFormatting.getByCode(next)).getValue();
            return new Tuples.T2<>(currentStyle.withShadowColor(0xFF000000 | color), i + 1);
        } else if (hexSupport && next == '#' && i < text.length() - 10) {
            String hex = text.substring(i + 2, i + 10);
            return new Tuples.T2<>(currentStyle.withShadowColor(HexFormat.fromHexDigits(hex)), i + 9);
        }
        return new Tuples.T2<>(currentStyle, i);
    }

}

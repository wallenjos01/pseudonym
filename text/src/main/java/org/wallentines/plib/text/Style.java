package org.wallentines.plib.text;

import org.wallentines.mdcfg.registry.Identifier;
import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.Serializer;


public record Style(Color color, Boolean bold, Boolean italic, Boolean underlined, Boolean strikethrough,
                    Boolean obfuscated, Identifier font, String insertion, HoverEvent hoverEvent, ClickEvent clickEvent,
                    Color shadowColor) {

    public static final Style EMPTY = new Style(null, null, null, null, null, null, null, null, null, null, null);

    public Style withColor(Color color) {
        return new Style(color, bold, italic, underlined, strikethrough, obfuscated, font, insertion, hoverEvent, clickEvent, shadowColor);
    }

    public Style withBold(Boolean bold) {
        return new Style(color, bold, italic, underlined, strikethrough, obfuscated, font, insertion, hoverEvent, clickEvent, shadowColor);
    }

    public Style withItalic(Boolean italic) {
        return new Style(color, bold, italic, underlined, strikethrough, obfuscated, font, insertion, hoverEvent, clickEvent, shadowColor);
    }

    public Style withUnderlined(Boolean underlined) {
        return new Style(color, bold, italic, underlined, strikethrough, obfuscated, font, insertion, hoverEvent, clickEvent, shadowColor);
    }

    public Style withStrikethrough(Boolean strikethrough) {
        return new Style(color, bold, italic, underlined, strikethrough, obfuscated, font, insertion, hoverEvent, clickEvent, shadowColor);
    }

    public Style withObfuscated(Boolean obfuscated) {
        return new Style(color, bold, italic, underlined, strikethrough, obfuscated, font, insertion, hoverEvent, clickEvent, shadowColor);
    }

    public Style withFont(Identifier font) {
        return new Style(color, bold, italic, underlined, strikethrough, obfuscated, font, insertion, hoverEvent, clickEvent, shadowColor);
    }

    public Style withHoverEvent(HoverEvent hoverEvent) {
        return new Style(color, bold, italic, underlined, strikethrough, obfuscated, font, insertion, hoverEvent, clickEvent, shadowColor);
    }

    public Style withClickEvent(ClickEvent clickEvent) {
        return new Style(color, bold, italic, underlined, strikethrough, obfuscated, font, insertion, hoverEvent, clickEvent, shadowColor);
    }

    public Style withInsertion(String insertion) {
        return new Style(color, bold, italic, underlined, strikethrough, obfuscated, font, insertion, hoverEvent, clickEvent, shadowColor);
    }

    public Style withShadowColor(Color shadowColor) {
        return new Style(color, bold, italic, underlined, strikethrough, obfuscated, font, insertion, hoverEvent, clickEvent, shadowColor);
    }


    public boolean isBold() { return Boolean.TRUE.equals(bold); }
    public boolean isItalic() { return Boolean.TRUE.equals(italic); }
    public boolean isUnderlined() { return Boolean.TRUE.equals(underlined); }
    public boolean isStrikethrough() { return Boolean.TRUE.equals(strikethrough); }
    public boolean isObfuscated() { return Boolean.TRUE.equals(obfuscated); }



    public static final Serializer<Style> LEGACY_SERIALIZER = ObjectSerializer.create(
            TextColor.LEGACY_SERIALIZER.entry("color", Style::color).optional(),
            Serializer.BOOLEAN.entry("bold", Style::bold).optional(),
            Serializer.BOOLEAN.entry("italic", Style::italic).optional(),
            Serializer.BOOLEAN.entry("underlined", Style::underlined).optional(),
            Serializer.BOOLEAN.entry("strikethrough", Style::strikethrough).optional(),
            Serializer.BOOLEAN.entry("obfuscated", Style::obfuscated).optional(),
            Serializer.STRING.entry("insertion", Style::insertion).optional(),
            HoverEvent.SERIALIZER.entry("hoverEvent", Style::hoverEvent).optional(),
            ClickEvent.SERIALIZER.entry("clickEvent", Style::clickEvent).optional(),
            (c,b,i,u,s,o,in,hE,cE) ->
                    new Style(c, b, i, u, s, o, null, in, hE, cE, null)
    );

    public static final Serializer<Style> RGB_SERIALIZER = ObjectSerializer.create(
            TextColor.NAME_SERIALIZER.entry("color", Style::color).optional(),
            Serializer.BOOLEAN.entry("bold", Style::bold).optional(),
            Serializer.BOOLEAN.entry("italic", Style::italic).optional(),
            Serializer.BOOLEAN.entry("underlined", Style::underlined).optional(),
            Serializer.BOOLEAN.entry("strikethrough", Style::strikethrough).optional(),
            Serializer.BOOLEAN.entry("obfuscated", Style::obfuscated).optional(),
            Identifier.serializer("minecraft").entry("font", Style::font).optional(),
            Serializer.STRING.entry("insertion", Style::insertion).optional(),
            HoverEvent.SERIALIZER.entry("hoverEvent", Style::hoverEvent).optional(),
            ClickEvent.SERIALIZER.entry("clickEvent", Style::clickEvent).optional(),
            (c,b,i,u,s,o,f, in,hE,cE) ->
                    new Style(c, b, i, u, s, o, f, in, hE, cE, null)
    );

    public static final Serializer<Style> SHADOW_SERIALIZER = ObjectSerializer.create(
            TextColor.NAME_SERIALIZER.entry("color", Style::color).optional(),
            Serializer.BOOLEAN.entry("bold", Style::bold).optional(),
            Serializer.BOOLEAN.entry("italic", Style::italic).optional(),
            Serializer.BOOLEAN.entry("underlined", Style::underlined).optional(),
            Serializer.BOOLEAN.entry("strikethrough", Style::strikethrough).optional(),
            Serializer.BOOLEAN.entry("obfuscated", Style::obfuscated).optional(),
            Identifier.serializer("minecraft").entry("font", Style::font).optional(),
            Serializer.STRING.entry("insertion", Style::insertion).optional(),
            HoverEvent.SERIALIZER.entry("hoverEvent", Style::hoverEvent).optional(),
            ClickEvent.SERIALIZER.entry("clickEvent", Style::clickEvent).optional(),
            TextColor.ARGB_SERIALIZER.entry("shadow_color", Style::shadowColor).optional(),
            Style::new
    );

    public static final Serializer<Style> MODERN_SERIALIZER = ObjectSerializer.create(
            TextColor.NAME_SERIALIZER.entry("color", Style::color).optional(),
            Serializer.BOOLEAN.entry("bold", Style::bold).optional(),
            Serializer.BOOLEAN.entry("italic", Style::italic).optional(),
            Serializer.BOOLEAN.entry("underlined", Style::underlined).optional(),
            Serializer.BOOLEAN.entry("strikethrough", Style::strikethrough).optional(),
            Serializer.BOOLEAN.entry("obfuscated", Style::obfuscated).optional(),
            Identifier.serializer("minecraft").entry("font", Style::font).optional(),
            Serializer.STRING.entry("insertion", Style::insertion).optional(),
            HoverEvent.SERIALIZER.entry("hover_event", Style::hoverEvent).optional(),
            ClickEvent.SERIALIZER.entry("click_event", Style::clickEvent).optional(),
            TextColor.ARGB_SERIALIZER.entry("shadow_color", Style::shadowColor).optional(),
            Style::new
    );

    public static final Serializer<Style> SERIALIZER = ProtocolContext.select(ctx -> {
        if(ctx.hasFeature(Features.INLINE_HOVER_CONTENTS)) return MODERN_SERIALIZER;
        if(ctx.hasFeature(Features.SHADOW_COLOR)) return SHADOW_SERIALIZER;
        if(ctx.hasFeature(Features.RGB_TEXT)) return RGB_SERIALIZER;
        return LEGACY_SERIALIZER;
    });

}

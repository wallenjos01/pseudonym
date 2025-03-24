package org.wallentines.plib;

import org.jetbrains.annotations.Nullable;
import org.wallentines.mdcfg.Either;
import org.wallentines.mdcfg.Tuples;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderParser implements MessagePipeline.PipelineStage<String, UnresolvedMessage<String>> {

    private static final Pattern TAG = Pattern.compile("<(/?)(" + PlaceholderManager.VALID_PLACEHOLDER.pattern() + ")>");

    private final PlaceholderManager placeholderManager;

    public PlaceholderParser(PlaceholderManager placeholderManager) {
        this.placeholderManager = placeholderManager;
    }

    @Override
    public UnresolvedMessage<String> apply(String message, PipelineContext ctx) {
        return parse(message);
    }

    public UnresolvedMessage<String> parse(String message) {
        return parseInternal(message, 0, null).p1;
    }

    private Tuples.T2<UnresolvedMessage<String>, Integer> parseInternal(String message, int start, @Nullable String tagName) {

        List<Either<String, PlaceholderInstance<?, ?>>> parts = new ArrayList<>();

        int lastStart = start;

        Matcher matcher = TAG.matcher(message);
        while(matcher.find(start)) {
            String placeholderId = matcher.group(2);
            start = matcher.end(2) + 1;

            int tagStart = matcher.start(1);
            if(tagStart - lastStart > 0) {
                parts.add(Either.left(message.substring(lastStart, tagStart - 1)));
            }

            if(!matcher.group(1).isEmpty()) {
                if(placeholderId.equals(tagName)) {
                    return new Tuples.T2<>(new UnresolvedMessage<>(parts), start);
                } else {
                    continue;
                }
            }


            Placeholder<?, ?> pl = placeholderManager.get(placeholderId);
            UnresolvedMessage<String> param = null;
            if(pl.acceptsParameter()) {
                Tuples.T2<UnresolvedMessage<String>, Integer> t2 = parseInternal(message, matcher.end(2) + 1, placeholderId);
                param = t2.p1;
                start = t2.p2;
            }

            parts.add(Either.right(pl.instantiate(param)));

            lastStart = start;
        }

        if(start < message.length()) {
            parts.add(Either.left(message.substring(start)));
        }

        return new Tuples.T2<>(new UnresolvedMessage<>(parts), start);
    }
}

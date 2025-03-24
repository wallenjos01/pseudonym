package org.wallentines.plib.text;

import org.wallentines.mdcfg.registry.Registry;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;

import java.net.URI;
import java.util.Objects;

public interface ClickEvent {

    Action<?> action();

    static <T> Simple<T> create(Simple.Action<T> act, T value) {
        return new Simple<>(act, value);
    }

    class Simple<T> implements ClickEvent {

        private final Action<T> action;
        private final T value;

        public Simple(Action<T> action, T value) {
            this.action = action;
            this.value = value;
        }

        public Action<T> action() {
            return action;
        }

        public T value() {
            return value;
        }

        public static class Action<T> implements ClickEvent.Action<Simple<T>> {
            private final Serializer<Simple<T>> serializer;

            Action(Serializer<T> serializer) {
                this.serializer = serializer.flatMap(Simple::value, value -> new Simple<>(this, value));
            }

            @Override
            public Serializer<Simple<T>> serializer() {
                return serializer;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Simple<?> simple = (Simple<?>) o;
            return Objects.equals(action, simple.action) && Objects.equals(value, simple.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(action, value);
        }

        @Override
        public String toString() {
            return "Simple{" +
                    "action=" + action +
                    ", value=" + value +
                    '}';
        }
    }

    interface Action<T extends ClickEvent> {
        Serializer<T> serializer();
        Registry<String, Action<?>> REGISTRY = Registry.createStringRegistry();

        static <T extends ClickEvent, A extends ClickEvent.Action<T>> A register(String name, A action) {
            REGISTRY.register(name, action);
            return action;
        }

        ClickEvent.Simple.Action<URI> OPEN_URL = register("open_url", new Simple.Action<>(ProtocolContext.select(ctx ->
                Serializer.STRING.fieldOf(ctx.hasFeature(Features.INLINE_HOVER_CONTENTS) ? "url" : "value")
                        .map(uri -> SerializeResult.success(uri.toString()), str -> {
                            try {
                                URI uri = URI.create(str);
                                if(uri.getScheme().equalsIgnoreCase("http") || uri.getScheme().equalsIgnoreCase("https")) {
                                    return SerializeResult.success(uri);
                                }
                                return SerializeResult.failure("Invalid URI scheme: " + uri.getScheme() + ", for " + str);
                            } catch (Exception e) {
                                return SerializeResult.failure("Unable to parse URI: " + str, e);
                            }
                        })
        )));

        ClickEvent.Simple.Action<String> OPEN_FILE = register("open_file", new Simple.Action<>(ProtocolContext.select(ctx ->
                Serializer.STRING.fieldOf(ctx.hasFeature(Features.INLINE_HOVER_CONTENTS) ? "path" : "value")
        )));

        ClickEvent.Simple.Action<String> RUN_COMMAND = register("run_command", new Simple.Action<>(ProtocolContext.select(ctx ->
                Serializer.STRING.fieldOf(ctx.hasFeature(Features.INLINE_HOVER_CONTENTS) ? "command" : "value")
        )));

        ClickEvent.Simple.Action<String> SUGGEST_COMMAND = register("suggest_command", new Simple.Action<>(ProtocolContext.select(ctx ->
                Serializer.STRING.fieldOf(ctx.hasFeature(Features.INLINE_HOVER_CONTENTS) ? "command" : "value")
        )));

        ClickEvent.Simple.Action<Integer> CHANGE_PAGE = register("change_page", new Simple.Action<>(ProtocolContext.select(ctx ->
                ctx.hasFeature(Features.INLINE_HOVER_CONTENTS) ? Serializer.INT.fieldOf("page") : Serializer.STRING.fieldOf("value")
                        .map(
                                i -> SerializeResult.success(Objects.toString(i)),
                                str -> {
                                    try {
                                        return SerializeResult.success(Integer.parseInt(str));
                                    } catch (Exception ex) {
                                        return SerializeResult.failure("Unable to parse page number: " + str, ex);
                                    }
                                }
                        )

        )));

        ClickEvent.Simple.Action<String> COPY_TO_CLIPBOARD = register("copy_to_clipboard", new Simple.Action<>(Serializer.STRING.fieldOf("value")));

    }

    Serializer<ClickEvent> SERIALIZER = ClickEvent.Action.REGISTRY.byIdSerializer().fieldOf("action").dispatch(ClickEvent.Action::serializer, ClickEvent::action);
    Serializer<ClickEvent> MAP_SERIALIZER = ProtocolContext.select(ctx -> {
        if(ctx.hasFeature(Features.INLINE_HOVER_CONTENTS)) return SERIALIZER.fieldOf("click_event");
        return SERIALIZER.fieldOf("clickEvent");
    });

}

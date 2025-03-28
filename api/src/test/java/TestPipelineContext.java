import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.pseudonym.PipelineContext;

import java.util.stream.Stream;

public class TestPipelineContext {

    @Test
    public void worksWhenEmpty() {

        PipelineContext ctx = PipelineContext.EMPTY;

        Assertions.assertEquals(0, ctx.values().size());

        Assertions.assertFalse(ctx.getByClass(Object.class).findFirst().isPresent());
        Assertions.assertFalse(ctx.getFirst(Object.class).isPresent());
    }

    @Test
    public void worksWhenSingle() {

        PipelineContext ctx = PipelineContext.of("Hello");

        Assertions.assertEquals(1, ctx.values().size());

        Assertions.assertTrue(ctx.getByClass(Object.class).findFirst().isPresent());
        Assertions.assertTrue(ctx.getFirst(Object.class).isPresent());

        Assertions.assertTrue(ctx.getByClass(String.class).findFirst().isPresent());
        Assertions.assertTrue(ctx.getFirst(String.class).isPresent());
        Assertions.assertEquals("Hello", ctx.getFirst(String.class).get());
    }

    @Test
    public void worksWhenMultiple() {

        PipelineContext ctx = PipelineContext.of("Hello", "World");

        Assertions.assertEquals(2, ctx.values().size());

        Assertions.assertTrue(ctx.getByClass(String.class).findFirst().isPresent());
        Assertions.assertEquals(2, ctx.getByClass(String.class).toList().size());
        Assertions.assertTrue(ctx.getFirst(String.class).isPresent());
        Assertions.assertEquals("Hello", ctx.getFirst(String.class).get());
    }

    @Test
    public void worksWhenMultipleDifferentTypes() {

        PipelineContext ctx = PipelineContext.of("Hello", 3);

        Assertions.assertEquals(2, ctx.values().size());

        Assertions.assertTrue(ctx.getByClass(String.class).findFirst().isPresent());
        Assertions.assertEquals(1, ctx.getByClass(String.class).toList().size());
        Assertions.assertTrue(ctx.getFirst(String.class).isPresent());
        Assertions.assertEquals("Hello", ctx.getFirst(String.class).get());

        Assertions.assertTrue(ctx.getByClass(Integer.class).findFirst().isPresent());
        Assertions.assertEquals(1, ctx.getByClass(Integer.class).toList().size());
        Assertions.assertTrue(ctx.getFirst(Integer.class).isPresent());
        Assertions.assertEquals(3, ctx.getFirst(Integer.class).get());
    }

    @Test
    public void andSingleWorks() {
        PipelineContext ctx = PipelineContext.of("Hello").and(PipelineContext.of("World"));

        Assertions.assertEquals(2, ctx.values().size());

        Assertions.assertTrue(ctx.getByClass(String.class).findFirst().isPresent());
        Assertions.assertEquals(2, ctx.getByClass(String.class).toList().size());
        Assertions.assertTrue(ctx.getFirst(String.class).isPresent());
        Assertions.assertEquals("Hello", ctx.getFirst(String.class).get());
    }

    @Test
    public void andStreamWorks() {

        PipelineContext ctx = PipelineContext.of("Hello").and(
                Stream.of(PipelineContext.of("World"), PipelineContext.of(2))
        );

        Assertions.assertEquals(3, ctx.values().size());

        Assertions.assertTrue(ctx.getByClass(String.class).findFirst().isPresent());
        Assertions.assertEquals(2, ctx.getByClass(String.class).toList().size());
        Assertions.assertTrue(ctx.getFirst(String.class).isPresent());
        Assertions.assertEquals("Hello", ctx.getFirst(String.class).get());

        Assertions.assertTrue(ctx.getByClass(Integer.class).findFirst().isPresent());
        Assertions.assertEquals(1, ctx.getByClass(Integer.class).toList().size());
        Assertions.assertTrue(ctx.getFirst(Integer.class).isPresent());
        Assertions.assertEquals(2, ctx.getFirst(Integer.class).get());
    }



}

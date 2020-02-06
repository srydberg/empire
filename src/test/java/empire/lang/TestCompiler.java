package empire.lang;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestCompiler {
    @Test
    public void emptyShouldCompile() throws IOException {
        String result = compile("");
        assertEquals("", result);
    }

    @Test
    public void bareShouldCompile() throws IOException {
        String result = compile("() {a\n}");
        assertEquals("a\n", result);
    }

    @Test
    public void invalidBindingsShouldCompile() throws IOException {
        String result = compile("bind a=b\n(a=b) {c\n}");
        assertEquals("", result);
    }

    @Test
    public void conditionShouldCompile() throws IOException {
        String result = compile("bind a=a\n(a=b) {c\n}", "a", "b");
        assertEquals("c\n", result);
    }

    @Test
    public void regexConditionShouldCompile() throws IOException {
        String result = compile("bind a=a\n(a=~/[b]/) {c\n}", "a", "b");
        assertEquals("c\n", result);
    }

    @Test
    public void invalidRegexConditionShouldCompile() throws IOException {
        String result = compile("bind a=a\n(a=~/[b]/) {c\n}", "a", "x");
        assertEquals("", result);
    }

    @Test
    public void regexEscapeConditionShouldCompile() throws IOException {
        String result = compile("bind a=a\n(a=~/\\/b/) {c\n}", "a", "/b");
        assertEquals("c\n", result);
    }

    @Test
    public void shouldCompileFile() throws IOException {
        String source = readUTF8File("Test.empire");
        String result = compile(source, "b", "x", "d", "y");
        assertEquals("property=value\nkey=value\n", result);
    }


    /**
     * Helper methods
     */

    private String compile(String source, String... props) throws IOException {
        Map<String, Object> properties = new HashMap<>();
        for (int i = 0; i < props.length; i += 2) {
            properties.put(props[i], props[i + 1]);
        }

        ByteArrayInputStream input = new ByteArrayInputStream(source.getBytes());
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        Compiler compiler = new Compiler(input, output, properties);
        compiler.compile();

        return new String(output.toByteArray());
    }

    private String readUTF8File(String file) throws IOException {
        URL resource = this.getClass().getResource("/" + file);

        if (resource == null)
            throw new IOException("Failed to get '" + file + "'");

        byte[] encoded = Files.readAllBytes(Paths.get(resource.getPath()));

        Charset utf8 = StandardCharsets.UTF_8;
        CharBuffer buffer = utf8.decode(ByteBuffer.wrap(encoded));

        return buffer.toString();
    }
}

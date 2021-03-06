package io.sited.template;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.sited.resource.ClasspathResourceRepository;
import io.sited.resource.SingleResourceRepository;
import io.sited.resource.StringResource;
import io.sited.util.collection.QueryResponse;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * @author chi
 */
class TemplateEngineTest {
    @Test
    void template() throws IOException {
        Template template = templateEngine().template("/simple.html").orElseThrow(RuntimeException::new);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HashMap<String, Object> bindings = Maps.newHashMap();
        SimpleModel simpleModel = new SimpleModel();
        simpleModel.list = new QueryResponse<>();
        simpleModel.list.items = Lists.newArrayList("1", "2", "3");
        bindings.put("model", simpleModel);
        bindings.put("html", "<p>1</p>");
        bindings.put("disabled", true);
        bindings.put("active", true);
        template.output(bindings, outputStream);
        assertEquals("<!doctype html><html lang=\"en-US\" xmlns:j=\"http://www.w3.org/1999/xhtml\"><head><meta charset=\"UTF-8\"/></head><body><div><p>1</p></div><div>&lt;p&gt;1&lt;/p&gt;</div><ul disabled class=\"active list\"><li>1</li><li>2</li><li>3</li></ul></body></html>", new String(outputStream.toByteArray(), Charsets.UTF_8));
    }

    @Test
    void text() throws IOException {
        Template template = singleTemplateEngine("/template.txt", "hi, {{user}}.").template("/template.txt").orElseThrow(RuntimeException::new);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HashMap<String, Object> bindings = Maps.newHashMap();
        bindings.put("user", "test");
        template.output(bindings, outputStream);
        assertEquals("hi, test.", new String(outputStream.toByteArray(), Charsets.UTF_8));
    }

    @Test
    void list() throws IOException {
        Template template = singleTemplateEngine("/template.txt", "{{list.get(0).substring(2)}}").template("/template.txt").orElseThrow(RuntimeException::new);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HashMap<String, Object> bindings = Maps.newHashMap();
        bindings.put("list", Lists.newArrayList("test"));
        template.output(bindings, outputStream);
        assertEquals("st", new String(outputStream.toByteArray(), Charsets.UTF_8));
    }

    @Test
    void function() throws IOException {
        Template template = singleTemplateEngine("/template.txt", "{{ellipsis(description,10)}}").template("/template.txt").orElseThrow(RuntimeException::new);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Map<String, Object> bindings = Maps.newHashMap();
        bindings.put("description", "this is a very long description");
        template.output(bindings, outputStream);
        assertEquals("this is a ...", new String(outputStream.toByteArray(), Charsets.UTF_8));
    }

    @Test
    public void forIndex() throws IOException {
        Template template = singleTemplateEngine("/template.html", "<!doctype html><html><div j:for='item:list'>{{$index}}</div></html>").template("/template.html").orElseThrow(RuntimeException::new);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Map<String, Object> bindings = Maps.newHashMap();
        bindings.put("list", Lists.newArrayList(1, 2, 3));
        template.output(bindings, outputStream);
        assertEquals("<!doctype html><html><div>0</div><div>1</div><div>2</div></html>", new String(outputStream.toByteArray(), Charsets.UTF_8));
    }

    @Test
    void nestComponent() throws IOException {
        TemplateEngine templateEngine = templateEngine();
        templateEngine.addComponent(new TemplateComponentImpl("component1", "component/component1.html", ImmutableList.of()));
        templateEngine.addComponent(new TemplateComponentImpl("component2", "component/component2.html", ImmutableList.of()));
        Template template = templateEngine.template("test-nest-component.html").orElseThrow(RuntimeException::new);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Map<String, Object> bindings = Maps.newHashMap();
        template.output(bindings, outputStream);
        assertTrue(new String(outputStream.toByteArray(), Charsets.UTF_8).contains(".component2"));
    }

    @Test
    void component() throws IOException {
        TemplateEngine templateEngine = templateEngine();
        templateEngine.addComponent(new TemplateComponentImpl("header", "component/header.html", ImmutableList.of()));
        templateEngine.addComponent(new TemplateComponentImpl("footer", "component/footer.html", ImmutableList.of()));

        Template template = templateEngine.template("test-header.html").orElseThrow(RuntimeException::new);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Map<String, Object> bindings = Maps.newHashMap();
        bindings.put("selected", "home");
        bindings.put("name", "hello");
        template.output(bindings, outputStream);
        assertNotNull(outputStream.toByteArray());
    }

    @Test
    void ieComment() throws IOException {
        Template template = templateEngine().template("test-ie-comment.html").orElseThrow(RuntimeException::new);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        template.output(Maps.newHashMap(), outputStream);
        assertNotNull(outputStream.toByteArray());
    }

    @Test
    void include() throws IOException {
        Template template = templateEngine().template("test-auto-component.html").orElseThrow(RuntimeException::new);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        template.output(Maps.newHashMap(), outputStream);
        assertEquals("<!doctype html><html lang=\"en-US\" xmlns:j=\"\"><head></head><body>value</body></html>", new String(outputStream.toByteArray(), Charsets.UTF_8));
    }


    private TemplateEngine templateEngine() {
        return new TemplateEngine().addRepository(new ClasspathResourceRepository("web/template"));
    }

    private TemplateEngine singleTemplateEngine(String templatePath, String content) {
        return new TemplateEngine().addRepository(new SingleResourceRepository(new StringResource(templatePath, content)));
    }

    public static class SimpleModel {
        public QueryResponse<String> list;
        public String name = "some";
    }

    public static class TemplateComponentImpl extends TemplateComponent {
        TemplateComponentImpl(String name, String templatePath, List<ComponentAttribute<?>> componentAttributes) {
            super(name, templatePath, componentAttributes);
        }
    }
}
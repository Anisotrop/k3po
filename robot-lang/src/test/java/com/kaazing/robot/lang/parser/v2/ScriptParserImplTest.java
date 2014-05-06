/**
 * Copyright (c) 2007-2013, Kaazing Corporation. All rights reserved.
 */

package com.kaazing.robot.lang.parser.v2;

import static com.kaazing.robot.lang.parser.v2.ScriptParseStrategy.ACCEPT;
import static com.kaazing.robot.lang.parser.v2.ScriptParseStrategy.CLOSE;
import static com.kaazing.robot.lang.parser.v2.ScriptParseStrategy.CLOSED;
import static com.kaazing.robot.lang.parser.v2.ScriptParseStrategy.CONNECTED;
import static com.kaazing.robot.lang.parser.v2.ScriptParseStrategy.EXACT_BYTES_MATCHER;
import static com.kaazing.robot.lang.parser.v2.ScriptParseStrategy.EXPRESSION_MATCHER;
import static com.kaazing.robot.lang.parser.v2.ScriptParseStrategy.FIXED_LENGTH_BYTES_MATCHER;
import static com.kaazing.robot.lang.parser.v2.ScriptParseStrategy.LITERAL_BYTES_VALUE;
import static com.kaazing.robot.lang.parser.v2.ScriptParseStrategy.LITERAL_TEXT_VALUE;
import static com.kaazing.robot.lang.parser.v2.ScriptParseStrategy.MATCHER_LIST;
import static com.kaazing.robot.lang.parser.v2.ScriptParseStrategy.READ;
import static com.kaazing.robot.lang.parser.v2.ScriptParseStrategy.READ_AWAIT;
import static com.kaazing.robot.lang.parser.v2.ScriptParseStrategy.READ_NOTIFY;
import static com.kaazing.robot.lang.parser.v2.ScriptParseStrategy.REGEX_MATCHER;
import static com.kaazing.robot.lang.parser.v2.ScriptParseStrategy.SCRIPT;
import static com.kaazing.robot.lang.parser.v2.ScriptParseStrategy.VARIABLE_LENGTH_BYTES_MATCHER;
import static com.kaazing.robot.lang.parser.v2.ScriptParseStrategy.WRITE;
import static com.kaazing.robot.lang.parser.v2.ScriptParseStrategy.WRITE_AWAIT;
import static com.kaazing.robot.lang.parser.v2.ScriptParseStrategy.WRITE_NOTIFY;
import static com.kaazing.robot.lang.parser.v2.ScriptParseStrategy.WRITE_VALUE_LIST;
import static com.kaazing.robot.lang.test.junit.Assert.assertEquals;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import org.junit.Ignore;
import org.junit.Test;

import com.kaazing.robot.lang.ast.AstAcceptNode;
import com.kaazing.robot.lang.ast.AstCloseNode;
import com.kaazing.robot.lang.ast.AstClosedNode;
import com.kaazing.robot.lang.ast.AstConnectedNode;
import com.kaazing.robot.lang.ast.AstReadAwaitNode;
import com.kaazing.robot.lang.ast.AstReadNotifyNode;
import com.kaazing.robot.lang.ast.AstReadValueNode;
import com.kaazing.robot.lang.ast.AstScriptNode;
import com.kaazing.robot.lang.ast.AstWriteAwaitNode;
import com.kaazing.robot.lang.ast.AstWriteNotifyNode;
import com.kaazing.robot.lang.ast.AstWriteValueNode;
import com.kaazing.robot.lang.ast.builder.AstAcceptNodeBuilder;
import com.kaazing.robot.lang.ast.builder.AstCloseNodeBuilder;
import com.kaazing.robot.lang.ast.builder.AstClosedNodeBuilder;
import com.kaazing.robot.lang.ast.builder.AstConnectedNodeBuilder;
import com.kaazing.robot.lang.ast.builder.AstReadAwaitNodeBuilder;
import com.kaazing.robot.lang.ast.builder.AstReadNodeBuilder;
import com.kaazing.robot.lang.ast.builder.AstReadNotifyNodeBuilder;
import com.kaazing.robot.lang.ast.builder.AstScriptNodeBuilder;
import com.kaazing.robot.lang.ast.builder.AstWriteAwaitNodeBuilder;
import com.kaazing.robot.lang.ast.builder.AstWriteNodeBuilder;
import com.kaazing.robot.lang.ast.builder.AstWriteNotifyNodeBuilder;
import com.kaazing.robot.lang.ast.matcher.AstByteLengthBytesMatcher;
import com.kaazing.robot.lang.ast.matcher.AstExactBytesMatcher;
import com.kaazing.robot.lang.ast.matcher.AstExactTextMatcher;
import com.kaazing.robot.lang.ast.matcher.AstExpressionMatcher;
import com.kaazing.robot.lang.ast.matcher.AstFixedLengthBytesMatcher;
import com.kaazing.robot.lang.ast.matcher.AstIntLengthBytesMatcher;
import com.kaazing.robot.lang.ast.matcher.AstLongLengthBytesMatcher;
import com.kaazing.robot.lang.ast.matcher.AstRegexMatcher;
import com.kaazing.robot.lang.ast.matcher.AstShortLengthBytesMatcher;
import com.kaazing.robot.lang.ast.matcher.AstValueMatcher;
import com.kaazing.robot.lang.ast.matcher.AstVariableLengthBytesMatcher;
import com.kaazing.robot.lang.ast.value.AstExpressionValue;
import com.kaazing.robot.lang.ast.value.AstLiteralBytesValue;
import com.kaazing.robot.lang.ast.value.AstLiteralTextValue;
import com.kaazing.robot.lang.ast.value.AstValue;
import com.kaazing.robot.lang.el.ExpressionContext;
import com.kaazing.robot.lang.parser.ScriptParseException;
import com.kaazing.robot.lang.regex.NamedGroupPattern;

public class ScriptParserImplTest {

    @Test
    public void shouldParseLiteralText()
        throws Exception {

        String scriptFragment = "\"012345 test, here!!\"";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstLiteralTextValue actual = parser.parseWithStrategy(scriptFragment, LITERAL_TEXT_VALUE);

        AstLiteralTextValue expected = new AstLiteralTextValue("012345 test, here!!");

        assertEquals(expected, actual);
    }

    @Test // see http://jira.kaazing.wan/NR-12
    public void shouldParseComplexLiteralText()
        throws Exception {

        String scriptFragment =
                "\"GET / HTTP/1.1\\r\\nHost: localhost:8000\\r\\nUser-Agent: Mozilla/5.0 "
                + "(Macintosh; Intel Mac OS X 10.6; rv:8.0) Gecko/20100101 Firefox/8.0\\r\\nAccept: text/html,"
                + "application/xhtml+xml, application/xml;q=0.9,*/*;q=0.8\\r\\n\\r\\n\"";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstLiteralTextValue actual = parser.parseWithStrategy(scriptFragment, LITERAL_TEXT_VALUE);

        AstLiteralTextValue expected = new AstLiteralTextValue(
                "GET / HTTP/1.1\\r\\nHost: localhost:8000\\r\\nUser-Agent: Mozilla/5.0 "
                + "(Macintosh; Intel Mac OS X 10.6; rv:8.0) Gecko/20100101 Firefox/8.0\\r\\nAccept: text/html,"
                + "application/xhtml+xml, application/xml;q=0.9,*/*;q=0.8\\r\\n\\r\\n");

        assertEquals(expected, actual);
    }

    @Test // see http://jira.kaazing.wan/NR-12
    public void shouldParseComplexLiteralText2()
        throws Exception {

        String scriptFragment = "\"POST /index.html HTTP/1.1\\r\\nHost: localhost:8000\\r\\nUser-Agent: "
                + "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:8.0) Gecko/20100101 Firefox/8.0\\r\\n"
                + "Accept: text/html, application/xhtml+xml, application/xml;q=0.9,*/*;q=0.8\\r\\n"
                + "Content-Length: 43\\r\\n\\r\\nfirst_name=John&last_name=Doe&action=Submit\\r\\n\\r\\n\"";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstLiteralTextValue actual = parser.parseWithStrategy(scriptFragment, LITERAL_TEXT_VALUE);

        AstLiteralTextValue expected = new AstLiteralTextValue("POST /index.html HTTP/1.1\\r\\nHost: "
                + "localhost:8000\\r\\nUser-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:8.0) "
                + "Gecko/20100101 Firefox/8.0\\r\\nAccept: text/html, application/xhtml+xml, "
                + "application/xml;q=0.9,*/*;q=0.8\\r\\nContent-Length: 43\\r\\n\\r\\nfirst_name=John"
                + "&last_name=Doe&action=Submit\\r\\n\\r\\n");

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseLiteralBytesValue()
        throws Exception {

        String scriptFragment = "[0x01 0xff 0XFA]";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstLiteralBytesValue actual = parser.parseWithStrategy(scriptFragment, LITERAL_BYTES_VALUE);

        AstLiteralBytesValue expected = new AstLiteralBytesValue(new byte[] { 0x01, (byte) 0xff, (byte) 0xfa });

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseShortLiteral()
        throws Exception {

        String scriptFragment = "0x0005";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstExactBytesMatcher actual = parser.parseWithStrategy(scriptFragment, EXACT_BYTES_MATCHER);

        byte[] arr = { 0x00, 0x05 };

        AstExactBytesMatcher expected = new AstExactBytesMatcher(arr);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseShortNegativeLiteral()
        throws Exception {

        String scriptFragment = "0xFFFB";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstExactBytesMatcher actual = parser.parseWithStrategy(scriptFragment, EXACT_BYTES_MATCHER);

        byte[] arr = ByteBuffer.allocate(2).putShort((short) -5).array();

        AstExactBytesMatcher expected = new AstExactBytesMatcher(arr);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseNegativeByteLiteral()
        throws Exception {

        String scriptFragment = "0xFB";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstExactBytesMatcher actual = parser.parseWithStrategy(scriptFragment, EXACT_BYTES_MATCHER);

        byte[] arr = { (byte) -5 };

        AstExactBytesMatcher expected = new AstExactBytesMatcher(arr);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseByteLiteral()
        throws Exception {

        String scriptFragment = "0x05";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstExactBytesMatcher actual = parser.parseWithStrategy(scriptFragment, EXACT_BYTES_MATCHER);

        byte[] arr = { 0x05 };

        AstExactBytesMatcher expected = new AstExactBytesMatcher(arr);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseIntLiteral()
        throws Exception {

        String scriptFragment = "5";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstExactBytesMatcher actual = parser.parseWithStrategy(scriptFragment, EXACT_BYTES_MATCHER);

        byte[] arr = { 0x00, 0x00, 0x00, 0x05 };

        AstExactBytesMatcher expected = new AstExactBytesMatcher(arr);

        assertEquals(expected, actual);
    }


    @Test
    public void shouldParseNegativeIntLiteral()
        throws Exception {

        String scriptFragment = "-5";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstExactBytesMatcher actual = parser.parseWithStrategy(scriptFragment, EXACT_BYTES_MATCHER);

        byte[] arr = ByteBuffer.allocate(4).putInt(-5).array();

        AstExactBytesMatcher expected = new AstExactBytesMatcher(arr);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseLongLiteral()
        throws Exception {

        String scriptFragment = "5L";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstExactBytesMatcher actual = parser.parseWithStrategy(scriptFragment, EXACT_BYTES_MATCHER);

        byte[] arr = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x05 };

        AstExactBytesMatcher expected = new AstExactBytesMatcher(arr);

        assertEquals(expected, actual);
    }


    @Test
    public void shouldParseNegativeLongLiteral()
        throws Exception {

        String scriptFragment = "-5L";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstExactBytesMatcher actual = parser.parseWithStrategy(scriptFragment, EXACT_BYTES_MATCHER);

        byte[] arr = ByteBuffer.allocate(8).putLong(-5).array();

        AstExactBytesMatcher expected = new AstExactBytesMatcher(arr);

        assertEquals(expected, actual);
    }

    @Ignore("KG-7535 not complete")
    @Test
    public void shouldParseRegexMatcher()
        throws Exception {

        String scriptFragment = "/([^\\s]+) followed by ([^\\s]+) before the end\\n/";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstRegexMatcher actual = parser.parseWithStrategy(scriptFragment, REGEX_MATCHER);

        NamedGroupPattern regex = NamedGroupPattern.compile("/([^\\s]+) followed by ([^\\s]+) before the end\\n/");
        AstRegexMatcher expected = new AstRegexMatcher(regex);

        assertEquals(expected, actual);
    }

    @Ignore("KG-7535 not complete")
    @Test
    public void shouldParseCapturingRegexMatcher()
        throws Exception {

        String scriptFragment = "/([^\\s]+) followed by ([^\\s]+) before the end\\n/(:group1)(:group2)/";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstRegexMatcher actual = parser.parseWithStrategy(scriptFragment, REGEX_MATCHER);

        // note: capture name extraction tested in NamedGroupPattern unit tests
        NamedGroupPattern pattern =
                NamedGroupPattern.compile("/([^\\s]+) followed by ([^\\s]+) before the end\\n/(:group1)(:group2)/");
        AstRegexMatcher expected = new AstRegexMatcher(pattern);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseFixedLengthBytesMatcher()
        throws Exception {

        String scriptFragment = "[0..25]";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstFixedLengthBytesMatcher actual = parser.parseWithStrategy(scriptFragment, FIXED_LENGTH_BYTES_MATCHER);

        AstFixedLengthBytesMatcher expected = new AstFixedLengthBytesMatcher(25);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseVariableLengthBytesMatcher()
        throws Exception {

        String scriptFragment = "[0..${len+2}]";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstVariableLengthBytesMatcher actual = parser.parseWithStrategy(scriptFragment, VARIABLE_LENGTH_BYTES_MATCHER);

        ExpressionFactory factory = parser.getExpressionFactory();
        ExpressionContext context = parser.getExpressionContext();
        ValueExpression length = factory.createValueExpression(context, "${len+2}", Integer.class);
        AstVariableLengthBytesMatcher expected = new AstVariableLengthBytesMatcher(length);

        assertEquals(expected, actual);
    }

    @Ignore("not yet supported")
    @Test
    public void shouldParsePrefixedLengthBytesMatcher()
        throws Exception {

//        String scriptFragment = "[(...){2+}]";
//
//        ScriptParserImpl parser = new ScriptParserImpl();
//        AstPrefixedLengthBytesMatcher actual = parser.parseWithStrategy(scriptFragment, PREFIXED_LENGTH_BYTES_MATCHER);
//
//        AstPrefixedLengthBytesMatcher expected = new AstPrefixedLengthBytesMatcher(2);
//
//        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseExpressionMatcher()
        throws Exception {

        String scriptFragment = "${ byteArray }";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstExpressionMatcher actual = parser.parseWithStrategy(scriptFragment, EXPRESSION_MATCHER);

        ExpressionFactory factory = parser.getExpressionFactory();
        ExpressionContext context = parser.getExpressionContext();
        ValueExpression value = factory.createValueExpression(context, "${ byteArray }", byte[].class);
        AstExpressionMatcher expected = new AstExpressionMatcher(value);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseCapturingFixedLengthBytesMatcher()
        throws Exception {

        String scriptFragment = "([0..1]:capture)";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstFixedLengthBytesMatcher actual = parser.parseWithStrategy(scriptFragment, FIXED_LENGTH_BYTES_MATCHER);

        AstFixedLengthBytesMatcher expected = new AstFixedLengthBytesMatcher(1, "capture");

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseCapturingByteLengthMatcher() throws Exception {

        String scriptFragment = "(byte:capture)";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstFixedLengthBytesMatcher actual = parser.parseWithStrategy(scriptFragment, FIXED_LENGTH_BYTES_MATCHER);

        AstByteLengthBytesMatcher expected = new AstByteLengthBytesMatcher("capture");

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseCapturingShortLengthMatcher() throws Exception {

        String scriptFragment = "(short:capture)";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstFixedLengthBytesMatcher actual = parser.parseWithStrategy(scriptFragment, FIXED_LENGTH_BYTES_MATCHER);

        AstShortLengthBytesMatcher expected = new AstShortLengthBytesMatcher("capture");

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseCapturingIntLengthMatcher() throws Exception {

        String scriptFragment = "(int:capture)";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstFixedLengthBytesMatcher actual = parser.parseWithStrategy(scriptFragment, FIXED_LENGTH_BYTES_MATCHER);

        AstIntLengthBytesMatcher expected = new AstIntLengthBytesMatcher("capture");

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseCapturingLongLengthMatcher() throws Exception {

        String scriptFragment = "(long:capture)";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstFixedLengthBytesMatcher actual = parser.parseWithStrategy(scriptFragment, FIXED_LENGTH_BYTES_MATCHER);

        AstLongLengthBytesMatcher expected = new AstLongLengthBytesMatcher("capture");

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseCapturingVariableLengthBytesMatcher()
        throws Exception {

        String scriptFragment = "([0..${len-45}]:capture)";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstVariableLengthBytesMatcher actual = parser.parseWithStrategy(scriptFragment, VARIABLE_LENGTH_BYTES_MATCHER);

        ExpressionFactory factory = parser.getExpressionFactory();
        ExpressionContext context = parser.getExpressionContext();
        ValueExpression length = factory.createValueExpression(context, "${len-45}", Integer.class);
        AstVariableLengthBytesMatcher expected = new AstVariableLengthBytesMatcher(length, "capture");

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseExactTextWithQuote() throws Exception {
        String scriptFragment = "\"He\\\"llo\"";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstLiteralTextValue actual = parser.parseWithStrategy(scriptFragment, LITERAL_TEXT_VALUE);

        // TODO: Implement escaping special characters. We have no way of printing " as a literal. Have to use bytes
        AstLiteralTextValue expected = new AstLiteralTextValue("He\\\"llo");

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseMultiCapturingByteLengthMatcher() throws Exception {

        String scriptFragment = "(byte:capture) (byte:capture2)";

        ScriptParserImpl parser = new ScriptParserImpl();
        List<AstValueMatcher> actual = parser.parseWithStrategy(scriptFragment, MATCHER_LIST);

        List<AstValueMatcher> expected = new ArrayList<AstValueMatcher>();
        expected.add(new AstByteLengthBytesMatcher("capture"));
        expected.add(new AstByteLengthBytesMatcher("capture2"));

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseMultiCapturingShortLengthMatcher() throws Exception {

        String scriptFragment = "(short:capture) (short:capture2)";

        ScriptParserImpl parser = new ScriptParserImpl();
        List<AstValueMatcher> actual = parser.parseWithStrategy(scriptFragment, MATCHER_LIST);

        List<AstValueMatcher> expected = new ArrayList<AstValueMatcher>();
        expected.add(new AstShortLengthBytesMatcher("capture"));
        expected.add(new AstShortLengthBytesMatcher("capture2"));

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseMultiCapturingIntLengthMatcher() throws Exception {

        String scriptFragment = "(int:capture) (int:capture2)";

        ScriptParserImpl parser = new ScriptParserImpl();
        List<AstValueMatcher> actual = parser.parseWithStrategy(scriptFragment, MATCHER_LIST);

        List<AstValueMatcher> expected = new ArrayList<AstValueMatcher>();
        expected.add(new AstIntLengthBytesMatcher("capture"));
        expected.add(new AstIntLengthBytesMatcher("capture2"));

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseMultiCapturingLongLengthMatcher() throws Exception {

        String scriptFragment = "(long:capture) (long:capture2)";

        ScriptParserImpl parser = new ScriptParserImpl();
        List<AstValueMatcher> actual = parser.parseWithStrategy(scriptFragment, MATCHER_LIST);

        List<AstValueMatcher> expected = new ArrayList<AstValueMatcher>();
        expected.add(new AstLongLengthBytesMatcher("capture"));
        expected.add(new AstLongLengthBytesMatcher("capture2"));

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseMultiExactText() throws Exception {
        String scriptFragment = "\"Hello\" \"World\"";

        ScriptParserImpl parser = new ScriptParserImpl();
        List<AstValueMatcher> actual = parser.parseWithStrategy(scriptFragment, MATCHER_LIST);

        List<AstValueMatcher> expected = new ArrayList<AstValueMatcher>();
        expected.add(new AstExactTextMatcher("Hello"));
        expected.add(new AstExactTextMatcher("World"));

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseMultiExactBytes() throws Exception {

        String scriptFragment = "[0x01 0xff 0XFA] [0x00 0xF0 0x03 0x05 0x08 0x04]";

        ScriptParserImpl parser = new ScriptParserImpl();
        List<AstValueMatcher> actual = parser.parseWithStrategy(scriptFragment, MATCHER_LIST);

        List<AstValueMatcher> expected = new ArrayList<AstValueMatcher>();
        expected.add(new AstExactBytesMatcher(new byte[] { 0x01, (byte) 0xff, (byte) 0xfa }));
        // @formatter:off
        expected.add(new AstExactBytesMatcher(
                new byte[] { 0x00, (byte) 0xf0, (byte) 0x03, (byte) 0x05, (byte) 0x08, (byte) 0x04 }));
        // @formatter:on

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseMultiRegex() throws Exception {
        String scriptFragment = "/.*\\n/ /.+\\r/";

        ScriptParserImpl parser = new ScriptParserImpl();
        List<AstValueMatcher> actual = parser.parseWithStrategy(scriptFragment, MATCHER_LIST);

        List<AstValueMatcher> expected = new ArrayList<AstValueMatcher>();
        NamedGroupPattern regex1 = NamedGroupPattern.compile("/.*\\n/");
        NamedGroupPattern regex2 = NamedGroupPattern.compile("/.+\\r/");

        expected.add(new AstRegexMatcher(regex1));
        expected.add(new AstRegexMatcher(regex2));

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseMultiRegexWithCaptures() throws Exception {
        String scriptFragment = "/.*\\n/ /.+\\r/ /(.*\\n)/(:cap1)/ /(.+\\r)/(:cap2)/";

        ScriptParserImpl parser = new ScriptParserImpl();
        List<AstValueMatcher> actual = parser.parseWithStrategy(scriptFragment, MATCHER_LIST);

        List<AstValueMatcher> expected = new ArrayList<AstValueMatcher>();

        NamedGroupPattern regex1 = NamedGroupPattern.compile("/.*\\n/");
        NamedGroupPattern regex2 = NamedGroupPattern.compile("/.+\\r/");
        NamedGroupPattern regex3 = NamedGroupPattern.compile("/(.*\\n)/(:cap1)/");
        NamedGroupPattern regex4 = NamedGroupPattern.compile("/(.+\\r)/(:cap2)/");


        expected.add(new AstRegexMatcher(regex1));
        expected.add(new AstRegexMatcher(regex2));
        expected.add(new AstRegexMatcher(regex3));
        expected.add(new AstRegexMatcher(regex4));

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseMultExpression() throws Exception {
        String scriptFragment = "${var} ${var2}";

        ScriptParserImpl parser = new ScriptParserImpl();
        List<AstValueMatcher> actual = parser.parseWithStrategy(scriptFragment, MATCHER_LIST);

        List<AstValueMatcher> expected = new ArrayList<AstValueMatcher>();

        ExpressionFactory factory = parser.getExpressionFactory();
        ExpressionContext context = parser.getExpressionContext();
        ValueExpression value = factory.createValueExpression(context, "${var}", byte[].class);
        ValueExpression value2 = factory.createValueExpression(context, "${var2}", byte[].class);


        expected.add(new AstExpressionMatcher(value));
        expected.add(new AstExpressionMatcher(value2));

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseMultiFixedLengthBytes()
        throws Exception {
        String scriptFragment = "[0..1024] [0..4096]";

        ScriptParserImpl parser = new ScriptParserImpl();
        List<AstValueMatcher> actual = parser.parseWithStrategy(scriptFragment, MATCHER_LIST);

        List<AstValueMatcher> expected = new ArrayList<AstValueMatcher>();
        expected.add(new AstFixedLengthBytesMatcher(1024));
        expected.add(new AstFixedLengthBytesMatcher(4096));

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseMultiFixedLengthBytesWithCaptures() throws Exception {
        String scriptFragment = "[0..1024] ([0..64]:var1) [0..4096] ([0..64]:var2)";

        ScriptParserImpl parser = new ScriptParserImpl();
        List<AstValueMatcher> actual = parser.parseWithStrategy(scriptFragment, MATCHER_LIST);

        List<AstValueMatcher> expected = new ArrayList<AstValueMatcher>();
        expected.add(new AstFixedLengthBytesMatcher(1024));
        expected.add(new AstFixedLengthBytesMatcher(64, "var1"));
        expected.add(new AstFixedLengthBytesMatcher(4096));
        expected.add(new AstFixedLengthBytesMatcher(64, "var2"));

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseMultVariableLengthBytes() throws Exception {
        String scriptFragment = "[0..${len1}] [0..${len2}]";

        ScriptParserImpl parser = new ScriptParserImpl();
        List<AstValueMatcher> actual = parser.parseWithStrategy(scriptFragment, MATCHER_LIST);

        List<AstValueMatcher> expected = new ArrayList<AstValueMatcher>();
        ExpressionFactory factory = parser.getExpressionFactory();
        ExpressionContext context = parser.getExpressionContext();
        ValueExpression value = factory.createValueExpression(context, "${len1}", Integer.class);
        ValueExpression value2 = factory.createValueExpression(context, "${len2}", Integer.class);

        expected.add(new AstVariableLengthBytesMatcher(value));
        expected.add(new AstVariableLengthBytesMatcher(value2));

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseMultVariableLengthBytesWithCapture() throws Exception {
        String scriptFragment = "([0..${len1}]:var1) ([0..${len2}]:var2)";

        ScriptParserImpl parser = new ScriptParserImpl();
        List<AstValueMatcher> actual = parser.parseWithStrategy(scriptFragment, MATCHER_LIST);

        List<AstValueMatcher> expected = new ArrayList<AstValueMatcher>();
        ExpressionFactory factory = parser.getExpressionFactory();
        ExpressionContext context = parser.getExpressionContext();
        ValueExpression value = factory.createValueExpression(context, "${len1}", Integer.class);
        ValueExpression value2 = factory.createValueExpression(context, "${len2}", Integer.class);

        expected.add(new AstVariableLengthBytesMatcher(value, "var1"));
        expected.add(new AstVariableLengthBytesMatcher(value2, "var2"));

        assertEquals(expected, actual);
    }

    @Ignore("KG-7535 not complete")
    @Test
    public void shouldParseMultAllMatcher() throws Exception {
        String scriptFragment = "\"Hello\" [0x01 0x02 0x03] /.*\\n/ /(.*)\\n/(:cap1)/ ${var}  [0..64] ([0..64]:cap2)"
                        + "[0..${var}] [0..${var-1}] ([0..${var}]:cap3) ([0..${var-1}]:cap4)"
                        + "(byte:b) (short:s) (int:i) (long:l)";

        ScriptParserImpl parser = new ScriptParserImpl();
        List<AstValueMatcher> actual = parser.parseWithStrategy(scriptFragment, MATCHER_LIST);

        ExpressionFactory factory = parser.getExpressionFactory();
        ExpressionContext context = parser.getExpressionContext();

        List<AstValueMatcher> expected = new ArrayList<AstValueMatcher>();
        expected.add(new AstExactTextMatcher("Hello"));
        expected.add(new AstExactBytesMatcher(new byte[] { 0x01, (byte) 0x02, (byte) 0x03}));
        expected.add(new AstRegexMatcher(NamedGroupPattern.compile("/.*\\n/")));

        expected.add(new AstRegexMatcher(NamedGroupPattern.compile("/(.*)\\n/(:cap1)/")));

        ValueExpression value = factory.createValueExpression(context, "${var}", byte[].class);
        expected.add(new AstExpressionMatcher(value));

        expected.add(new AstFixedLengthBytesMatcher(64));
        expected.add(new AstFixedLengthBytesMatcher(64, "cap2"));

        ValueExpression value2 = factory.createValueExpression(context, "${var}", Integer.class);
        expected.add(new AstVariableLengthBytesMatcher(value2));

        ValueExpression value3 = factory.createValueExpression(context, "${var-1}", Integer.class);
        expected.add(new AstVariableLengthBytesMatcher(value3));

        ValueExpression value4 = factory.createValueExpression(context, "${var}", Integer.class);
        expected.add(new AstVariableLengthBytesMatcher(value4, "cap3"));

        ValueExpression value5 = factory.createValueExpression(context, "${var-1}", Integer.class);
        expected.add(new AstVariableLengthBytesMatcher(value5, "cap4"));

        expected.add(new AstByteLengthBytesMatcher("b"));
        expected.add(new AstShortLengthBytesMatcher("s"));
        expected.add(new AstIntLengthBytesMatcher("i"));
        expected.add(new AstLongLengthBytesMatcher("l"));

        assertEquals(expected, actual);
    }

    @Ignore("KG-7535 not complete")
    @Test
    public void shouldParseReadMult() throws Exception {
        String scriptFragment = "read \"Hello\" [0x01 0x02 0x03] /.*\\n/ /(.*)\\n/(:cap1)/ ${var}  [0..64] ([0..64]:cap2)"
                + "[0..${var}] [0..${var-1}] ([0..${var}]:cap3) ([0..${var-1}]:cap4) (byte:b) (short:s) (int:i) (long:l)";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        ExpressionFactory factory = parser.getExpressionFactory();
        ExpressionContext context = parser.getExpressionContext();

        // @formatter:off
        AstReadValueNode expected = new AstReadNodeBuilder()
            .setNextLineInfo(1, 0)
            .addExactText("Hello")
            .addExactBytes(new byte[] { 0x01, (byte) 0x02, (byte) 0x03})
            .addRegex(NamedGroupPattern.compile("/.*\\n/"))
            .addRegex(NamedGroupPattern.compile("/(.*)\\n/(:cap1)/"))
            .addExpression(factory.createValueExpression(context, "${var}", byte[].class))
            .addFixedLengthBytes(64)
            .addFixedLengthBytes(64, "cap2")
            .addVariableLengthBytes(factory.createValueExpression(context, "${var}", Integer.class))
            .addVariableLengthBytes(factory.createValueExpression(context, "${var-1}", Integer.class))
            .addVariableLengthBytes(factory.createValueExpression(context, "${var}", Integer.class), "cap3")
            .addVariableLengthBytes(factory.createValueExpression(context, "${var-1}", Integer.class), "cap4")
            .addFixedLengthBytes(1, "b")
            .addFixedLengthBytes(2, "s")
            .addFixedLengthBytes(4, "i")
            .addFixedLengthBytes(8, "l")
            .done();
        // @formatter:on

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseMultLiteralTextValue() throws Exception {
        String scriptFragment = "\"Hello\" \"World\"";

        ScriptParserImpl parser = new ScriptParserImpl();
        List<AstValue> actual = parser.parseWithStrategy(scriptFragment, WRITE_VALUE_LIST);

        List<AstValue> expected = new ArrayList<AstValue>();
        expected.add(new AstLiteralTextValue("Hello"));
        expected.add(new AstLiteralTextValue("World"));

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseMultLiteralBytesValue() throws Exception {
        String scriptFragment = "[0x01 0x02] [0x03 0x04]";

        ScriptParserImpl parser = new ScriptParserImpl();
        List<AstValue> actual = parser.parseWithStrategy(scriptFragment, WRITE_VALUE_LIST);

        List<AstValue> expected = new ArrayList<AstValue>();
        expected.add(new AstLiteralBytesValue(new byte[] { (byte) 0x01, (byte) 0x02 }));
        expected.add(new AstLiteralBytesValue(new byte[] { (byte) 0x03, (byte) 0x04 }));

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseMultExpressionValue() throws Exception {
        String scriptFragment = "${var1} ${var2}";

        ScriptParserImpl parser = new ScriptParserImpl();
        List<AstValue> actual = parser.parseWithStrategy(scriptFragment, WRITE_VALUE_LIST);

        ExpressionFactory factory = parser.getExpressionFactory();
        ExpressionContext context = parser.getExpressionContext();

        List<AstValue> expected = new ArrayList<AstValue>();
        expected.add(new AstExpressionValue(factory.createValueExpression(context, "${var1}", byte[].class)));
        expected.add(new AstExpressionValue(factory.createValueExpression(context, "${var2}", byte[].class)));

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseMultAllValue() throws Exception {
        String scriptFragment = "\"Hello\" [0x01 0x02] ${var1}";

        ScriptParserImpl parser = new ScriptParserImpl();
        List<AstValue> actual = parser.parseWithStrategy(scriptFragment, WRITE_VALUE_LIST);

        ExpressionFactory factory = parser.getExpressionFactory();
        ExpressionContext context = parser.getExpressionContext();

        List<AstValue> expected = new ArrayList<AstValue>();
        expected.add(new AstLiteralTextValue("Hello"));
        expected.add(new AstLiteralBytesValue(new byte[] { (byte) 0x01, (byte) 0x02 }));
        expected.add(new AstExpressionValue(factory.createValueExpression(context, "${var1}", byte[].class)));
        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseWriteMultAllValue() throws Exception {
        String scriptFragment = "write \"Hello\" [0x01 0x02] ${var1}";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteValueNode actual = parser.parseWithStrategy(scriptFragment, WRITE);

        ExpressionFactory factory = parser.getExpressionFactory();
        ExpressionContext context = parser.getExpressionContext();

        // @formatter:off
        AstWriteValueNode expected = new AstWriteNodeBuilder()
            .setNextLineInfo(1, 0)
            .addExactText("Hello")
            .addExactBytes(new byte[] { 0x01, (byte) 0x02})
            .addExpression(factory.createValueExpression(context, "${var1}", byte[].class))
            .done();
        // @formatter:on

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseAccept()
        throws Exception {

        String scriptFragment = "accept http://localhost:8001/echo";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstAcceptNode actual = parser.parseWithStrategy(scriptFragment, ACCEPT);

        AstAcceptNode expected = new AstAcceptNodeBuilder()
            .setNextLineInfo(1, 0)
            .setLocation(URI.create("http://localhost:8001/echo"))
            .done();

        assertEquals(expected, actual);
    }

    @Test(expected = ScriptParseException.class)
    public void shouldNotParseAcceptedWithoutBehavior()
        throws Exception {

        String script = "accepted";

        ScriptParserImpl parser = new ScriptParserImpl();
        parser.parseWithStrategy(script, SCRIPT);
    }

    @Test
    public void shouldParseClose()
        throws Exception {

        String scriptFragment = "close";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstCloseNode actual = parser.parseWithStrategy(scriptFragment, CLOSE);

        AstCloseNode expected = new AstCloseNodeBuilder()
            .setNextLineInfo(1, 0)
            .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseClosed()
        throws Exception {

        String scriptFragment = "closed";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstClosedNode actual = parser.parseWithStrategy(scriptFragment, CLOSED);

        AstClosedNode expected = new AstClosedNodeBuilder()
            .setNextLineInfo(1, 0)
            .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseConnected()
        throws Exception {

        String scriptFragment = "connected";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstConnectedNode actual = parser.parseWithStrategy(scriptFragment, CONNECTED);

        AstConnectedNode expected = new AstConnectedNodeBuilder()
            .setNextLineInfo(1, 0)
            .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseReadLiteralText()
        throws Exception {

        String scriptFragment = "read \"Hello\"";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadNodeBuilder()
            .setNextLineInfo(1, 0)
            .addExactText("Hello")
            .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseReadExactByte()
        throws Exception {

        String scriptFragment = "read 0x05";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadNodeBuilder()
            .setNextLineInfo(1, 0)
            .addExactBytes(new byte[] { 0x05 })
            .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseReadExactShort()
        throws Exception {

        String scriptFragment = "read 0x0005";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadNodeBuilder()
            .setNextLineInfo(1, 0)
            .addExactBytes(new byte[] { 0x00, 0x05 })
            .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseReadExactInt()
        throws Exception {

        String scriptFragment = "read 5";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadNodeBuilder()
            .setNextLineInfo(1, 0)
            .addExactBytes(new byte[] { 0x00, 0x00, 0x00, 0x05 })
            .done();

        assertEquals(expected, actual);
    }


    @Test
    public void shouldParseReadExactLong()
        throws Exception {

        String scriptFragment = "read 5L";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadNodeBuilder()
            .setNextLineInfo(1, 0)
            .addExactBytes(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x05 })
            .done();

        assertEquals(expected, actual);
    }


    @Test // see http://jira.kaazing.wan/browse/NR-12
    public void shouldParseReadLiteralTextWithMuchPunctuation()
        throws Exception {

        String scriptFragment = "read \"HTTP/1.1 404 Not Found\\r\\nServer: Kaazing Gateway\\r\\n"
                + "Date: Thu, 03 May 2012 20:41:24 GMT\\r\\n\\r\\nContent-Type: text/html\\r\\n"
                + "Content-length: 61 <html><head></head><body><h1>404 Not Found</h1></body></html>\"";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadNodeBuilder()
            .setNextLineInfo(1, 0)
            .addExactText("HTTP/1.1 404 Not Found\\r\\nServer: Kaazing Gateway\\r\\n"
                    + "Date: Thu, 03 May 2012 20:41:24 GMT\\r\\n\\r\\nContent-Type: text/html\\r\\n"
                    + "Content-length: 61 <html><head></head><body><h1>404 Not Found</h1></body></html>")
            .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseReadLiteralBytes()
        throws Exception {

        String scriptFragment = "read [0x01 0x02 0xFF]";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadNodeBuilder()
            .setNextLineInfo(1, 0)
            .addExactBytes(new byte[] {0x01, 0x02, (byte) 0xff})
            .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseReadExpression()
        throws Exception {

        String scriptFragment = "read ${hello}";

        ScriptParserImpl parser = new ScriptParserImpl();
        ExpressionFactory factory = parser.getExpressionFactory();
        ExpressionContext context = parser.getExpressionContext();

        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        AstReadValueNode expected = new AstReadNodeBuilder()
            .setNextLineInfo(1, 0)
            .addExpression(factory.createValueExpression(context, "${hello}", byte[].class))
            .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseReadRegexLiteral()
        throws Exception {

        String scriptFragment = "read /hello\\:^foo.*\\n/";

        ScriptParserImpl parser = new ScriptParserImpl();

        AstReadValueNode actual = parser.parseWithStrategy(scriptFragment, READ);

        // @formatter:off
        AstReadValueNode expected = new AstReadNodeBuilder()
            .setNextLineInfo(1, 0)
            .addRegex(NamedGroupPattern.compile("/hello\\:^foo.*\\n/"))
            .done();
        // @formatter:on

        assertEquals(expected, actual);
    }

    @Test // see http://jira.kaazing.wan/browse/NR-10
    public void shouldParseWriteLiteralTextWithSlash()
        throws Exception {

        String scriptFragment = "write \"GET /index.html blah\"";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteValueNode actual = parser.parseWithStrategy(scriptFragment, WRITE);

        AstWriteValueNode expected = new AstWriteNodeBuilder()
            .setNextLineInfo(1, 0)
            .addExactText("GET /index.html blah")
            .done();

        assertEquals(expected, actual);
    }

    @Test // see http://jira.kaazing.wan/browse/NR-10
    public void shouldParseWriteLiteralTextWithAsterisk()
        throws Exception {

        String scriptFragment = "write \"GET /index.html blah*\"";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteValueNode actual = parser.parseWithStrategy(scriptFragment, WRITE);

        AstWriteValueNode expected = new AstWriteNodeBuilder()
            .setNextLineInfo(1, 0)
            .addExactText("GET /index.html blah*")
            .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseWriteLiteralTextWithDollarSign()
        throws Exception {

        String scriptFragment = "write \"GET $foo\"";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteValueNode actual = parser.parseWithStrategy(scriptFragment, WRITE);

        AstWriteValueNode expected = new AstWriteNodeBuilder()
            .setNextLineInfo(1, 0)
            .addExactText("GET $foo")
            .done();

        assertEquals(expected, actual);
    }

    @Test // see http://jira.kaazing.wan/browse/NR-12
    public void shouldParseWriteLiteralTextWithMuchPunctuation()
        throws Exception {

        String scriptFragment = "write \"GET / HTTP/1.1\\r\\nHost: localhost:8000\\r\\n"
                + "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:8.0) Gecko/20100101 Firefox/8.0\\r\\n"
                + "Accept: text/html, application/xhtml+xml, application/xml;q=0.9,*/*;q=0.8\\r\\n\\r\\n\"";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteValueNode actual = parser.parseWithStrategy(scriptFragment, WRITE);

        AstWriteValueNode expected = new AstWriteNodeBuilder()
            .setNextLineInfo(1, 0)
            .addExactText("GET / HTTP/1.1\\r\\nHost: localhost:8000\\r\\n"
                    + "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:8.0) Gecko/20100101 Firefox/8.0\\r\\n"
                    + "Accept: text/html, application/xhtml+xml, application/xml;q=0.9,*/*;q=0.8\\r\\n\\r\\n")
            .done();

        assertEquals(expected, actual);
    }

    @Test // see http://jira.kaazing.wan/NR-34
    public void shouldParseWriteLiteralTextWithSingleQuote()
        throws Exception {

        String scriptFragment = "write \"DON'T WORK\"";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteValueNode actual = parser.parseWithStrategy(scriptFragment, WRITE);

        AstWriteValueNode expected = new AstWriteNodeBuilder()
            .setNextLineInfo(1, 0)
            .addExactText("DON'T WORK")
            .done();

        assertEquals(expected, actual);
    }

    @Test // see http://jira.kaazing.wan/NR-37
    public void shouldParseWriteLongLiteralText()
        throws Exception {

        StringBuilder longLiteralTextBuilder = new StringBuilder();
        longLiteralTextBuilder.append("POST /index.html HTTP/1.1\\r\\nHost: localhost:8000\\r\\n"
                + "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:8.0) Gecko/20100101 Firefox/8.0\\r\\n"
                + "Accept: text/html, application/xhtml+xml, application/xml;q=0.9,*/*;q=0.8\\r\\n"
                + "Content-Length: 99860\\r\\n\\r\\nfirst_name=Johnlast_nameDoeactionSubmitLoremipsumdolorsitametconsectetur");
        for (int i = 0; i < 3030; i++) {
            longLiteralTextBuilder.append("Loremipsumdolorsitametconsectetur");
        }
        String longLiteralText = longLiteralTextBuilder.toString();

        String scriptFragment = String.format("write \"%s\"", longLiteralText);

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteValueNode actual = parser.parseWithStrategy(scriptFragment, WRITE);

        AstWriteValueNode expected = new AstWriteNodeBuilder()
            .setNextLineInfo(1, 0)
            .addExactText(longLiteralText)
            .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseReadAwaitBarrier()
        throws Exception {

        String scriptFragment = "read await BARRIER";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadAwaitNode actual = parser.parseWithStrategy(scriptFragment, READ_AWAIT);

        AstReadAwaitNode expected = new AstReadAwaitNodeBuilder()
            .setNextLineInfo(1, 0)
            .setBarrierName("BARRIER")
            .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseReadNotifyBarrier()
        throws Exception {

        String scriptFragment = "read notify BARRIER";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstReadNotifyNode actual = parser.parseWithStrategy(scriptFragment, READ_NOTIFY);

        AstReadNotifyNode expected = new AstReadNotifyNodeBuilder()
            .setNextLineInfo(1, 0)
            .setBarrierName("BARRIER")
            .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseWriteAwaitBarrier()
        throws Exception {

        String scriptFragment = "write await BARRIER";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteAwaitNode actual = parser.parseWithStrategy(scriptFragment, WRITE_AWAIT);

        AstWriteAwaitNode expected = new AstWriteAwaitNodeBuilder()
            .setNextLineInfo(1, 0)
            .setBarrierName("BARRIER")
            .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseWriteNotifyBarrier()
        throws Exception {

        String scriptFragment = "write notify BARRIER";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstWriteNotifyNode actual = parser.parseWithStrategy(scriptFragment, WRITE_NOTIFY);

        AstWriteNotifyNode expected = new AstWriteNotifyNodeBuilder()
            .setNextLineInfo(1, 0)
            .setBarrierName("BARRIER")
            .done();

        assertEquals(expected, actual);
    }

    // @formatter:off
    @Test
    public void shouldParseConnectScript() throws Exception {

        String script =
            "# tcp.client.connect-then-close\n" +
            "connect tcp://localhost:7788\n" +
            "connected\n" +
            "close\n" +
            "closed\n";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstScriptNode actual = parser.parseWithStrategy(script, SCRIPT);

        AstScriptNode expected = new AstScriptNodeBuilder()
               .addConnectStream()
                .setNextLineInfo(2, 0)
                .setLocation(URI.create("tcp://localhost:7788"))
                .addConnectedEvent()
                .setNextLineInfo(1, 0)
                    .done()
                .addCloseCommand()
                    .setNextLineInfo(1, 0)
                    .done()
                .addClosedEvent()
                    .setNextLineInfo(1, 0)
                    .done()
                .done()
            .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseConnectScriptWithComments()
        throws Exception {

        String script = "# tcp.client.connect-then-close\n" +
                        "connect tcp://localhost:7788 # Comment 1\n" +
                        "\t\t # Comment 2\n" +
                        "connected\n" +
                        "close\n" +
                        "closed\n";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstScriptNode actual = parser.parseWithStrategy(script, SCRIPT);

        AstScriptNode expected = new AstScriptNodeBuilder()
            .addConnectStream()
                .setNextLineInfo(2, 0)
                  .setLocation(URI.create("tcp://localhost:7788"))
                .addConnectedEvent()
                      .setNextLineInfo(2, 0)
                    .done()
                   .addCloseCommand()
                    .setNextLineInfo(1, 0)
                    .done()
                .addClosedEvent()
                    .setNextLineInfo(1, 0)
                    .done()
                .done()
            .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseAcceptScript()
        throws Exception {

        String script =
            "# tcp.client.accept-then-close\n" +
            "accept tcp://localhost:7788\n" +
            "accepted\n" +
            "connected\n" +
            "close\n" +
            "closed\n";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstScriptNode actual = parser.parseWithStrategy(script, SCRIPT);

        AstScriptNode expected = new AstScriptNodeBuilder()
            .addAcceptStream()
                   .setNextLineInfo(2, 0)
                   .setLocation(URI.create("tcp://localhost:7788"))
                .done()
               .addAcceptedStream()
                .setNextLineInfo(1, 0)
                .addConnectedEvent()
                    .setNextLineInfo(1, 0)
                    .done()
                .addCloseCommand()
                       .setNextLineInfo(1, 0)
                    .done()
                .addClosedEvent()
                    .setNextLineInfo(1, 0)
                    .done()
                .done()
            .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseMultiConnectScript()
        throws Exception {

        String script =
            "# tcp.client.echo-multi-conn.upstream\n" +
            "connect tcp://localhost:8785\n" +
            "connected\n" +
            "write \"Hello, world!\"\n" +
            "write notify BARRIER\n" +
            "close\n" +
            "closed\n" +
            "# tcp.client.echo-multi-conn.downstream\n" +
            "connect tcp://localhost:8783\n" +
            "connected\n" +
            "read await BARRIER\n" +
            "read \"Hello, world!\"\n" +
            "close\n" +
            "closed\n";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstScriptNode actual = parser.parseWithStrategy(script, SCRIPT);

        AstScriptNode expected = new AstScriptNodeBuilder()
            .addConnectStream()
                .setNextLineInfo(2, 0)
                .setLocation(URI.create("tcp://localhost:8785"))
                .addConnectedEvent()
                    .setNextLineInfo(1, 0)
                    .done()
                .addWriteCommand()
                    .setNextLineInfo(1, 0)
                    .addExactText("Hello, world!")
                    .done()
                .addWriteNotifyBarrier()
                    .setNextLineInfo(1, 0)
                    .setBarrierName("BARRIER")
                    .done()
                .addCloseCommand()
                    .setNextLineInfo(1, 0)
                    .done()
                .addClosedEvent()
                    .setNextLineInfo(1, 0)
                    .done()
                .done()
            .addConnectStream()
                .setNextLineInfo(2, 0)
                .setLocation(URI.create("tcp://localhost:8783"))
                .addConnectedEvent()
                    .setNextLineInfo(1, 0)
                    .done()
                .addReadAwaitBarrier()
                    .setNextLineInfo(1, 0)
                    .setBarrierName("BARRIER")
                    .done()
                .addReadEvent()
                    .setNextLineInfo(1, 0)
                    .addExactText("Hello, world!")
                    .done()
                .addCloseCommand()
                    .setNextLineInfo(1, 0)
                    .done()
                .addClosedEvent()
                    .setNextLineInfo(1, 0)
                    .done()
                .done()
            .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseMultiAcceptScript()
        throws Exception {

        String script =
            "# tcp.server.echo-multi-conn.upstream\n" +
            "accept tcp://localhost:8783\n" +
            "accepted\n" +
            "connected\n" +
            "read await BARRIER\n" +
            "read \"Hello, world!\"\n" +
            "close\n" +
            "closed\n" +
            "# tcp.server.echo-multi-conn.downstream\n" +
            "accept tcp://localhost:8785\n" +
            "accepted\n" +
            "connected\n" +
            "write \"Hello, world!\"\n" +
            "write notify BARRIER\n" +
            "close\n" +
            "closed\n";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstScriptNode actual = parser.parseWithStrategy(script, SCRIPT);

        AstScriptNode expected = new AstScriptNodeBuilder()
            .addAcceptStream()
                .setNextLineInfo(2, 0)
                .setLocation(URI.create("tcp://localhost:8783"))
                .done()
                .addAcceptedStream()
                    .setNextLineInfo(1, 0)
                    .addConnectedEvent()
                    .setNextLineInfo(1, 0)
                    .done()
                .addReadAwaitBarrier()
                    .setNextLineInfo(1, 0)
                    .setBarrierName("BARRIER")
                    .done()
                .addReadEvent()
                    .setNextLineInfo(1, 0)
                    .addExactText("Hello, world!")
                    .done()
                .addCloseCommand()
                    .setNextLineInfo(1, 0)
                    .done()
                .addClosedEvent()
                    .setNextLineInfo(1, 0)
                    .done()
                .done()
            .addAcceptStream()
                .setNextLineInfo(2, 0)
                .setLocation(URI.create("tcp://localhost:8785"))
                .done()
            .addAcceptedStream()
                    .setNextLineInfo(1, 0)
                    .addConnectedEvent()
                    .setNextLineInfo(1, 0)
                    .done()
                .addWriteCommand()
                    .setNextLineInfo(1, 0)
                    .addExactText("Hello, world!")
                    .done()
                .addWriteNotifyBarrier()
                    .setNextLineInfo(1, 0)
                    .setBarrierName("BARRIER")
                    .done()
                .addCloseCommand()
                    .setNextLineInfo(1, 0)
                    .done()
                .addClosedEvent()
                    .setNextLineInfo(1, 0)
                    .done()
                .done()
            .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseAcceptAndConnectScript()
        throws Exception {

        String script =
            "# tcp.server.accept-then-close\n" +
            "accept tcp://localhost:7788\n" +
            "accepted\n" +
            "connected\n" +
            "closed\n" +
            "# tcp.client.connect-then-close\n" +
            "connect tcp://localhost:7788\n" +
            "connected\n" +
            "close\n" +
            "closed\n";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstScriptNode actual = parser.parseWithStrategy(script, SCRIPT);
        AstScriptNode expected;

        expected = new AstScriptNodeBuilder()
            .addAcceptStream()
                .setNextLineInfo(2, 0)
                .setLocation(URI.create("tcp://localhost:7788"))
                .done()
            .addAcceptedStream()
                .setNextLineInfo(1, 0)
                .addConnectedEvent()
                    .setNextLineInfo(1, 0)
                    .done()
                .addClosedEvent()
                    .setNextLineInfo(1, 0)
                    .done()
                .done()
            .addConnectStream()
                .setNextLineInfo(2, 0)
                .setLocation(URI.create("tcp://localhost:7788"))
                .addConnectedEvent()
                    .setNextLineInfo(1, 0)
                    .done()
                .addCloseCommand()
                    .setNextLineInfo(1, 0)
                    .done()
                .addClosedEvent()
                    .setNextLineInfo(1, 0)
                    .done()
                .done()
            .done();
        assertEquals(expected, actual);
    }


    @Test // see http://jira.kaazing.wan/NR-35
    public void shouldParseNonClosingConnectScript()
        throws Exception {

        String script =
            "# tcp.client.non-closing\n" +
            "connect tcp://localhost:7788\n" +
            "connected\n" +
            "read \"foo\"\n" +
            "write [0x01 0x02 0xff]\n" +
            "closed\n";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstScriptNode actual = parser.parseWithStrategy(script, SCRIPT);

        AstScriptNode expected = new AstScriptNodeBuilder()
            .addConnectStream()
                .setNextLineInfo(2, 0)
                .setLocation(URI.create("tcp://localhost:7788"))
                .addConnectedEvent()
                    .setNextLineInfo(1, 0)
                    .done()
                .addReadEvent()
                    .setNextLineInfo(1, 0)
                    .addExactText("foo")
                    .done()
                .addWriteCommand()
                    .setNextLineInfo(1, 0)
                    .addExactBytes(new byte[] { 0x01, 0x02, (byte) 0xff})
                    .done()
                .addClosedEvent()
                    .setNextLineInfo(1, 0)
                    .done()
                .done()
            .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseEmptyScript()
        throws Exception {

        String script = "";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstScriptNode actual = parser.parseWithStrategy(script, SCRIPT);

        AstScriptNode expected = new AstScriptNodeBuilder()
//            .setNextLineInfo(1, 0)
            .done();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseScriptWithCommentsOnly()
        throws Exception {

        String script = "# Comment 1\n" +
            "# Comment 2\n" +
            "# Comment 3\n";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstScriptNode actual = parser.parseWithStrategy(script, SCRIPT);

        AstScriptNode expected = new AstScriptNode();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseScriptWithCommentsAndWhitespace()
        throws Exception {

        String script = "# Comment 1\n" +
            "\t\n" +
            " # Comment 2\n" +
            "\r\n" +
            "# Comment 3\n";

        ScriptParserImpl parser = new ScriptParserImpl();
        AstScriptNode actual = parser.parseWithStrategy(script, SCRIPT);

        AstScriptNode expected = new AstScriptNode();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseScript()
        throws Exception {

        String script =
            "#\n" +
            "# server\n" +
            "#\n" +
            "accept tcp://localhost:8000 as ACCEPT\n" +
            "opened\n" +
            "bound\n" +
            "child opened\n" +
            "child closed\n" +
            "unbound\n" +
            "closed\n" +
            "#\n" +
            "# child\n" +
            "#\n" +
            " accepted ACCEPT\n" +
            "opened\n" +
            " bound\n" +
            "connected\n" +
            " read ([0..32]:input)\n" +
            "read notify BARRIER\n" +
            "write await BARRIER\n" +
            "write [ 0x01 0xfe ]\n" +
            "close\n" +
            "disconnected\n" +
            "unbound\n" +
            "closed\n" +
            "#\n" +
            "# client\n" +
            "#\n" +
            "connect tcp://localhost:8000\n" +
            " opened\n" +
            "bound\n" +
            " connected\n" +
            "write ${input}\n" +
            " read [ 0x00 0xff ]\n" +
            "close\n" +
            "disconnected\n" +
            "unbound\n" +
            "closed";

        ExpressionFactory factory = ExpressionFactory.newInstance();
        ExpressionContext context = new ExpressionContext();

        ScriptParserImpl parser = new ScriptParserImpl(factory, context);
        //parser.lex(new ByteArrayInputStream(script.getBytes(UTF_8)));
        AstScriptNode actual = parser.parseWithStrategy(script, SCRIPT);

        AstScriptNode expected = new AstScriptNodeBuilder()
            .addAcceptStream()
                .setNextLineInfo(4, 0)
                .setLocation(URI.create("tcp://localhost:8000"))
                .setAcceptName("ACCEPT")
                .addOpenedEvent()
                    .setNextLineInfo(1, 0)
                    .done()
                .addBoundEvent()
                    .setNextLineInfo(1, 0)
                    .done()
                .addChildOpenedEvent()
                    .setNextLineInfo(1, 0)
                    .done()
                .addChildClosedEvent()
                    .setNextLineInfo(1, 0)
                    .done()
                .addUnboundEvent()
                    .setNextLineInfo(1, 0)
                    .done()
                .addClosedEvent()
                    .setNextLineInfo(1, 0)
                    .done()
                .done()
            .addAcceptedStream()
                .setNextLineInfo(4, 1)
                .setAcceptName("ACCEPT")
                .addOpenedEvent()
                    .setNextLineInfo(1, 0)
                    .done()
                .addBoundEvent()
                    .setNextLineInfo(1, 1)
                    .done()
                .addConnectedEvent()
                    .setNextLineInfo(1, 0)
                    .done()
                .addReadEvent()
                    .setNextLineInfo(1, 1)
                    .addFixedLengthBytes(32, "input")
                    .done()
                .addReadNotifyBarrier()
                    .setNextLineInfo(1, 0)
                    .setBarrierName("BARRIER")
                    .done()
                .addWriteAwaitBarrier()
                    .setNextLineInfo(1, 0)
                    .setBarrierName("BARRIER")
                    .done()
                .addWriteCommand()
                    .setNextLineInfo(1, 0)
                    .addExactBytes(new byte[] { 0x01, -0x02 })
                    .done()
                .addCloseCommand()
                    .setNextLineInfo(1, 0)
                    .done()
                .addDisconnectedEvent()
                    .setNextLineInfo(1, 0)
                    .done()
                .addUnboundEvent()
                    .setNextLineInfo(1, 0)
                    .done()
                .addClosedEvent()
                    .setNextLineInfo(1, 0)
                    .done()
                .done()
            .addConnectStream()
                .setNextLineInfo(4, 0)
                .setLocation(URI.create("tcp://localhost:8000"))
                .addOpenedEvent()
                    .setNextLineInfo(1, 1)
                    .done()
                .addBoundEvent()
                    .setNextLineInfo(1, 0)
                    .done()
                .addConnectedEvent()
                    .setNextLineInfo(1, 1)
                    .done()
                .addWriteCommand()
                    .setNextLineInfo(1, 0)
                    .addExpression(factory.createValueExpression(context, "${input}", byte[].class))
                    .done()
                .addReadEvent()
                    .setNextLineInfo(1, 1)
                    .addExactBytes(new byte[] { 0x00, -0x01 })
                    .done()
                .addCloseCommand()
                    .setNextLineInfo(1, 0)
                    .done()
                .addDisconnectedEvent()
                    .setNextLineInfo(1, 0)
                    .done()
                .addUnboundEvent()
                    .setNextLineInfo(1, 0)
                    .done()
                .addClosedEvent()
                    .setNextLineInfo(1, 0)
                    .done()
                .done()
            .done();

        assertEquals(expected, actual);
    }

    @Test(expected = ScriptParseException.class)
    public void shouldNotParseScriptWithUnknownKeyword()
        throws Exception {

        String script = "written\n";

        ScriptParserImpl parser = new ScriptParserImpl();
        parser.parseWithStrategy(script, SCRIPT);
    }

    @Test(expected = ScriptParseException.class)
    public void shouldNotParseScriptWithReadBeforeConnect()
        throws Exception {

        String script =
            "# tcp.client.connect-then-close\n" +
            "read [0x01 0x02 0x03]\n" +
            "connect tcp://localhost:7788\n" +
            "connected\n" +
            "close\n" +
            "closed\n";

        ScriptParserImpl parser = new ScriptParserImpl();
        parser.parseWithStrategy(script, SCRIPT);
    }
 // @formatter:on
}

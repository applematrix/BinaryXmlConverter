package com.hdz;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Xml {
    public static boolean ENABLE_BINARY_DEFAULT = false;

    public static TypedXmlSerializer resolveSerializer(OutputStream out, boolean binary)
            throws IOException {
        final TypedXmlSerializer xml;
        if (binary) {
            xml = newBinarySerializer();
        } else {
            xml = newFastSerializer();
        }
        xml.setOutput(out, StandardCharsets.UTF_8.name());
        return xml;
    }

    public static TypedXmlSerializer newFastSerializer() {
        return XmlUtils.makeTyped(new FastXmlSerializer());
    }

    public static TypedXmlSerializer newBinarySerializer() {
        return new BinaryXmlSerializer();
    }

    public static TypedXmlPullParser resolvePullParser(BufferedInputStream in)
            throws IOException {
        final byte[] magic = new byte[4];

        in.mark(8);
        in.read(magic);
        in.reset();

        final TypedXmlPullParser xml;
        if (Arrays.equals(magic, BinaryXmlSerializer.PROTOCOL_MAGIC_VERSION_0)) {
            xml = newBinaryPullParser();
        } else {
            xml = newFastPullParser();
        }
        try {
            xml.setInput(in, StandardCharsets.UTF_8.name());
        } catch (XmlPullParserException e) {
            throw new IOException(e);
        }
        return xml;
    }

    public static TypedXmlPullParser newBinaryPullParser() {
        return new BinaryXmlPullParser();
    }

    /**
     * Creates a new {@link TypedXmlPullParser} which is optimized for use
     * inside the system, typically by supporting only a basic set of features.
     * <p>
     * In particular, the returned parser does not support namespaces, prefixes,
     * properties, or options.
     *
     * @hide
     */
    @SuppressWarnings("AndroidFrameworkEfficientXml")
    public static TypedXmlPullParser newFastPullParser() {
        return XmlUtils.makeTyped(newPullParser());
    }

    /**
     * Returns a new pull parser with namespace support.
     */
    public static XmlPullParser newPullParser() {
        try {
            XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_DOCDECL, true);
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
            return parser;
        } catch (XmlPullParserException e) {
            throw new AssertionError();
        }
    }
}

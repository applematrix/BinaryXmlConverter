package com.hdz;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;
import java.util.LinkedList;

public class Main {
    private static String usage = "java -jar -i inputFilePath -o outputFilePath [-b]";
    public static void usage(String error) {
        if (error != null) {
            System.out.println(error);
        }
        System.out.println(usage);
    }

    public static void main(String[] args) {
        String inputPath = null;
        String outputPath = null;
        boolean binaryOutput = false;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-i":
                case "-input":
                    i++;
                    if (i == args.length) {
                        usage("No input file specified");
                        return;
                    }
                    inputPath = args[i];
                    break;
                case "-o":
                case "-output":
                    i++;
                    if (i == args.length) {
                        usage("No output file specified");
                        return;
                    }
                    outputPath = args[i];
                    break;
                case "-b":
                    binaryOutput = true;
                    break;
            }
        }

        convert(inputPath, outputPath, binaryOutput);
        //convert("/home/lenovo/IdeaProjects/BinaryXmlConverter/src/com/hdz/packages_bin.xml",
        //        "/home/lenovo/IdeaProjects/BinaryXmlConverter/src/com/hdz/packages_generated.xml", false);
    }

    public static void convert(String inputPath, String outputPath, boolean binaryOutput) {
        if (inputPath == null || outputPath == null) {
            usage("Invalid command.");
            return;
        }
        System.out.println("covert " + inputPath + " to " + outputPath);

        try {
            BufferedInputStream input = new BufferedInputStream(new FileInputStream(inputPath));
            final FileOutputStream output = new FileOutputStream(outputPath);
            final TypedXmlSerializer serializer = Xml.resolveSerializer(output, binaryOutput);

            serializer.startDocument(null, true);
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

            XmlPullParser parser = Xml.resolvePullParser(input);
            int type;
            while ((type = parser.next()) != XmlPullParser.START_TAG
                    && type != XmlPullParser.END_DOCUMENT) {
                ;
            }

            if (type != XmlPullParser.START_TAG) {
                System.out.println("No start tag found in input file\n");
                return;
            }

            String root = parser.getName();
            serializer.startTag(null, root);
            int rootAttrs = parser.getAttributeCount();
            for (int i = 0; i < rootAttrs; i++) {
                String attrName = parser.getAttributeName(i);
                String attrValue = parser.getAttributeValue(i);
                serializer.attribute(null, attrName, attrValue);
            }

            int outerDepth = parser.getDepth();
            LinkedList<String> tags = new LinkedList<>();
            String curTag = null;
            while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                    && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
                if (type == XmlPullParser.END_TAG) {
                    if (curTag != null) {
                        serializer.endTag(null, curTag);
                    }
                    if (!tags.isEmpty()) {
                        curTag = tags.pop();
                    }
                }
                if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                    continue;
                }

                String name = parser.getName();
                int attributes = parser.getAttributeCount();

                if (type == XmlPullParser.START_TAG) {
                    serializer.startTag(null, name);
                    if (curTag != null) {
                        tags.push(curTag);
                    }
                    curTag = name;
                }

                for (int i = 0; i < attributes; i++) {
                    String attrName = parser.getAttributeName(i);
                    String attrValue = parser.getAttributeValue(i);
                    serializer.attribute(null, attrName, attrValue);
                }
            }
            serializer.endTag(null, root);
            serializer.endDocument();

            input.close();
            output.flush();
            output.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        System.out.println("xml converted complete");
    }
}

package hiromitsu.gclogviewer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class Main {

  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  private static Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
    File f = new File("verbosegc.001.log");

    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser parser = factory.newSAXParser();
    DefaultHandler handler = new GCLogHandler();

    try (InputStream is = new FileInputStream(f);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);) {

      StringBuilder sb = new StringBuilder();
      String line = null;
      while ((line = br.readLine()) != null) {
        if (line.isEmpty()) {
          if (sb.toString().contains("<?xml") || sb.toString().contains("<verbosegc")) {
            sb = new StringBuilder();
            continue;
          }
          String part = wrapByRoot(sb);
          ByteArrayInputStream bis = new ByteArrayInputStream(part.getBytes(StandardCharsets.US_ASCII));
          try {
            parser.parse(bis, handler);
            logger.debug(part);
          } catch (SAXParseException e) {
            logger.info(part);
            logger.error("parse failure: ", e);
          }
          sb = new StringBuilder();
        } else {
          sb.append(line).append(LINE_SEPARATOR);
        }
      }
    }
  }

  private static String wrapByRoot(StringBuilder sb) {
    return sb.insert(0, "<root>").append("</root>").toString();
  }
}

class GCLogHandler extends DefaultHandler {

  private Logger logger = LoggerFactory.getLogger(GCLogHandler.class);

  private boolean inGcEnd = false;
  private String timestamp;
  private String intervalms;
  private String type;
  private String durationms;
  private long used;
  private long total;

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) {
    if (qName.equals("exclusive-start")) {
      timestamp = attributes.getValue("timestamp");
      intervalms = attributes.getValue("intervalms");
    }

    if (qName.equals("gc-end")) {
      inGcEnd = true;
      type = attributes.getValue("type");
      durationms = attributes.getValue("durationms");
    }

    if (inGcEnd && qName.equals("mem-info")) {
      long free = Long.parseLong(attributes.getValue("free"));
      total = Long.parseLong(attributes.getValue("total"));
      used = total - free;
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) {
    if (qName.equals("gc-end")) {
      logger.info("{},{},{},{},{},{}", timestamp, type, used, total, durationms, intervalms);
      inGcEnd = false;
    }
  }
}

package hiromitsu.gclogviewer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * メイン・クラス
 */
public class Main {

  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {

    if (args == null || args.length != 1) {
      LOGGER.error("第1引数: パースするverbosegcログ");
      return;
    }

    File f = new File(args[0]);

    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser parser = factory.newSAXParser();
    parser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // Compliant
    parser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""); // compliant

    DefaultHandler handler = new GCLogHandler();

    try (InputStream is = new FileInputStream(f);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr)) {

      StringBuilder sb = new StringBuilder();
      String line;
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
            LOGGER.debug(part);
          } catch (SAXParseException e) {
            LOGGER.info(part);
            LOGGER.error("parse failure: ", e);
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

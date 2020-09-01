package hiromitsu.gclogviewer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * GCログをパースするためのハンドラー
 */
public class GCLogHandler extends DefaultHandler {

    private final Logger logger = LoggerFactory.getLogger(GCLogHandler.class);

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

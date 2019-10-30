package gov.usds.case_issues.config;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.LayoutBase;

public class JSONLayout extends LayoutBase<ILoggingEvent> {

  public String doLayout(ILoggingEvent event) {
    JSONObject log = new JSONObject();
    try {
      log.put("Timestamp", formatedEventTimestamp(event.getTimeStamp()));
      log.put("Level", event.getLevel());
      log.put("Thread Name", event.getThreadName());
      log.put("Logger Name", event.getLoggerName());
      log.put("Message", event.getFormattedMessage());
    } catch(JSONException e) {
      return "Unable to parse log event into JSON: " + e;
    }
    return log.toString() + System.lineSeparator();
  }

  private String formatedEventTimestamp(long eventTimestamp) {
    Date timestamp = new Date(eventTimestamp);
    DateFormat formater = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss.SSS a");
    return formater.format(timestamp);
  }
}

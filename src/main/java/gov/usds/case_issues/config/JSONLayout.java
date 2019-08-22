package gov.usds.case_issues.config;

import org.json.JSONObject;
import org.json.JSONException;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.LayoutBase;

public class JSONLayout extends LayoutBase<ILoggingEvent> {

  public String doLayout(ILoggingEvent event) {
    JSONObject log = new JSONObject();
    try {
      log.put("Timestamp", event.getTimeStamp() - event.getLoggerContextVO().getBirthTime());
      log.put("Level", event.getLevel());
      log.put("Thread Name", event.getThreadName());
      log.put("Logger Name", event.getLoggerName());
      log.put("Message", event.getFormattedMessage());
    } catch(JSONException e) {
      return "Unable to parse log event into JSON: " + e;
    }
    return log.toString() + System.lineSeparator();
  }
}

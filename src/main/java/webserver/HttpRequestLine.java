package webserver;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

public class HttpRequestLine {
    private static final Logger log = LoggerFactory.getLogger(HttpRequestLine.class);

    private String method;
    private String path;
    private Map<String, String> params = new HashMap<>();

    public HttpRequestLine(String line){
        log.debug("request line : {}", line);

        String[] tokens = line.split(" ");
        method = tokens[0];

        String source = tokens[1];
        if (method.equals("GET")){
            int index = source.indexOf("?");

            if (index == -1){
                path = source;
                return;
            }

            path = source.substring(0, index);
            params = HttpRequestUtils.parseQueryString(source.substring(index+1));
            return;
        }
        path = source;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getParams() {
        return params;
    }


    public boolean isHaveParams() {
        return !getParams().isEmpty();
    }
}

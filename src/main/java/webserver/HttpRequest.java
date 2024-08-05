package webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

public class HttpRequest {
    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);

    private String method;
    private String path;

    private Map<String, String> params = new HashMap<>();
    private Map<String, String> headers = new HashMap<>();
    public HttpRequest(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String line = reader.readLine();

        if (line==null){
            return;
        }

        parseRequestLine(line);

        line = reader.readLine();
        while(!line.equals("")){
            log.debug("header : {}", line);
            setHeader(line);
            line = reader.readLine();
        }

        if (method.equals("POST")){
            setRequestBody(reader);
        }
    }

    private void parseRequestLine(String line) {
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

    private void setHeader(String line){
        String[] tokens = line.split(":");

        String headerKey = tokens[0].trim();
        String headerValue = tokens[1].trim();

        headers.put(headerKey, headerValue);
    }

    private void setRequestBody(BufferedReader reader) throws IOException {
        int contentLength = Integer.parseInt(headers.get("Content-Length"));
        String requestBody = IOUtils.readData(reader, contentLength);
        params.putAll(HttpRequestUtils.parseQueryString(requestBody));
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getParameter(String name){
        return params.get(name);
    }

    public String getHeader(String name){
        return headers.get(name);
    }
}

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
    private HttpRequestLine requestLine;

    private Map<String, String> params = new HashMap<>();
    private Map<String, String> headers = new HashMap<>();
    public HttpRequest(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String line = reader.readLine();

        if (line==null){
            return;
        }
        requestLine = new HttpRequestLine(line);

        line = reader.readLine();
        while(!line.equals("")){
            log.debug("header : {}", line);
            setHeader(line);
            line = reader.readLine();
        }

        if (HttpMethod.isSame(getMethod(), HttpMethod.POST)){
            params.putAll(getRequestBody(reader));
            return;
        }

        if (requestLine.isHaveParams()){
            params.putAll(requestLine.getParams());
        }
    }

    private void setHeader(String line){
        String[] tokens = line.split(":");

        String headerKey = tokens[0].trim();
        String headerValue = tokens[1].trim();

        headers.put(headerKey, headerValue);
    }

    private Map<String, String> getRequestBody(BufferedReader reader) throws IOException {
        int contentLength = Integer.parseInt(headers.get("Content-Length"));
        String requestBody = IOUtils.readData(reader, contentLength);
        return HttpRequestUtils.parseQueryString(requestBody);
    }

    public String getMethod() {
        return requestLine.getMethod();
    }

    public String getPath() {
        return requestLine.getPath();
    }

    public String getParameter(String name){
        return params.get(name);
    }

    public String getHeader(String name){
        return headers.get(name);
    }
}

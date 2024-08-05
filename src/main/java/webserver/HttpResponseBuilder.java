package webserver;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HttpResponseBuilder {
    private static final String HTTP_VERSION = "HTTP/1.1 ";
    private final DataOutputStream dos;
    private final Map<String, String> headers;
    private byte[] body;

    private int bodyLength;
    private int statusCode;
    private String statusMessage;

    public HttpResponseBuilder(DataOutputStream dos) {
        this.dos = dos;
        this.headers = new HashMap<>();
    }

    public HttpResponseBuilder status(int code) {
        HttpStatus httpStatus = HttpStatus.find(code);
        this.statusCode = code;
        this.statusMessage = httpStatus.getStatusMessage();
        return this;
    }

    public HttpResponseBuilder header(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public HttpResponseBuilder body(byte[] body) {
        this.body = body;
        this.bodyLength = body.length;
        return this;
    }

    public void build() throws IOException {
        dos.writeBytes(HTTP_VERSION + statusCode + " " + statusMessage + " \r\n");

        for (Map.Entry<String, String> header : headers.entrySet()) {
            dos.writeBytes(header.getKey() + ": " + header.getValue() + "\r\n");
        }

        dos.writeBytes("\r\n");

        if (body != null) {
            dos.write(body, 0, bodyLength);
        }
        dos.flush();
    }
}

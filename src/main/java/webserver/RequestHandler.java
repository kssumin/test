package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import java.nio.file.Files;
import java.util.Map;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = reader.readLine();
            int contentLength=0;

            if (line==null){
                return;
            }

            log.debug("request line : {}", line);

            String[] tokens = line.split(" ");
            String requestUrl = tokens[1];

            while(!line.equals("")){
                line = reader.readLine();

                if (line.startsWith("Content-Length")){
                    contentLength = getContentLength(line);
                }
                log.debug("header : {}", line);
            }

            if (requestUrl.startsWith("/user/create")){
                String requestBody = IOUtils.readData(reader, contentLength);
                Map<String, String> query = HttpRequestUtils.parseQueryString(requestBody);
            User user = new User(query.get("userId"), query.get("password"), query.get("name"),
                    query.get("email"));

            log.debug("body : {}", requestBody);
            log.debug("user : {}", user);
            }else{
                DataOutputStream dos = new DataOutputStream(out);
                byte[] body = Files.readAllBytes(new File("./webapp"+requestUrl).toPath());

                response200Header(dos, body.length);
                responseBody(dos, body);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private int getContentLength(String line){
        int index = line.indexOf(":");
        return Integer.parseInt(line.substring(index + 1).trim());
    }
}

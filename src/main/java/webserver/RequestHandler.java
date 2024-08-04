package webserver;

import db.DataBase;
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
            DataOutputStream dos = new DataOutputStream(out);

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
                Map<String, String> body = getRequestBody(reader, contentLength);
            User user = new User(body.get("userId"), body.get("password"), body.get("name"),
                    body.get("email"));
            DataBase.addUser(user);

            log.debug("user : {}", user);

            response302Header(dos,"/index.html");
            }

            if (requestUrl.startsWith("/user/login")){
                Map<String, String> body = getRequestBody(reader, contentLength);

                String userId = body.get("userId");
                User user = DataBase.findUserById(userId);

                if (user == null){
                    log.info("user not found");
                    responseResource(out, "/user/login_failed.html");
                    return;
                }

                if(validatePassword(user.getPassword(), body.get("password"))){
                    log.info("success login");
                    responseSuccessLoginHeader(dos);
                    return;
                }
                responseResource(out, "/user/login_failed.html");
            }
            else{
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

    private void response302Header(DataOutputStream dos, String location) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: "+location+"\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseSuccessLoginHeader(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: /index.html \r\n");
            dos.writeBytes("Set-Cookie : logined=true \r\n");
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

    private Map<String, String> getRequestBody(BufferedReader reader, int contentLength) throws IOException {
        String requestBody = IOUtils.readData(reader, contentLength);
        return HttpRequestUtils.parseQueryString(requestBody);
    }

    private void responseResource(OutputStream out, String url) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());

        response200Header(dos, body.length);
        responseBody(dos, body);
    }

    private boolean validatePassword(String answer, String compare){
       return answer.equals(compare);
    }
}

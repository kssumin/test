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
            HttpRequest request = new HttpRequest(in);
            DataOutputStream dos = new DataOutputStream(out);

            boolean isLogin = isLogin(request);
            String requestUrl = request.getPath();

            if (requestUrl.endsWith(".css")){
                byte[] body = Files.readAllBytes(new File("./webapp" + requestUrl).toPath());
                new HttpResponseBuilder(dos)
                        .status(200)
                        .header("Content-Type", "text/css")
                        .body(body)
                        .build();
            }

            if (requestUrl.startsWith("/user/create")){
            User user = new User(
                    request.getParameter("userId"),
                    request.getParameter("password"),
                    request.getParameter("name"),
                    request.getParameter("email"));
            DataBase.addUser(user);

            log.debug("user : {}", user);

            new HttpResponseBuilder(dos)
                        .status(302)
                        .header("Location", "/index.html")
                        .build();
            }

            if (requestUrl.startsWith("/user/login")){
                validateUser(
                        request.getParameter("userId"),
                        request.getParameter("password"),
                        dos);

                new HttpResponseBuilder(dos)
                        .status(302)
                        .header("Location", "/index.html")
                        .header("Set-Cookie", "logined=true")
                        .build();
            }

            if (requestUrl.startsWith("/user/list")){
                if (isLogin){
                    StringBuilder sb = new StringBuilder();
                    sb.append("<table border='1>");
                    DataBase.findAll()
                            .forEach(user -> sb.append("<tr><td>"+user.getUserId()+"</td><td>"+user.getName()+"</td><td>"+user.getEmail()+"</td></tr>"));

                    sb.append("</table>");

                    new HttpResponseBuilder(dos)
                            .status(200)
                            .body(sb.toString().getBytes())
                            .build();

                    return;
                }
                responseResource(out, "/login.html");
            }
            else{
                byte[] body = Files.readAllBytes(new File("./webapp"+requestUrl).toPath());

                new HttpResponseBuilder(dos)
                        .status(200)
                        .header("Content-Type", "text/html;charset=UTF-8")
                        .header("Content-Length", String.valueOf(body.length))
                        .body(body)
                        .build();
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseResource(OutputStream out, String url) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());

        new HttpResponseBuilder(dos)
                .status(200)
                .header("Content-Type", "text/html;charset=UTF-8")
                .header("Content-Length", String.valueOf(body.length))
                .body(body)
                .build();
    }

    private void validateUser(String compareUserId, String comparePassword, DataOutputStream out) throws IOException {
        User user = DataBase.findUserById(compareUserId);

        if (user == null){
            log.info("user not found");
            responseResource(out, "/user/login_failed.html");
            return;
        }

        if(isSame(user.getPassword(), comparePassword)){
            return;
        }

        responseResource(out, "/user/login_failed.html");
    }

    private boolean isSame(String answer, String compare){
       return answer.equals(compare);
    }

    private boolean isLogin(HttpRequest httpRequest){
        String value = httpRequest.getCookie("logined");
        return value != null;
    }
}
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
            HttpResponse response = new HttpResponse(out);
            DataOutputStream dos = new DataOutputStream(out);

            boolean isLogin = isLogin(request);
            String requestUrl = request.getPath();

            if (requestUrl.endsWith(".css")) {
                response.forwardCss(requestUrl);
            }

            if (requestUrl.startsWith("/user/create")) {
                User user = new User(
                        request.getParameter("userId"),
                        request.getParameter("password"),
                        request.getParameter("name"),
                        request.getParameter("email"));
                DataBase.addUser(user);

                log.debug("user : {}", user);

                response.sendRedirect("/index.html");
            }

            if (requestUrl.startsWith("/user/login")) {
                validateUser(
                        request.getParameter("userId"),
                        request.getParameter("password"),
                        response);

                response.sendRedirect("/index.html");
            }

            if (requestUrl.startsWith("/user/list")) {
                if (isLogin) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("<table border='1>");
                    DataBase.findAll()
                            .forEach(user -> sb.append(
                                    "<tr><td>" + user.getUserId() + "</td><td>" + user.getName() + "</td><td>"
                                            + user.getEmail() + "</td></tr>"));

                    sb.append("</table>");

                    response.forwardBody(sb.toString().getBytes());
                    return;
                }
                response.forward("/login.html");
            } else {
                response.forward(requestUrl);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void validateUser(String compareUserId, String comparePassword, HttpResponse response) throws IOException {
        User user = DataBase.findUserById(compareUserId);

        if (user == null){
            log.info("user not found");
            response.forward("/user/login_failed.html");
            return;
        }
        if(isSame(user.getPassword(), comparePassword)){
            return;
        }
        response.forward("/user/login_failed.html");
    }

    private boolean isSame(String answer, String compare){
       return answer.equals(compare);
    }

    private boolean isLogin(HttpRequest httpRequest){
        String value = httpRequest.getCookie("logined");
        return value != null;
    }
}
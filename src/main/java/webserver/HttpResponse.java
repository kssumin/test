package webserver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpResponse {
    private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);
    private final DataOutputStream dos;

    public HttpResponse(OutputStream out) {
        this.dos = new DataOutputStream(out);
    }

    public void forward(String url){
        try {
            byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());

            new HttpResponseBuilder(dos)
                    .status(200)
                    .header("Content-Type", "text/html;charset=UTF-8")
                    .header("Content-Length", String.valueOf(body.length))
                    .body(body)
                    .build();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void forwardCss(String url){
        try {
            byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
            new HttpResponseBuilder(dos)
                    .status(200)
                    .header("Content-Type", "text/css")
                    .body(body)
                    .build();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void forwardBody(byte[] body){
        try {
            new HttpResponseBuilder(dos)
                    .status(200)
                    .body(body)
                    .build();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void sendRedirect(String redirectUrl){
         try{
             new HttpResponseBuilder(dos)
                     .status(302)
                     .header("Location", "/index.html")
                     .build();
         }catch (IOException e){
             log.error(e.getMessage());
         }
    }
}

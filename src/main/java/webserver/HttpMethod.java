package webserver;

import java.util.Arrays;

public enum HttpMethod {
    GET("GET"),
    POST("POST"),
    ;

    private final String method;

    HttpMethod(String method) {
        this.method = method;
    }

    public static boolean isSame(String method, HttpMethod httpMethod){
        return httpMethod.equals(find(method));
    }

    private String getMethod() {
        return method;
    }

    private static HttpMethod find(String method){
        return Arrays.stream(HttpMethod.values())
                .filter(httpMethod -> httpMethod.getMethod().equals(method))
                .findAny()
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 http method"));
    }
}

package webserver;

import java.util.Arrays;

public enum HttpStatus {
    OK(200, "200 OK"),
    FOUND(302, "302 Found");

    private final int statusCode;
    private final String statusMessage;

    HttpStatus(int statusCode, String statusMessage) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public static HttpStatus find(int statusCode){
        return Arrays.stream(HttpStatus.values())
                .filter(code -> code.getStatusCode() == statusCode)
                .findAny()
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 상태 코드"));
    }
}

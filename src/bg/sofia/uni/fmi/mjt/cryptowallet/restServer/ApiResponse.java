package bg.sofia.uni.fmi.mjt.cryptowallet.restServer;

public class ApiResponse<T> {
    private int status;
    private String msg;
    private T data;

    public ApiResponse(T data, int statusCode, String message) {
        this.data = data;
        status = statusCode;
        msg = message;
    }

    public T getData() {
        return data;
    }

    public int getStatusCode() {
        return status;
    }

    public String getMessage() {
        return msg;
    }

}

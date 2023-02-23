package bg.sofia.uni.fmi.mjt.cryptowallet.restServer;

public class ResponseError {
    private String error;

    public ResponseError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return error;
    }

    public void setMessage(String error) {
        this.error = error;
    }
}

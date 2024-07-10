package travelwith.com.demo.image;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomResponse<T> {
    private int code;
    private HttpStatus status;
    private String message;
    private T data; // = body

    public CustomResponse(HttpStatus status, String message) {
        this(status, message, null);
    }

    public CustomResponse(HttpStatus status, String message, T data) {
        this.code = status.value();
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> CustomResponse<T> of(HttpStatus status, String message) {
        return new CustomResponse<>(status, message);
    }

    public static <T> CustomResponse<T> of(HttpStatus status, String message, T data) {
        return new CustomResponse<>(status, message, data);
    }

    public static <T> CustomResponse<T> ok(String message, T data) {
        return CustomResponse.of(HttpStatus.OK, message, data);
    }

    public static CustomResponse ok(String message) {
        return CustomResponse.of(HttpStatus.OK, message);
    }

    public static <T> CustomResponse<T> success(String message, T data) {
        return CustomResponse.of(HttpStatus.OK, message, data);
    }

    public static <T> CustomResponse<T> failure(String message) {
        return CustomResponse.of(HttpStatus.BAD_REQUEST, message);
    }

    public static <T> CustomResponse<T> error(String message) {
        return CustomResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    public static <T> CustomResponse<T> from(T data) {
        return CustomResponse.of(HttpStatus.OK, "Operation successful", data);
    }
}
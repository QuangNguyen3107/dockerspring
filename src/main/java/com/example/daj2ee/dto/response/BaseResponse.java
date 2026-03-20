package com.example.daj2ee.dto.response;

import com.example.daj2ee.util.enums.BaseResponseMessage;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.net.URI;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BaseResponse<T>(
  int status,
  String message,
  T data,
  Instant timestamp
) implements Serializable {
  private static final long serialVersionUID = 1L;

  public BaseResponse(int status, String message, T data) {
    this(status, message, data, Instant.now());
  }

  public BaseResponse {
    if (timestamp == null) {
      timestamp = Instant.now();
    }
  }

  public static <T> BaseResponse<T> build(int status, String message, T data) {
    return new BaseResponse<>(status, message, data);
  }

  public static <T> BaseResponse<T> ok() {
    return build(200, BaseResponseMessage.OK.getMessage(), null);
  }

  public static <T> BaseResponse<T> ok(T data) {
    return build(200, BaseResponseMessage.OK.getMessage(), data);
  }

  public static <T> BaseResponse<T> ok(String message, T data) {
    return build(200, message, data);
  }

  public static <T> BaseResponse<T> created(T data) {
    return build(201, BaseResponseMessage.CREATE.getMessage(), data);
  }

  public static <T> BaseResponse<T> accepted(T data) {
    return build(202, "Accepted", data);
  }

  public static <T> BaseResponse<T> badRequest(String message) {
    return build(400, message, null);
  }

  public static <T> BaseResponse<T> unauthorized(String message) {
    return build(401, message, null);
  }

  public static <T> BaseResponse<T> forbidden(String message) {
    return build(403, message, null);
  }

  public static <T> BaseResponse<T> notFound(String message) {
    return build(404, message, null);
  }

  public static <T> BaseResponse<T> conflict(String message) {
    return build(409, message, null);
  }

  public static <T> BaseResponse<T> serverError(String message) {
    return build(500, message, null);
  }

  public boolean isSuccess() {
    return this.status >= 200 && this.status < 300;
  }

  public ResponseEntity<BaseResponse<T>> toResponseEntity() {
    HttpStatus httpStatus;
    try {
      httpStatus = HttpStatus.valueOf(this.status);
    } catch (IllegalArgumentException ex) {
      httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    }
    return new ResponseEntity<>(this, httpStatus);
  }

  public static <T> ResponseEntity<BaseResponse<T>> okEntity() {
    return ResponseEntity.ok(BaseResponse.ok());
  }

  public static <T> BaseResponse<T> ok(T data, String message) {
    return build(200, message, data);
  }

  public static <T> ResponseEntity<BaseResponse<T>> okEntity(T data, String message) {
    return ResponseEntity.ok(BaseResponse.ok(data, message));
  }

  public static <T> ResponseEntity<BaseResponse<T>> okEntity(T data) {
    return ResponseEntity.ok(BaseResponse.ok(data));
  }

  public static <T> ResponseEntity<BaseResponse<T>> createdEntity(T data) {
    return ResponseEntity.status(HttpStatus.CREATED).body(
      BaseResponse.created(data)
    );
  }

  public static <T> ResponseEntity<BaseResponse<T>> createdEntity(
    T data,
    URI location
  ) {
    return ResponseEntity.created(location).body(BaseResponse.created(data));
  }

  public static <T> ResponseEntity<BaseResponse<T>> badRequestEntity(
    String message
  ) {
    return ResponseEntity.badRequest().body(BaseResponse.badRequest(message));
  }

  public static <T> ResponseEntity<BaseResponse<T>> unauthorizedEntity(
    String message
  ) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
      BaseResponse.unauthorized(message)
    );
  }

  public static <T> ResponseEntity<BaseResponse<T>> forbiddenEntity(
    String message
  ) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
      BaseResponse.forbidden(message)
    );
  }

  public static <T> ResponseEntity<BaseResponse<T>> notFoundEntity(
    String message
  ) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
      BaseResponse.notFound(message)
    );
  }

  public static <T> ResponseEntity<BaseResponse<T>> conflictEntity(
    String message
  ) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(
      BaseResponse.conflict(message)
    );
  }

  public static <T> ResponseEntity<BaseResponse<T>> serverErrorEntity(
    String message
  ) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
      BaseResponse.serverError(message)
    );
  }
}

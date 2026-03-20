package com.example.daj2ee.util.enums;

public enum BaseResponseMessage {
  CREATE("CREATED"),
  OK("OK");

  private final String message;

  BaseResponseMessage(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}

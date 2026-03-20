package com.example.daj2ee.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TestCaseResult(
  int index,
  boolean passed,
  boolean hidden,
  String stdout,
  String stderr,
  String compileOutput,
  String expectedOutput,
  Integer statusId,
  String statusDescription,
  String time,
  Long memory
) {
  public TestCaseResult {
    if (hidden) {
      stdout = null;
      expectedOutput = null;
    }
  }

  @Override
  public String toString() {
    return (
      "TestCaseResult{" +
      "index=" +
      index +
      ", passed=" +
      passed +
      ", hidden=" +
      hidden +
      ", statusId=" +
      statusId +
      ", statusDescription='" +
      statusDescription +
      '\'' +
      ", time='" +
      time +
      '\'' +
      '}'
    );
  }
}

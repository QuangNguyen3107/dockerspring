package com.example.daj2ee.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SubmissionRequest(
  @NotNull(message = "languageId is required") Integer languageId,

  Long problemId,

  @NotBlank(message = "sourceCode is required")
  @Size(max = 200_000, message = "sourceCode is too large")
  String sourceCode,

  @Size(max = 20_000, message = "stdin is too large") String stdin,

  @Size(max = 20_000, message = "expectedOutput is too large")
  String expectedOutput,

  Boolean base64Encoded,

  Integer numberOfRuns,

  Integer cpuTimeLimit,

  Double cpuExtraTime,

  Integer wallTimeLimit,

  Integer memoryLimit,

  Integer maxFileSize,

  Boolean waitForResult
) {
  public SubmissionRequest {
    if (base64Encoded == null) {
      base64Encoded = false;
    }
    if (waitForResult == null) {
      waitForResult = false;
    }
  }

  @Override
  public String toString() {
    return (
      "SubmissionRequest{" +
      "languageId=" +
      languageId +
      ", problemId=" +
      problemId +
      ", stdin=" +
      (stdin != null ? "[provided]" : "null") +
      ", expectedOutput=" +
      (expectedOutput != null ? "[provided]" : "null") +
      ", base64Encoded=" +
      base64Encoded +
      ", numberOfRuns=" +
      numberOfRuns +
      ", cpuTimeLimit=" +
      cpuTimeLimit +
      ", cpuExtraTime=" +
      cpuExtraTime +
      ", wallTimeLimit=" +
      wallTimeLimit +
      ", memoryLimit=" +
      memoryLimit +
      ", maxFileSize=" +
      maxFileSize +
      ", waitForResult=" +
      waitForResult +
      '}'
    );
  }
}

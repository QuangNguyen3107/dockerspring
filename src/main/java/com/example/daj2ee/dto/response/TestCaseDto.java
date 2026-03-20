package com.example.daj2ee.dto.response;

import com.example.daj2ee.entity.TestCase;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TestCaseDto(
  Long id,
  String input,
  String expectedOutput,
  boolean hidden,
  Integer sortOrder
) {
  public static TestCaseDto fromEntity(TestCase tc) {
    return new TestCaseDto(
      tc.getId(),
      tc.isHidden() ? null : tc.getInput(),
      tc.isHidden() ? null : tc.getExpectedOutput(),
      tc.isHidden(),
      tc.getSortOrder()
    );
  }
}

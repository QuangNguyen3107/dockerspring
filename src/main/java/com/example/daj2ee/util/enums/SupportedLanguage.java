package com.example.daj2ee.util.enums;

/**
 * Enum of programming languages supported by this application.
 *
 * Each constant maps to a Judge0 language ID and carries metadata useful
 * for building boilerplate code, displaying language names in the UI, and
 * validating incoming submission requests.
 *
 * Judge0 CE language IDs (subset used here):
 *   https://ce.judge0.com/languages
 *
 * Supported:
 *   JAVA       – Java (OpenJDK 13.0.1),  id=62
 *   PYTHON     – Python (3.8.1),          id=71
 *   JAVASCRIPT – JavaScript (Node.js),    id=63
 *   C          – C (GCC 9.2.0),           id=50
 *   CPP        – C++ (GCC 9.2.0),         id=54
 */
public enum SupportedLanguage {

  JAVA(62, "Java (OpenJDK 13.0.1)", "java"),
  PYTHON(71, "Python (3.8.1)", "py"),
  JAVASCRIPT(63, "JavaScript (Node.js 12.14.0)", "js"),
  C(50, "C (GCC 9.2.0)", "c"),
  CPP(54, "C++ (GCC 9.2.0)", "cpp");

  private final int judgeId;
  private final String displayName;
  private final String fileExtension;

  SupportedLanguage(int judgeId, String displayName, String fileExtension) {
    this.judgeId = judgeId;
    this.displayName = displayName;
    this.fileExtension = fileExtension;
  }

  /** The Judge0 language ID used in submission requests. */
  public int getJudgeId() {
    return judgeId;
  }

  /** Human-readable language name for display in the UI. */
  public String getDisplayName() {
    return displayName;
  }

  /** File extension associated with this language (without the leading dot). */
  public String getFileExtension() {
    return fileExtension;
  }

  /**
   * Look up a {@link SupportedLanguage} by its Judge0 language ID.
   *
   * @param judgeId the Judge0 integer language ID
   * @return the matching enum constant, or {@code null} if not supported
   */
  public static SupportedLanguage fromJudgeId(int judgeId) {
    for (SupportedLanguage lang : values()) {
      if (lang.judgeId == judgeId) return lang;
    }
    return null;
  }

  /**
   * Returns true if the given Judge0 language ID is supported by this application.
   *
   * @param judgeId the Judge0 integer language ID to check
   * @return true if supported
   */
  public static boolean isSupported(int judgeId) {
    return fromJudgeId(judgeId) != null;
  }
}

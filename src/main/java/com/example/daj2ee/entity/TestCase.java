package com.example.daj2ee.entity;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * Represents a test case for a coding problem.
 * Used to validate user submissions by comparing actual output with expected output.
 */
@Entity
@Table(name = "test_cases")
public class TestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String input;

    @Lob
    @Column(name = "expected_output", columnDefinition = "TEXT", nullable = false)
    private String expectedOutput;

    /**
     * Whether this test case is hidden from the user (e.g., for final grading).
     */
    @Column(nullable = false)
    private boolean hidden = false;

    /**
     * Optional field to define the order in which test cases are executed.
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    public TestCase() {
    }

    public TestCase(Problem problem, String input, String expectedOutput, boolean hidden) {
        this.problem = problem;
        this.input = input;
        this.expectedOutput = expectedOutput;
        this.hidden = hidden;
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Problem getProblem() {
        return problem;
    }

    public void setProblem(Problem problem) {
        this.problem = problem;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestCase testCase = (TestCase) o;
        return Objects.equals(id, testCase.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TestCase{" +
                "id=" + id +
                ", problem=" + (problem != null ? problem.getId() : null) +
                ", hidden=" + hidden +
                ", sortOrder=" + sortOrder +
                '}';
    }
}

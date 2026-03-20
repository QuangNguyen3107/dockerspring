package com.example.daj2ee.config;

import com.example.daj2ee.entity.Problem;
import com.example.daj2ee.entity.TestCase;
import com.example.daj2ee.entity.User;
import com.example.daj2ee.repository.ProblemRepository;
import com.example.daj2ee.repository.TestCaseRepository;
import com.example.daj2ee.repository.UserRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds the database with default users, problems, and test cases on first startup.
 *
 * Seeded users:
 *   admin   / admin123   (ROLE_ADMIN, ROLE_USER)
 *   alice   / alice123   (ROLE_USER)
 *   bob     / bob123     (ROLE_USER)
 *
 * Seeded problems (one per difficulty × topic combination):
 *   EASY   – Hello World, Sum of Two Numbers, Reverse a String
 *   MEDIUM – FizzBuzz, Palindrome Check, Fibonacci (nth term)
 *   HARD   – Two Sum (return indices), Merge Two Sorted Arrays, Longest Common Prefix
 *
 * Each problem ships boilerplate for all 4 supported languages:
 *   Java (62), Python 3 (71), JavaScript/Node (63), C (50), C++ (54)
 *
 * Each problem has at least 3 test cases (mix of visible + hidden).
 *
 * NOTE: Only seeds when a problem/test-case does not yet exist — safe to restart without duplication.
 */
@Component
@Profile("!test")
public class DataInitializer implements ApplicationRunner {

  // ── Judge0 language IDs ───────────────────────────────────────────────────
  private static final int JAVA = 62;
  private static final int PYTHON = 71;
  private static final int JAVASCRIPT = 63;
  private static final int C = 50;
  private static final int CPP = 54;

  private static final Logger log = LoggerFactory.getLogger(
    DataInitializer.class
  );

  private final UserRepository userRepository;
  private final ProblemRepository problemRepository;
  private final TestCaseRepository testCaseRepository;
  private final PasswordEncoder passwordEncoder;

  public DataInitializer(
    UserRepository userRepository,
    ProblemRepository problemRepository,
    TestCaseRepository testCaseRepository,
    PasswordEncoder passwordEncoder
  ) {
    this.userRepository = userRepository;
    this.problemRepository = problemRepository;
    this.testCaseRepository = testCaseRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    ensureUsers();
    ensureProblems();
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // Users
  // ─────────────────────────────────────────────────────────────────────────────

  private void ensureUsers() {
    seedUser("admin", "admin@example.com", "admin123", "ROLE_ADMIN,ROLE_USER");
    seedUser("alice", "alice@example.com", "alice123", "ROLE_USER");
    seedUser("bob", "bob@example.com", "bob123", "ROLE_USER");
  }

  private User seedUser(
    String username,
    String email,
    String rawPassword,
    String roles
  ) {
    return userRepository
      .findByUsername(username)
      .orElseGet(() -> {
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setRoles(roles);
        User saved = userRepository.save(u);
        log.info("Seeded user '{}' ({})", username, roles);
        return saved;
      });
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // Problems
  // ─────────────────────────────────────────────────────────────────────────────

  private void ensureProblems() {
    User admin = userRepository.findByUsername("admin").orElseThrow();
    User alice = userRepository.findByUsername("alice").orElseThrow();

    seedEasyProblems(admin, alice);
    seedMediumProblems(admin, alice);
    seedHardProblems(admin, alice);

    log.info("Finished seeding problems.");
  }

  // ── EASY ─────────────────────────────────────────────────────────────────────

  private void seedEasyProblems(User admin, User alice) {
    // 1. Hello World
    Problem helloWorld = problem(
      "Hello World",
      """
      Print the text `Hello, World!` to standard output.

      This is the classic first program — get familiar with how output works \
      in your chosen language.
      """,
      "Output must be exactly: `Hello, World!` followed by a newline.",
      "EASY",
      "basics,output",
      boilerplates(
        // Java
        """
        public class Main {
            public static void main(String[] args) {
                // Your code here
            }
        }
        """,
        // Python
        """
        # Your code here
        """,
        // JavaScript
        """
        // Your code here
        """,
        // C
        """
        #include <stdio.h>
        int main() {
            // Your code here
            return 0;
        }
        """,
        // C++
        """
        #include <iostream>
        using namespace std;
        int main() {
            // Your code here
            return 0;
        }
        """
      ),
      admin
    );
    addTestCases(
      helloWorld,
      List.of(
        tc("", "Hello, World!\n", false, 1),
        tc("", "Hello, World!\n", false, 2),
        tc("", "Hello, World!\n", true, 3)
      )
    );

    // 2. Sum of Two Numbers
    Problem sumTwo = problem(
      "Sum of Two Numbers",
      """
      Read two integers from stdin (each on its own line) and print their sum.

      **Input format**
      ```
      a
      b
      ```
      **Output format**
      ```
      sum
      ```
      """,
      "Both numbers fit in a 32-bit signed integer. Output the sum followed by a newline.",
      "EASY",
      "math,input,output",
      boilerplates(
        // Java
        """
        import java.util.Scanner;
        public class Main {
            public static void main(String[] args) {
                Scanner sc = new Scanner(System.in);
                // Your code here
            }
        }
        """,
        // Python
        """
        a = int(input())
        b = int(input())
        # Your code here
        """,
        // JavaScript
        """
        const lines = require('fs').readFileSync('/dev/stdin','utf8').trim().split('\\n');
        const a = parseInt(lines[0]);
        const b = parseInt(lines[1]);
        // Your code here
        """,
        // C
        """
        #include <stdio.h>
        int main() {
            int a, b;
            scanf("%d", &a);
            scanf("%d", &b);
            // Your code here
            return 0;
        }
        """,
        // C++
        """
        #include <iostream>
        using namespace std;
        int main() {
            int a, b;
            cin >> a >> b;
            // Your code here
            return 0;
        }
        """
      ),
      alice
    );
    addTestCases(
      sumTwo,
      List.of(
        tc("1\n2\n", "3\n", false, 1),
        tc("0\n0\n", "0\n", false, 2),
        tc("-5\n3\n", "-2\n", false, 3),
        tc("100\n200\n", "300\n", true, 4)
      )
    );

    // 3. Reverse a String
    Problem reverseStr = problem(
      "Reverse a String",
      """
      Given a single line of text, print it reversed.

      **Example**
      Input: `hello`
      Output: `olleh`
      """,
      "Input is a single non-empty line with no leading/trailing whitespace.",
      "EASY",
      "strings,basics",
      boilerplates(
        // Java
        """
        import java.util.Scanner;
        public class Main {
            public static void main(String[] args) {
                Scanner sc = new Scanner(System.in);
                String line = sc.nextLine();
                // Your code here
            }
        }
        """,
        // Python
        """
        line = input()
        # Your code here
        """,
        // JavaScript
        """
        const line = require('fs').readFileSync('/dev/stdin','utf8').trim();
        // Your code here
        """,
        // C
        """
        #include <stdio.h>
        #include <string.h>
        int main() {
            char s[1001];
            scanf("%1000s", s);
            // Your code here
            return 0;
        }
        """,
        // C++
        """
        #include <iostream>
        #include <algorithm>
        using namespace std;
        int main() {
            string s;
            getline(cin, s);
            // Your code here
            return 0;
        }
        """
      ),
      alice
    );
    addTestCases(
      reverseStr,
      List.of(
        tc("hello\n", "olleh\n", false, 1),
        tc("abcde\n", "edcba\n", false, 2),
        tc("a\n", "a\n", false, 3),
        tc("racecar\n", "racecar\n", true, 4)
      )
    );
  }

  // ── MEDIUM ────────────────────────────────────────────────────────────────────

  private void seedMediumProblems(User admin, User alice) {
    // 4. FizzBuzz
    Problem fizzBuzz = problem(
      "FizzBuzz",
      """
      Given an integer `n`, print numbers from `1` to `n` (inclusive), one per line, with these rules:
      - If the number is divisible by 3, print `Fizz` instead.
      - If the number is divisible by 5, print `Buzz` instead.
      - If divisible by both 3 and 5, print `FizzBuzz`.
      - Otherwise print the number itself.
      """,
      "1 ≤ n ≤ 10,000",
      "MEDIUM",
      "loops,math,conditionals",
      boilerplates(
        // Java
        """
        import java.util.Scanner;
        public class Main {
            public static void main(String[] args) {
                Scanner sc = new Scanner(System.in);
                int n = sc.nextInt();
                // Your code here
            }
        }
        """,
        // Python
        """
        n = int(input())
        # Your code here
        """,
        // JavaScript
        """
        const n = parseInt(require('fs').readFileSync('/dev/stdin','utf8').trim());
        // Your code here
        """,
        // C
        """
        #include <stdio.h>
        int main() {
            int n;
            scanf("%d", &n);
            // Your code here
            return 0;
        }
        """,
        // C++
        """
        #include <iostream>
        using namespace std;
        int main() {
            int n;
            cin >> n;
            // Your code here
            return 0;
        }
        """
      ),
      admin
    );
    addTestCases(
      fizzBuzz,
      List.of(
        tc("5\n", "1\n2\nFizz\n4\nBuzz\n", false, 1),
        tc(
          "15\n",
          "1\n2\nFizz\n4\nBuzz\nFizz\n7\n8\nFizz\nBuzz\n11\nFizz\n13\n14\nFizzBuzz\n",
          false,
          2
        ),
        tc("1\n", "1\n", false, 3),
        tc(
          "20\n",
          "1\n2\nFizz\n4\nBuzz\nFizz\n7\n8\nFizz\nBuzz\n11\nFizz\n13\n14\nFizzBuzz\n16\n17\nFizz\n19\nBuzz\n",
          true,
          4
        )
      )
    );

    // 5. Palindrome Check
    Problem palindrome = problem(
      "Palindrome Check",
      """
      Given a string, determine whether it is a palindrome (reads the same forwards and backwards).

      Print `true` if it is a palindrome, `false` otherwise.

      Comparison is **case-sensitive**.
      """,
      "Input is a single line of printable ASCII characters, length 1–1000.",
      "MEDIUM",
      "strings,two-pointers",
      boilerplates(
        // Java
        """
        import java.util.Scanner;
        public class Main {
            public static void main(String[] args) {
                Scanner sc = new Scanner(System.in);
                String s = sc.nextLine();
                // Your code here
            }
        }
        """,
        // Python
        """
        s = input()
        # Your code here
        """,
        // JavaScript
        """
        const s = require('fs').readFileSync('/dev/stdin','utf8').trim();
        // Your code here
        """,
        // C
        """
        #include <stdio.h>
        #include <string.h>
        int main() {
            char s[1001];
            fgets(s, sizeof(s), stdin);
            int len = strlen(s);
            if (s[len-1] == '\\n') s[--len] = '\\0';
            // Your code here
            return 0;
        }
        """,
        // C++
        """
        #include <iostream>
        using namespace std;
        int main() {
            string s;
            getline(cin, s);
            // Your code here
            return 0;
        }
        """
      ),
      alice
    );
    addTestCases(
      palindrome,
      List.of(
        tc("racecar\n", "true\n", false, 1),
        tc("hello\n", "false\n", false, 2),
        tc("a\n", "true\n", false, 3),
        tc("Racecar\n", "false\n", true, 4),
        tc("abacaba\n", "true\n", true, 5)
      )
    );

    // 6. Fibonacci Number
    Problem fibonacci = problem(
      "Fibonacci Number",
      """
      Given `n`, return the `n`-th Fibonacci number (0-indexed).

      The sequence starts: `0, 1, 1, 2, 3, 5, 8, 13, 21, ...`

      So `F(0) = 0`, `F(1) = 1`, `F(6) = 8`.
      """,
      "0 ≤ n ≤ 45. Output fits in a 64-bit integer.",
      "MEDIUM",
      "math,recursion,dynamic-programming",
      boilerplates(
        // Java
        """
        import java.util.Scanner;
        public class Main {
            public static void main(String[] args) {
                Scanner sc = new Scanner(System.in);
                int n = sc.nextInt();
                // Your code here
            }
        }
        """,
        // Python
        """
        n = int(input())
        # Your code here
        """,
        // JavaScript
        """
        const n = parseInt(require('fs').readFileSync('/dev/stdin','utf8').trim());
        // Your code here
        """,
        // C
        """
        #include <stdio.h>
        int main() {
            int n;
            scanf("%d", &n);
            // Your code here
            return 0;
        }
        """,
        // C++
        """
        #include <iostream>
        using namespace std;
        int main() {
            int n;
            cin >> n;
            // Your code here
            return 0;
        }
        """
      ),
      admin
    );
    addTestCases(
      fibonacci,
      List.of(
        tc("0\n", "0\n", false, 1),
        tc("1\n", "1\n", false, 2),
        tc("6\n", "8\n", false, 3),
        tc("10\n", "55\n", true, 4),
        tc("45\n", "1134903170\n", true, 5)
      )
    );
  }

  // ── HARD ─────────────────────────────────────────────────────────────────────

  private void seedHardProblems(User admin, User alice) {
    // 7. Two Sum
    Problem twoSum = problem(
      "Two Sum",
      """
      Given an array of integers and a target value, find the **two indices** \
      whose values sum to the target.

      **Input format**
      ```
      n
      a1 a2 ... an
      target
      ```
      **Output format**
      Print two space-separated indices (0-based), smaller index first.

      Each input has **exactly one** solution and you may not use the same element twice.
      """,
      "2 ≤ n ≤ 10,000. All values and target fit in a 32-bit signed integer.",
      "HARD",
      "arrays,hash-map,two-pointers",
      boilerplates(
        // Java
        """
        import java.util.Scanner;
        public class Main {
            public static void main(String[] args) {
                Scanner sc = new Scanner(System.in);
                int n = sc.nextInt();
                int[] nums = new int[n];
                for (int i = 0; i < n; i++) nums[i] = sc.nextInt();
                int target = sc.nextInt();
                // Your code here
            }
        }
        """,
        // Python
        """
        n = int(input())
        nums = list(map(int, input().split()))
        target = int(input())
        # Your code here
        """,
        // JavaScript
        """
        const lines = require('fs').readFileSync('/dev/stdin','utf8').trim().split('\\n');
        const n = parseInt(lines[0]);
        const nums = lines[1].split(' ').map(Number);
        const target = parseInt(lines[2]);
        // Your code here
        """,
        // C
        """
        #include <stdio.h>
        int main() {
            int n;
            scanf("%d", &n);
            int nums[n];
            for (int i = 0; i < n; i++) scanf("%d", &nums[i]);
            int target;
            scanf("%d", &target);
            // Your code here
            return 0;
        }
        """,
        // C++
        """
        #include <iostream>
        #include <vector>
        using namespace std;
        int main() {
            int n;
            cin >> n;
            vector<int> nums(n);
            for (int i = 0; i < n; i++) cin >> nums[i];
            int target;
            cin >> target;
            // Your code here
            return 0;
        }
        """
      ),
      admin
    );
    addTestCases(
      twoSum,
      List.of(
        tc("4\n2 7 11 15\n9\n", "0 1\n", false, 1),
        tc("3\n3 2 4\n6\n", "1 2\n", false, 2),
        tc("2\n3 3\n6\n", "0 1\n", false, 3),
        tc("5\n1 4 8 3 2\n6\n", "1 3\n", true, 4),
        tc("6\n0 -1 2 -3 1 4\n-2\n", "1 3\n", true, 5)
      )
    );

    // 8. Merge Two Sorted Arrays
    Problem mergeSorted = problem(
      "Merge Two Sorted Arrays",
      """
      Given two sorted arrays, merge them into a single sorted array and print all elements \
      separated by spaces on one line.

      **Input format**
      ```
      m
      a1 a2 ... am
      n
      b1 b2 ... bn
      ```
      **Output format**
      All `m + n` elements in non-decreasing order, space-separated, followed by a newline.
      """,
      "0 ≤ m, n ≤ 100,000. All values fit in a 32-bit signed integer.",
      "HARD",
      "arrays,sorting,two-pointers",
      boilerplates(
        // Java
        """
        import java.util.Scanner;
        public class Main {
            public static void main(String[] args) {
                Scanner sc = new Scanner(System.in);
                int m = sc.nextInt();
                int[] a = new int[m];
                for (int i = 0; i < m; i++) a[i] = sc.nextInt();
                int n = sc.nextInt();
                int[] b = new int[n];
                for (int i = 0; i < n; i++) b[i] = sc.nextInt();
                // Your code here
            }
        }
        """,
        // Python
        """
        m = int(input())
        a = list(map(int, input().split())) if m > 0 else []
        n = int(input())
        b = list(map(int, input().split())) if n > 0 else []
        # Your code here
        """,
        // JavaScript
        """
        const lines = require('fs').readFileSync('/dev/stdin','utf8').trim().split('\\n');
        const m = parseInt(lines[0]);
        const a = m > 0 ? lines[1].split(' ').map(Number) : [];
        const n = parseInt(lines[m > 0 ? 2 : 1]);
        const b = n > 0 ? lines[m > 0 ? 3 : 2].split(' ').map(Number) : [];
        // Your code here
        """,
        // C
        """
        #include <stdio.h>
        int main() {
            int m;
            scanf("%d", &m);
            int a[m > 0 ? m : 1];
            for (int i = 0; i < m; i++) scanf("%d", &a[i]);
            int n;
            scanf("%d", &n);
            int b[n > 0 ? n : 1];
            for (int i = 0; i < n; i++) scanf("%d", &b[i]);
            // Your code here
            return 0;
        }
        """,
        // C++
        """
        #include <iostream>
        #include <vector>
        using namespace std;
        int main() {
            int m;
            cin >> m;
            vector<int> a(m);
            for (int i = 0; i < m; i++) cin >> a[i];
            int n;
            cin >> n;
            vector<int> b(n);
            for (int i = 0; i < n; i++) cin >> b[i];
            // Your code here
            return 0;
        }
        """
      ),
      alice
    );
    addTestCases(
      mergeSorted,
      List.of(
        tc("3\n1 3 5\n3\n2 4 6\n", "1 2 3 4 5 6\n", false, 1),
        tc("3\n1 2 3\n3\n4 5 6\n", "1 2 3 4 5 6\n", false, 2),
        tc("0\n\n3\n1 2 3\n", "1 2 3\n", false, 3),
        tc("4\n-3 -1 0 2\n3\n-2 1 3\n", "-3 -2 -1 0 1 2 3\n", true, 4),
        tc("2\n5 10\n2\n5 10\n", "5 5 10 10\n", true, 5)
      )
    );

    // 9. Longest Common Prefix
    Problem lcp = problem(
      "Longest Common Prefix",
      """
      Given `n` strings, find the longest common prefix shared by **all** of them.

      If there is no common prefix, print an empty line.

      **Input format**
      ```
      n
      string1
      string2
      ...
      ```
      """,
      "1 ≤ n ≤ 200. Each string has length 0–200, contains only lowercase English letters.",
      "HARD",
      "strings,sorting",
      boilerplates(
        // Java
        """
        import java.util.Scanner;
        public class Main {
            public static void main(String[] args) {
                Scanner sc = new Scanner(System.in);
                int n = sc.nextInt(); sc.nextLine();
                String[] words = new String[n];
                for (int i = 0; i < n; i++) words[i] = sc.nextLine();
                // Your code here
            }
        }
        """,
        // Python
        """
        n = int(input())
        words = [input() for _ in range(n)]
        # Your code here
        """,
        // JavaScript
        """
        const lines = require('fs').readFileSync('/dev/stdin','utf8').trim().split('\\n');
        const n = parseInt(lines[0]);
        const words = lines.slice(1, n + 1);
        // Your code here
        """,
        // C
        """
        #include <stdio.h>
        #include <string.h>
        int main() {
            int n;
            scanf("%d\\n", &n);
            char words[200][201];
            for (int i = 0; i < n; i++) fgets(words[i], 201, stdin);
            // Your code here
            return 0;
        }
        """,
        // C++
        """
        #include <iostream>
        #include <vector>
        using namespace std;
        int main() {
            int n;
            cin >> n; cin.ignore();
            vector<string> words(n);
            for (int i = 0; i < n; i++) getline(cin, words[i]);
            // Your code here
            return 0;
        }
        """
      ),
      alice
    );
    addTestCases(
      lcp,
      List.of(
        tc("3\nflower\nflow\nflight\n", "fl\n", false, 1),
        tc("3\ndog\nracecar\ncar\n", "\n", false, 2),
        tc("1\nabc\n", "abc\n", false, 3),
        tc(
          "4\ninteresting\ninterface\ninternal\ninteger\n",
          "inter\n",
          true,
          4
        ),
        tc("2\nabc\nabc\n", "abc\n", true, 5)
      )
    );
  }

  // ─────────────────────────────────────────────────────────────────────────────
  // Helpers
  // ─────────────────────────────────────────────────────────────────────────────

  /**
   * Builds a per-language boilerplate map from positional arguments.
   * Order must be: Java, Python, JavaScript, C, C++.
   */
  private Map<Integer, String> boilerplates(
    String java,
    String python,
    String javascript,
    String c,
    String cpp
  ) {
    Map<Integer, String> map = new HashMap<>();
    map.put(JAVA, java.stripIndent().strip());
    map.put(PYTHON, python.stripIndent().strip());
    map.put(JAVASCRIPT, javascript.stripIndent().strip());
    map.put(C, c.stripIndent().strip());
    map.put(CPP, cpp.stripIndent().strip());
    return map;
  }

  private Problem problem(
    String title,
    String description,
    String constraints,
    String difficulty,
    String tags,
    Map<Integer, String> boilerplates,
    User author
  ) {
    if (problemRepository.existsByTitle(title)) {
      Problem existing = problemRepository.findByTitle(title).orElseThrow();
      if (
        existing.getBoilerplates() == null ||
        existing.getBoilerplates().isEmpty()
      ) {
        existing.setBoilerplates(boilerplates);
        existing = problemRepository.save(existing);
        log.info("Patched boilerplates for existing problem '{}'", title);
      } else {
        log.debug("Problem '{}' already exists — skipping.", title);
      }
      return existing;
    }
    Problem p = new Problem();
    p.setTitle(title);
    p.setDescription(description.stripIndent().strip());
    p.setConstraints(constraints);
    p.setDifficulty(difficulty);
    p.setTags(tags);
    p.setBoilerplates(boilerplates);
    p.setAuthor(author);
    p.setPublished(true);
    Problem saved = problemRepository.save(p);
    log.info("Seeded problem [{}] '{}'", difficulty, title);
    return saved;
  }

  private TestCase tc(
    String input,
    String expectedOutput,
    boolean hidden,
    int order
  ) {
    TestCase tc = new TestCase();
    tc.setInput(input);
    tc.setExpectedOutput(expectedOutput);
    tc.setHidden(hidden);
    tc.setSortOrder(order);
    return tc;
  }

  private void addTestCases(Problem problem, List<TestCase> testCases) {
    if (testCaseRepository.existsByProblemId(problem.getId())) {
      log.debug(
        "Test cases for problem '{}' already exist — skipping.",
        problem.getTitle()
      );
      return;
    }
    for (TestCase tc : testCases) {
      tc.setProblem(problem);
      testCaseRepository.save(tc);
    }
  }
}

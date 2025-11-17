package org.vsu;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ExprEvalTests {
    // ─── 1. NUMBERS AND EXPONENTIAL NOTATION ───────────────────────────────────

    @Test
    @DisplayName("Numbers: integers, decimals, exponential notation")
    void testNumbers() {
        assertEquals(42, parseEval("42"), 0.0);
        assertEquals(3.14, parseEval("3.14"), 1e-12);
        assertEquals(0.001, parseEval("1e-3"), 1e-15);
        assertEquals(0.001, parseEval("1E-3"), 1e-15);
        assertEquals(25000.0, parseEval("2.5e4"), 1e-9);
        assertEquals(25000.0, parseEval("2.5E+4"), 1e-9);
        assertEquals(1000.0, parseEval("1e3"), 1e-9);
        assertEquals(1.0, parseEval("1e0"), 1e-12);
        assertEquals(1.0, parseEval("1E0"), 1e-12);
    }

    @Test
    @DisplayName("Error: invalid exponential notation")
    void testInvalidExponential() {
        assertThrows(IllegalArgumentException.class, () -> ExprEval.parse("1e"));
        assertThrows(IllegalArgumentException.class, () -> ExprEval.parse("e5"));
        assertThrows(IllegalArgumentException.class, () -> ExprEval.parse("1e++3"));
        assertThrows(IllegalArgumentException.class, () -> ExprEval.parse("1.2.3"));
        assertThrows(IllegalArgumentException.class, () -> ExprEval.parse("."));
    }

    // ─── 2. CONSTANTS ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Constants: pi and e")
    void testConstants() {
        Expr piExpr = ExprEval.parse("pi");
        assertEquals("pi", piExpr.toString());
        assertEquals(Math.PI, piExpr.eval(Map.of()), 1e-15);

        Expr eExpr = ExprEval.parse("e");
        assertEquals("e", eExpr.toString());
        assertEquals(Math.E, eExpr.eval(Map.of()), 1e-15);

        // In expressions
        assertTrue(Math.abs(parseEval("pi") - Math.PI) < 1e-15);
        assertTrue(Math.abs(parseEval("e") - Math.E) < 1e-15);
        assertTrue(Math.abs(parseEval("pi * 2") - 2 * Math.PI) < 1e-15);
    }

    // ─── 3. VARIABLES AND CONTEXT ───────────────────────────────────────────────

    @Test
    @DisplayName("Variables: correct values from context")
    void testVariables() {
        Map<String, Double> ctx = Map.of("x", 3.0, "y", 4.0);
        assertEquals(7.0, parseEval("x + y", ctx), 1e-12);
        assertEquals(9.0, parseEval("x ^ 2", ctx), 1e-12);
    }

    @Test
    @DisplayName("Error: unknown variable")
    void testUnknownVariable() {
        Map<String, Double> ctx = Map.of("x", 1.0);
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> parseEval("x + y", ctx)
        );
        assertTrue(ex.getMessage().contains("y"));
    }

    @Test
    @DisplayName("Variable named like a function (but not a function call)")
    void testVariableNamedLikeFunction() {
        // 'sin' as a variable — no parentheses → treated as identifier
        Expr ast = ExprEval.parse("sin");
        assertEquals("sin", ast.toString());
        assertEquals(5.0, ast.eval(Map.of("sin", 5.0)), 0.0);
    }

    // ─── 4. ARITHMETIC OPERATORS AND PRECEDENCE ────────────────────────────────

    @Test
    @DisplayName("Operator precedence: +, -, *, /, ^")
    void testOperatorPrecedence() {
        assertEquals(14.0, parseEval("2 + 3 * 4"), 1e-12);       // 2 + (3*4)
        assertEquals(20.0, parseEval("(2 + 3) * 4"), 1e-12);     // (2+3)*4
        assertEquals(81.0, parseEval("3 ^ 4"), 1e-12);           // 81
        assertEquals(512.0, parseEval("2 ^ 3 ^ 2"), 1e-12);      // 2^(3^2) = 512
        assertEquals(-5.0, parseEval("-3 - 2"), 1e-12);          // -(3) - 2
        assertEquals(1.0, parseEval("6 / 3 / 2"), 1e-12);        // (6/3)/2 = 1
    }

    @Test
    @DisplayName("Unary + and -")
    void testUnaryOperators() {
        assertEquals(5.0, parseEval("+5"), 0.0);
        assertEquals(-5.0, parseEval("-5"), 0.0);
        assertEquals(-3.0, parseEval("-(2 + 1)"), 1e-12);
        assertEquals(5.0, parseEval("+(3 + 2)"), 1e-12);
        assertEquals(-6.0, parseEval("-x", Map.of("x", 6.0)), 0.0);
    }

    // ─── 5. FUNCTIONS ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Trigonometric functions: sin, cos, tan (in radians)")
    void testTrigFunctions() {
        assertEquals(1.0, parseEval("sin(pi/2)"), 1e-12);
        assertEquals(0.0, parseEval("cos(pi/2)"), 1e-12);
        assertEquals(0.0, parseEval("sin(0)"), 1e-12);
        assertEquals(1.0, parseEval("cos(0)"), 1e-12);

        // tan(pi/4) = 1
        assertEquals(1.0, parseEval("tan(pi/4)"), 1e-12);

        // tan(pi/2) — not an exception, but a very large finite value
        double tanPi2 = parseEval("tan(pi/2)");
        assertTrue(tanPi2 > 1e15, "tan(pi/2) should be very large (~1.6e16)");
        assertFalse(Double.isInfinite(tanPi2));
    }

    @Test
    @DisplayName("sqrt, log, abs")
    void testMathFunctions() {
        assertEquals(2.0, parseEval("sqrt(4)"), 1e-12);
        // 'exp' is not supported → expect error
        assertThrows(IllegalArgumentException.class, () -> parseEval("exp(1)"));

        assertEquals(Math.log(10), parseEval("log(10)"), 1e-12);
        assertEquals(5.0, parseEval("abs(-5)"), 0.0);

        // Validation errors:
        assertThrows(IllegalArgumentException.class, () -> parseEval("sqrt(-1)"));
        assertThrows(IllegalArgumentException.class, () -> parseEval("log(0)"));
        assertThrows(IllegalArgumentException.class, () -> parseEval("log(-5)"));
    }

    @Test
    @DisplayName("clamp function: 3 arguments, lo ≤ hi validation")
    void testClampFunction() {
        assertEquals(5.0, parseEval("clamp(5, 0, 10)"), 0.0);
        assertEquals(0.0, parseEval("clamp(-3, 0, 10)"), 0.0);
        assertEquals(10.0, parseEval("clamp(15, 0, 10)"), 0.0);

        // clamp with variables
        assertEquals(100.0, parseEval("clamp(x, 0, 100)", Map.of("x", 150.0)), 0.0);

        // Errors:
        assertThrows(IllegalArgumentException.class, () -> parseEval("clamp(5, 10, 0)")); // lo > hi
        assertThrows(IllegalArgumentException.class, () -> parseEval("clamp(5, 0)"));      // too few args
        assertThrows(IllegalArgumentException.class, () -> parseEval("clamp(5, 0, 10, 20)")); // too many
    }

    @Test
    @DisplayName("Error: unknown function")
    void testUnknownFunction() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> parseEval("foo(5)")
        );
        assertTrue(ex.getMessage().contains("foo"));
    }

    // ─── 6. AST: STRUCTURE VERIFICATION VIA toString() ─────────────────────────

    @Test
    @DisplayName("AST: correct tree structure for complex expressions")
    void testASTStructure() {
        Expr ast = ExprEval.parse("2 + 3 * sin(x)");
        assertEquals("(2 + (3 * sin(x)))", ast.toString());

        ast = ExprEval.parse("clamp(temp, 0, 100)");
        assertEquals("clamp(temp, 0, 100)", ast.toString());

        ast = ExprEval.parse("-x^2");
        // Unary minus applies to the entire power: -(x^2), not (-x)^2
        assertEquals("(-(x ^ 2))", ast.toString());

        ast = ExprEval.parse("(-x)^2");
        assertEquals("(((-x)) ^ 2)", ast.toString());
    }

    // ─── 7. SYNTAX ERRORS ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Error: empty expression")
    void testEmptyExpression() {
        assertThrows(IllegalArgumentException.class, () -> ExprEval.parse(""));
        assertThrows(IllegalArgumentException.class, () -> ExprEval.parse("   "));
    }

    @Test
    @DisplayName("Error: unbalanced parentheses")
    void testUnbalancedParentheses() {
        assertThrows(IllegalArgumentException.class, () -> ExprEval.parse("(2 + 3"));
        assertThrows(IllegalArgumentException.class, () -> ExprEval.parse("2 + 3)"));
        assertThrows(IllegalArgumentException.class, () -> ExprEval.parse("((2 + 3)"));
    }

    @Test
    @DisplayName("Error: incomplete expressions")
    void testIncompleteExpressions() {
        assertThrows(IllegalArgumentException.class, () -> ExprEval.parse("5 +"));
        assertThrows(IllegalArgumentException.class, () -> ExprEval.parse("* 3"));
        assertThrows(IllegalArgumentException.class, () -> ExprEval.parse("sin("));
        assertThrows(IllegalArgumentException.class, () -> ExprEval.parse("sin(5"));
        assertThrows(IllegalArgumentException.class, () -> ExprEval.parse("sin 5")); // no parentheses → not a function call
    }

    // ─── 8. NUMERICAL EDGE CASES ────────────────────────────────────────────────

    @Test
    @DisplayName("Division by zero")
    void testDivisionByZero() {
        ArithmeticException ex = assertThrows(
                ArithmeticException.class,
                () -> parseEval("5 / 0")
        );
        assertTrue(ex.getMessage().contains("zero") || ex.getMessage().contains("ноль"));
    }

    @Test
    @DisplayName("Overflow (infinity)")
    void testOverflow() {
        double inf = parseEval("1e308 * 10");
        assertTrue(Double.isInfinite(inf));
        assertTrue(inf > 0);
    }

    @Test
    @DisplayName("NaN: 0/0; note: sqrt(-0.0) = -0.0 per IEEE 754")
    void testNaN() {
        // 0.0 / 0.0 → NaN
        double nan = ExprEval.parse("0 / 0").eval(Map.of());
        assertTrue(Double.isNaN(nan));

        // sqrt(-0.0) = -0.0, not NaN!
        double negZero = ExprEval.parse("sqrt(-0.0)").eval(Map.of());
        assertEquals(-0.0, negZero, 0.0);
        assertEquals(Double.doubleToRawLongBits(negZero), Double.doubleToRawLongBits(-0.0));

        // But sqrt(-1e-10) → validation error (negative argument)
        assertThrows(IllegalArgumentException.class, () -> parseEval("sqrt(-1e-10)"));
    }

    // ─── 9. HELPER METHODS ──────────────────────────────────────────────────────

    @Test
    @DisplayName("extractVariables: correct variable name extraction")
    void testExtractVariables() {
        Expr ast = ExprEval.parse("x + y * sin(z)");
        Set<String> vars = ExprEval.extractVariables(ast);
        assertEquals(Set.of("x", "y", "z"), vars);

        ast = ExprEval.parse("pi * r^2");
        vars = ExprEval.extractVariables(ast);
        assertEquals(Set.of("r"), vars); // 'pi' is a constant, not a variable

        ast = ExprEval.parse("5 + 3");
        vars = ExprEval.extractVariables(ast);
        assertTrue(vars.isEmpty());
    }

    // ─── HELPER METHODS FOR TESTS ───────────────────────────────────────────────

    /** Evaluate expression without variables */
    private double parseEval(String expr) {
        return ExprEval.parse(expr).eval(Map.of());
    }

    /** Evaluate expression with context */
    private double parseEval(String expr, Map<String, Double> ctx) {
        return ExprEval.parse(expr).eval(ctx);
    }
}

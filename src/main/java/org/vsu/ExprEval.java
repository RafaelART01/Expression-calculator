package org.vsu;

import java.util.*;

public class ExprEval {
    public static Expr parse(String expression){
        Lexer lexer = new Lexer(expression);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        return parser.parse();
    }

    public static double evaluate(String expression, Map<String, Double> variables){
        Expr ast = parse(expression);
        return ast.eval(variables);
    }

    public static double evaluateInteractive(String expression){
        Expr ast = parse(expression);
        Set<String> vars = extractVariables(ast);
        Map<String, Double> context = new HashMap<>();

        Scanner scanner = new Scanner(System.in);
        for (String var : vars){
            if (!isConstant(var)){
                System.out.print("Enter value for the variable '" + var + "': ");
                try{
                    context.put(var, Double.parseDouble(scanner.nextLine().trim()));
                } catch (NumberFormatException e){
                    throw new IllegalArgumentException("Incorrect value for the variable '" + var + "'");
                }
            }
        }
        return ast.eval(context);
    }

    private static boolean isConstant(String name){
        return "pi".equals(name) || "e".equals(name);
    }

    private static Set<String> extractVariables(Expr expr){
        Set<String> vars = new LinkedHashSet<>();
        extractVarsRecursive(expr, vars);
        return vars;
    }

    private static void extractVarsRecursive(Expr expr, Set<String> vars){
        if (expr instanceof VariableExpr v){
            vars.add(v.toString());
        } else if (expr instanceof BinaryExpr b) {
            extractVarsRecursive(b.left(), vars);
            extractVarsRecursive(b.right(), vars);
        } else if (expr instanceof UnaryExpr u) {
            extractVarsRecursive(u.operand(), vars);
        } else if (expr instanceof FunctionCallExpr f) {
            for (Expr arg : f.args()){
                extractVarsRecursive(arg, vars);
            }
        }
    }
}

package org.vsu;

import java.util.*;

public class Main {
    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Expression evaluator with functions(sin, cos, sqrt, tan, log, abs, clamp), " +
                "constant(pi, e) and variables implementation");
        System.out.println("Enter 'exit' to finish \n");

        while (true){
            System.out.print("Expression: ");
            String input = scanner.nextLine().trim();

            if ("exit".equalsIgnoreCase(input)){
                System.out.println("Goodbye!");
                break;
            }

            if (input.isEmpty()){
                System.out.println("Error! Empty input. Try again!");
                continue;
            }

            try {
                Expr ast = ExprEval.parse(input);
                System.out.println("AST: " + ast);

                double result = ExprEval.evaluateInteractive(ast);
                System.out.printf("Answer: %.10f%n%n", result);
            } catch (Exception e){
                System.err.println("Error! " + e.getMessage() + "\n");
            }
        }

        scanner.close();
    }
}

package org.vsu;

import java.util.List;

public class FunctionCallExpr implements Expr{
    private final String funcName;
    private final List<Expr> args;

    public FunctionCallExpr(String funcName, List<Expr> args){
        this.funcName = funcName;
        this.args = args;
    }

    @Override
    public double eval(java.util.Map<String, Double> variables){
        java.util.List<Double> evaluatedArgs = args.stream()
                .map(expr -> expr.eval(variables))
                .toList();

        return switch (funcName.toLowerCase()){
            case "sin" -> {
                checkArgs(1, evaluatedArgs, funcName);
                yield Math.sin(evaluatedArgs.getFirst());
            }
            case "cos" -> {
                checkArgs(1, evaluatedArgs, funcName);
                yield Math.cos(evaluatedArgs.getFirst());
            }
            case "tan" -> {
                checkArgs(1, evaluatedArgs, funcName);
                yield Math.tan(evaluatedArgs.getFirst());
            }
            case "sqrt" -> {
                checkArgs(1, evaluatedArgs, funcName);
                double x = evaluatedArgs.getFirst();
                if (x < 0) throw new IllegalArgumentException("Sqrt of a negative number: " + x);
                yield Math.sqrt(x);
            }
            case "log" -> {
                checkArgs(1, evaluatedArgs, funcName);
                double x = evaluatedArgs.getFirst();
                if (x <= 0) throw new IllegalArgumentException("Log of a non-positive number: " + x);
                yield Math.log(x);
            }
            case "abs" -> {
                checkArgs(1, evaluatedArgs, funcName);
                yield  Math.abs(evaluatedArgs.getFirst());
            }
            case "clamp" -> {
                checkArgs(3, evaluatedArgs, funcName);
                double v = evaluatedArgs.getFirst(), lo = evaluatedArgs.get(1), hi = evaluatedArgs.get(2);
                if (lo > hi) throw new IllegalArgumentException("Clamp: lower > higher");
                yield Math.max(lo, Math.min(hi, v));
            }
            default -> throw new IllegalArgumentException("Unknown function: " + funcName);
        };
    }

    private void checkArgs(int expected, java.util.List<Double> actual, String func){
        if (actual.size() != expected){
            throw new IllegalArgumentException("Function '" + func + "' awaits " + expected + " arguments, " +
                    "but received: " + actual.size());
        }
    }

    @Override
    public String toString(){
        String argsStr = args.stream().map(Expr::toString).reduce((a, b) -> a + ", " + b)
                .orElse("");
        return funcName + "(" + argsStr + ")";
    }
}

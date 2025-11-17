package org.vsu;

public class BinaryExpr implements Expr{
    private final String op;
    private final Expr left;
    private final Expr right;

    public BinaryExpr(String op, Expr left, Expr right){
        this.op = op;
        this.left = left;
        this.right = right;
    }

    public Expr left(){ return left; }
    public Expr right(){ return right; }

    public double eval(java.util.Map<String, Double> variables){
        double l = left.eval(variables);
        double r = right.eval(variables);
        return switch (op) {
            case "+" -> l + r;
            case "-" -> l - r;
            case "*" -> l * r;
            case "/" -> {
                if (r == 0.0) {
                    if (l == 0.0) {
                        yield  Double.NaN;
                    }
                    yield l > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
                }
                yield l / r;
            }
            case "^" -> Math.pow(l, r);
            default -> throw new IllegalArgumentException("Unknown operation: " + op);
        };
    }

    @Override
    public String toString(){
        return "(" + left + " " + op + " " + right + ")";
    }
}

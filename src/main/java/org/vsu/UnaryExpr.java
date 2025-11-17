package org.vsu;

public class UnaryExpr implements Expr{
    private final String op;
    private final Expr operand;

    public UnaryExpr(String op, Expr operand){
        this.op = op;
        this.operand = operand;
    }

    public Expr operand(){ return  operand; }

    @Override
    public double eval(java.util.Map<String, Double> variables){
        double val = operand.eval(variables);
        return op.equals("-") ? -val : val;
    }

    @Override
    public String toString(){
        return "(" + op + operand + ")";
    }
}

package org.vsu;

public class NumberExpr implements Expr{
    private final double value;

    public NumberExpr(double value){
        this.value = value;
    }

    @Override
    public double eval(java.util.Map<String, Double> variables){
        return value;
    }

    @Override
    public String toString(){
        if (value == (long) value){
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
}

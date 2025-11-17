package org.vsu;

public class VariableExpr implements Expr{
    private final String name;

    public VariableExpr(String name){
        this.name = name;
    }

    @Override
    public double eval(java.util.Map<String, Double> variables){
        if (!variables.containsKey(name)){
            throw new IllegalArgumentException("Unknown variable: " + name);
        }
        return variables.get(name);
    }

    @Override
    public String toString(){
        return name;
    }
}

package org.vsu;

public interface Expr {
    double eval(java.util.Map<String, Double> variables);

    @Override
    String toString();
}

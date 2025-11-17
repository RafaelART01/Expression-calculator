package org.vsu;

public class ConstantExpr implements Expr{
    public enum Constant{
        PI(Math.PI, "pi"),
        E(Math.E, "e");

        public final double value;
        public final String name;

        Constant(double value, String name){
            this.value = value;
            this.name = name;
        }

        public static Constant fromName(String name){
            for (Constant c : values()){
                if (c.name.equals(name)) return c;
            }
            return null;
        }
    }

    private final Constant constant;

    public ConstantExpr(Constant constant){
        this.constant = constant;
    }

    @Override
    public double eval(java.util.Map<String, Double> variables){
        return constant.value;
    }

    @Override
    public String toString(){
        return constant.name;
    }
}

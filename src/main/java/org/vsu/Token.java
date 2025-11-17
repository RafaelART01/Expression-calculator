package org.vsu;

public class Token {
    public enum Type{
        NUMBER, IDENTIFIER, PLUS, MINUS, MULT, DIV, POW,
        LPAREN, RPAREN, COMMA
    }

    public final Type type;
    public final String value;

    public Token(Type type, String value){
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString(){
        return type + "(" + value + ")";
    }
}

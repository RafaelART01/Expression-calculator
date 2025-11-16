package org.vsu;

import java.util.*;

public class Parser {
    private final List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    public Expr parse(){
        if (tokens.isEmpty()) throw new IllegalArgumentException("Empty expression");
        Expr expr = parseExpression();
        if (pos < tokens.size()){
            throw new IllegalArgumentException("Excessive symbols after expression: " + tokens.get(pos));
        }
        return expr;
    }

    private Expr parseExpression(){
        Expr left = parseMultDiv();
        while (pos < tokens.size()){
            Token.Type type = tokens.get(pos).type;
            if (type == Token.Type.PLUS || type == Token.Type.MINUS){
                String op = tokens.get(pos).value;
                pos++;
                Expr right = parseMultDiv();
                left = new BinaryExpr(op, left, right);
            } else {
                break;
            }
        }
        return left;
    }

    private Expr parseMultDiv(){
        Expr left = parsePower();
        while (pos < tokens.size()){
            Token.Type type = tokens.get(pos).type;
            if (type == Token.Type.MULT || type == Token.Type.DIV){
                String op = tokens.get(pos).value;
                pos++;
                Expr right = parsePower();
                left = new BinaryExpr(op, left, right);
            } else {
                break;
            }
        }
        return left;
    }

    private Expr parsePower(){
        Expr left = parseUnary();
        while (pos < tokens.size() && tokens.get(pos).type == Token.Type.POW){
            pos++;
            Expr right = parseUnary();
            left = new BinaryExpr("^", left, right);
        }
        return left;
    }

    private Expr parseUnary(){
        if (pos < tokens.size()){
            Token token = tokens.get(pos);
            if (token.type == Token.Type.PLUS || token.type == Token.Type.MINUS){
                String op = token.value;
                pos++;
                Expr operand = parseUnary();
                return new UnaryExpr(op, operand);
            }
        }
        return parsePrimary();
    }

    private Expr parsePrimary(){
        if (pos >= tokens.size()){
            throw new IllegalArgumentException("Number, variable, function or '(' were awaited");
        }

        Token token = tokens.get(pos);
        switch (token.type){
            case NUMBER -> {
                pos++;
                return new NumberExpr(Double.parseDouble(token.value));
            }
            case IDENTIFIER -> {
                String name = token.value;
                ConstantExpr.Constant constant = ConstantExpr.Constant.fromName(name);
                if (constant != null){
                    pos++;
                    return new ConstantExpr(constant);
                }

                //Функция?
                if (pos + 1 < tokens.size() && tokens.get(pos + 1).type == Token.Type.LPAREN){
                    return parseFunctionCall(name);
                }

                //Если нет, значит переменная
                pos++;
                return new VariableExpr(name);
            }
            case LPAREN -> {
                pos++;
                Expr expr = parseExpression();
                if (pos >= tokens.size() || tokens.get(pos).type != Token.Type.RPAREN){
                    throw new IllegalArgumentException("')' was awaited");
                }
                pos++;
                return expr;
            }
            default -> {
                throw new IllegalArgumentException("Unknown token: " + token);
            }
        }
    }

    private Expr parseFunctionCall(String funcName){
        pos += 2;
        List<Expr> args = new ArrayList<>();
        if (pos < tokens.size() && tokens.get(pos).type != Token.Type.RPAREN){
            args.add(parseExpression());
            while (pos < tokens.size() && tokens.get(pos).type == Token.Type.COMMA){
                pos++;
                args.add(parseExpression());
            }
        }
        if (pos >= tokens.size() || tokens.get(pos).type != Token.Type.RPAREN){
            throw new IllegalArgumentException("')' was awaited after arguments of the function " + funcName);
        }
        pos++;
        return new FunctionCallExpr(funcName, args);
    }
}

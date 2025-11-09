package org.vsu;

import java.util.*;

public class Lexer {
    private final String input;
    private int pos = 0;

    public Lexer(String input){
        this.input = input.replaceAll("\\s+", "");
    }

    public List<Token> tokenize(){
        List<Token> tokens = new ArrayList<>();
        while (pos < input.length()){
            char c = input.charAt(pos);

            if (c >= '0' && c <= '9' || c == '.'){
                tokens.add(lexNumber());
                continue;
            }

            if (Character.isLetter(c)){
                tokens.add(lexIdentifier());
                continue;
            }

            switch (c){
                case '+': tokens.add(new Token(Token.Type.PLUS, "+")); break;
                case '-': tokens.add(new Token(Token.Type.MINUS, "-")); break;
                case '*': tokens.add(new Token(Token.Type.MULT, "*")); break;
                case '/': tokens.add(new Token(Token.Type.DIV, "/")); break;
                case '^': tokens.add(new Token(Token.Type.POW, "^")); break;
                case '(': tokens.add(new Token(Token.Type.LPAREN, "(")); break;
                case ')': tokens.add(new Token(Token.Type.RPAREN, ")")); break;
                case ',': tokens.add(new Token(Token.Type.COMMA, ",")); break;
                default:
                    throw new IllegalArgumentException("Unknown token: '" + c + "' on position " + pos);
            }

            pos++;
        }
        return tokens;
    }

    private Token lexNumber(){
        int start = pos;
        while (pos < input.length()){
            char c = input.charAt(pos);
            if (Character.isDigit(c) || c == '.' || c == 'e' || c == 'E' || c == '+' || c == '-'){
                if ((c == '+' || c == '-') && pos >start){
                    char prev = input.charAt(pos - 1);
                    if (prev != 'e' && prev != 'E') break;
                }
                pos++;
            } else{
                break;
            }
        }
        String numStr = input.substring(start, pos);
        try {
            Double.parseDouble(numStr);
        } catch (NumberFormatException e){
            throw new IllegalArgumentException("Incorrect number: " + numStr);
        }
        return new Token (Token.Type.NUMBER, numStr);
    }

    private Token lexIdentifier(){
        int start = pos;
        while (pos < input.length() && Character.isLetterOrDigit(input.charAt(pos))){
            pos++;
        }
        String word = input.substring(start, pos);
        return new Token(Token.Type.IDENTIFIER, word);
    }
}

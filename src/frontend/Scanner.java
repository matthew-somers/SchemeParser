package frontend;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import frontend.Token.Type;

public class Scanner
{
    public Scanner(File file) throws FileNotFoundException
    {
        this.file = new java.util.Scanner(file);
        tokens = new ArrayList<Token>();
    }
    
    public boolean hasNextToken()
    {
        return tokens.size() > 0 || file.hasNextLine();
    }
    
    public Token nextToken()
    {
        while (tokens.size() == 0 && file.hasNextLine())
        {
            tokens.addAll(getTokens(file.nextLine()));
        }
        if (!hasNextToken())
            return null;
        // System.out.printf("Reading Token: %s\n", tokens.get(0).getName());
        return tokens.remove(0);
    }
    
    public Token peekNext()
    {
        while (tokens.size() == 0 && file.hasNextLine())
        {
            tokens.addAll(getTokens(file.nextLine()));
        }
        if (!hasNextToken())
            return null;
        // System.out.printf("Reading Token: %s\n", tokens.get(0).getName());
        return tokens.get(0);
    }
    
    private static ArrayList<Token> getTokens(String line)
    {
        ArrayList<Token> tokens = new ArrayList<Token>();
        char[] characters = line.toCharArray();
        Token currentToken = null;
        for (char c : characters)
        {
            String s = new Character(c).toString();
            if (s.equals(";"))
                return tokens;
            if (currentToken == null)
            {
                currentToken = initializeToken(s);
            }
            else
            {
                if (currentToken.getName().concat(s)
                                .matches(currentToken.getType().getFormat()))
                {
                    currentToken.setName(currentToken.getName().concat(s));
                }
                else
                {
                    tokens.add(finalize(currentToken));
                    currentToken = initializeToken(s);
                }
            }
        }
        if (currentToken != null)
        {
            tokens.add(finalize(currentToken));
        }
        return tokens;
    }
    
    private static Token initializeToken(String s)
    {
        for (Type t : Token.Type.values())
        {
            if (s.matches(t.getInitChars()))
                return new Token(t, s);
        }
        return null;
    }
    
    private static Token finalize(Token t)
    {
        if (t.getType() == Token.Type.Word
                        && t.getName().matches(
                                        Token.Type.ReservedWord.getFormat()))
        {
            t.setType(Token.Type.ReservedWord);
        }
        return t;
    }
    
    private java.util.Scanner file;
    public ArrayList<Token>   tokens;
}

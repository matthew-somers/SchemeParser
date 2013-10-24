package frontend;

import intermediate.IntermediateCode.CodeTree;
import intermediate.IntermediateCode.CodeTree.Blank;
import intermediate.IntermediateCode.CodeTree.Node;
import java.io.File;
import java.io.FileNotFoundException;

public class Parser
{
    public static void main(String[] args)
    {
        try
        {
            Scanner s = new Scanner(new File("test.lisp"));
            s.nextToken();
            while (s.hasNextToken())
            {
                System.out.println(Parser.createCodeTree(s).toString(0));
                // code.addCodeTree(Parser.createCodeTree(s));
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }
    
    public static CodeTree createCodeTree(Scanner s)
    {
        CodeTree c = new CodeTree();
        CodeTree currentPart = c;
        while (s.hasNextToken())
        {
            Token t = s.nextToken();
            if (t.getType().equals(Token.Type.Symbol)
                            && t.getName().equals("("))
            {
                currentPart.setLeft(createCodeTree(s));
                if (!s.peekNext().getType().equals(Token.Type.Symbol)
                                || !s.peekNext().getName().equals(")"))
                {
                    Blank b = new Blank();
                    currentPart.setRight(b);
                    currentPart = b;
                }
            }
            else if (t.getType().equals(Token.Type.Symbol)
                            && t.getName().equals(")"))
            {
                return c;
            }
            else
            {
                currentPart.setLeft(new Node(t.getName()));
                if (!s.peekNext().getType().equals(Token.Type.Symbol)
                                || !s.peekNext().getName().equals(")"))
                {
                    Blank b = new Blank();
                    currentPart.setRight(b);
                    currentPart = b;
                }
            }
        }
        return null;
    }
}

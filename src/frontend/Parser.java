package frontend;

import intermediate.IntermediateCode;
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
            Token current = s.nextToken();
            IntermediateCode code = new IntermediateCode();
            while (s.hasNextToken())
            {
                depth = 0;
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
        System.out.println(depth++);
        CodeTree c = new CodeTree();
        CodeTree currentPart = c;
        while (s.hasNextToken())
        {
            Token t = s.nextToken();
            System.out.printf("Token: %s\n", t.getName());
            if (t.getType().equals(Token.Type.Symbol)
                            && t.getName().equals("("))
            {
                System.out.println("Recursive CodeTree call!");
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
                System.out.println("CodeTree resolved!");
                System.out.println(--depth);
                return c;
            }
            else
            {
                System.out.printf("Adding node: %s\n", t.getName());
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
    
    public static int depth;
}

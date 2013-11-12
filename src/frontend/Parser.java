package frontend;

import intermediate.IntermediateCode;
import intermediate.IntermediateCode.CodeTree;
import intermediate.IntermediateCode.CodeTree.Blank;
import intermediate.IntermediateCode.CodeTree.Node;
import java.io.File;
import java.io.FileNotFoundException;
import backend.CodeGenerator;
import backend.Interpreter;

public class Parser
{
    public static void main(String[] args)
    {
        IntermediateCode icode = new IntermediateCode();
        CodeTree c = new CodeTree();
        CodeGenerator cg = new CodeGenerator(icode);
        Interpreter interpreter = new Interpreter();
        try
        {
            Scanner s = new Scanner(new File("test.lisp")); // our own scanner
            s.nextToken();
            
            while (s.hasNextToken())
            {
                c = Parser.createCodeTree(s);
                icode.addCodeTree(c);
                icode.fillSymbolTable();
                // cg.traverseandprint();
                // System.out.println();
                interpreter.interpret(icode);
                // spec says to clear icode and symbol table after each top
                // level list
                icode.reset();
                
                s.nextToken();
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }
    
    public static CodeTree createCodeTree(Scanner s)
    {
        // System.out.println("CREATE!");
        CodeTree c = new CodeTree();
        CodeTree currentPart = c;
        while (s.hasNextToken())
        {
            Token t = s.nextToken();
            // System.out.println(t);
            
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
            else if (t.getType().equals(Token.Type.Symbol)
                            && t.getName().equals("'"))
            {
                CodeTree quote = new CodeTree();
                quote.setLeft(new Node("quote", Token.Type.ReservedWord));
                quote.setRight(new Blank());
                t = s.nextToken();
                if (t.getType() == Token.Type.Symbol && t.getName().equals("("))
                {
                    quote.getRight().setLeft(createCodeTree(s));
                }
                else
                {
                    quote.setRight(new Blank());
                    quote.getRight()
                                    .setLeft(new Node(t.getName(), t.getType()));
                }
                currentPart.setLeft(quote);
                if (!s.peekNext().getType().equals(Token.Type.Symbol)
                                || !s.peekNext().getName().equals(")"))
                {
                    Blank b = new Blank();
                    currentPart.setRight(b);
                    currentPart = b;
                }
            }
            else
            {
                currentPart.setLeft(new Node(t.getName(), t.getType()));
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

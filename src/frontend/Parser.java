package frontend;

import intermediate.SymbolTable;
import java.io.File;
import java.io.FileNotFoundException;

public class Parser
{
    public static void main(String[] args)
    {
        try
        {
            Scanner s = new Scanner(new File("input.lisp"));
            SymbolTable symTable = new SymbolTable();
            SchemeList[] lists = Parser.getLists(s);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }
    
    public static class SchemeList extends Token
    {
        public SchemeList()
        {
            super(Token.Type.List, "");
        }
    }
}

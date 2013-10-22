package intermediate;

import java.util.TreeMap;

public class SymbolTable
{
    public SymbolTable()
    {
        symbols = new TreeMap<String, ParseTree>();
    }
    
    public TreeMap<String, ParseTree> getSymbols()
    {
        return symbols;
    }
    
    private TreeMap<String, ParseTree> symbols;
    
    public static class ParseTree
    {
        public ParseTree()
        {
            left = null;
            right = null;
        }
        
        public ParseTree getLeft()
        {
            return left;
        }
        
        public void setLeft(ParseTree left)
        {
            this.left = left;
        }
        
        public ParseTree getRight()
        {
            return right;
        }
        
        public void setRight(ParseTree right)
        {
            this.right = right;
        }
        
        private ParseTree left;
        private ParseTree right;
        
        public static class Leaf extends ParseTree
        {
            
        }
    }
}

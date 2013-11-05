package intermediate;

import intermediate.IntermediateCode.CodeTree;
import intermediate.IntermediateCode.CodeTree.Blank;
import intermediate.IntermediateCode.CodeTree.Node;
import intermediate.IntermediateCode.TreePart;
import java.util.TreeMap;

public class SymbolTable
{
    private TreeMap<String, CodeTree> symbols;
    
    public SymbolTable()
    {
        symbols = new TreeMap<String, CodeTree>();
    }
    
    public TreeMap<String, CodeTree> getSymbols()
    {
        return symbols;
    }
    
    // string gymnastics, as traversing tree is a pain
    public void analyzeTree(CodeTree tree)
    {
        TreePart left = tree.getLeft();
        if (left instanceof Node)
        {
            if (((Node) left).getValue().equals("define"))
            {
                Blank right = tree.getRight();
                String symbolName = ((Node) right.getLeft()).getValue();
                CodeTree symbolTree = ((CodeTree) right.getRight().getLeft());
                // System.out.printf("Adding '%s' to the symTable:\n",
                // symbolName);
                // System.out.println(symbolTree.toString(0));
                symbols.put(symbolName, symbolTree);
            }
        }
    }
    
    public void printSymbolTable()
    {
        System.out.println("\nSymbol Table: " + symbols.toString());
    }
    
    public void clear()
    {
        symbols.clear();
    }
}

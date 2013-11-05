package backend;

import intermediate.IntermediateCode;
import intermediate.IntermediateCode.CodeTree;
import intermediate.IntermediateCode.CodeTree.Blank;
import intermediate.IntermediateCode.CodeTree.Node;
import intermediate.SymbolTable;
import java.util.ArrayList;

public class Interpreter
{
    public Interpreter()
    {
        symbolTableStack = new SymbolTableStack();
    }
    
    public void interpret(IntermediateCode iCode)
    {
        System.out.println(interpret(iCode.getCodeTree()));
    }
    
    private String interpret(CodeTree tree)
    {
        System.out.println("INTERPRET!");
        System.out.println(tree.toString(0));
        Node car = (Node) tree.getLeft();
        String output = "ERROR";
        switch (car.getType())
        {
            case Number:
                System.out.printf("Attempt to apply non-procedure '%s'.\n",
                                car.getValue());
            break;
            case ReservedWord:
                System.out.println("ReservedWord!");
                output = ReservedWord.valueOf(car.getValue()).applyTo(this,
                                tree.getRight());
            break;
            case Symbol:
                if (car.getValue().equals("'"))
                {
                    output = ReservedWord.valueOf("quote").applyTo(this,
                                    tree.getRight());
                }
                else
                {
                    System.out.printf("Attempt to apply non-procedure '%s'.\n",
                                    car.getValue());
                }
            break;
            case Word:
                System.out.println("Word!");
                output = interpret(symbolTableStack.getCodeTreeForSymbol(car
                                .getValue()));
            break;
        }
        return output;
    }
    
    public SymbolTableStack getSymbolTableStack()
    {
        return symbolTableStack;
    }
    
    private SymbolTableStack symbolTableStack;
    
    public static class SymbolTableStack
    {
        public SymbolTableStack()
        {
            symbolTables = new ArrayList<SymbolTable>();
            symbolTables.add(new SymbolTable());
        }
        
        public void addSymbolAtBase(String name, CodeTree value)
        {
            symbolTables.get(0).getSymbols().put(name, value);
        }
        
        public CodeTree getCodeTreeForSymbol(String value)
        {
            for (int i = symbolTables.size() - 1; i >= 0; i--)
            {
                SymbolTable table = symbolTables.get(i);
                if (table.getSymbols().containsKey("value"))
                {
                    return table.getSymbols().get("value");
                }
            }
            return null;
        }
        
        private ArrayList<SymbolTable> symbolTables;
    }
    
    public static enum ReservedWord
    {
        let("let", null), letStar("let*", null), define("define",
                        new Applicator()
                        {
                            
                            @Override
                            public String apply(Interpreter i, Blank node)
                            {
                                String symbolName = ((Node) node.getLeft())
                                                .getValue();
                                CodeTree symbolTree = ((CodeTree) node
                                                .getRight().getLeft());
                                i.getSymbolTableStack().addSymbolAtBase(
                                                symbolName, symbolTree);
                                System.out.printf(
                                                "Adding '%s' to the symTable:\n",
                                                symbolName);
                                System.out.println(symbolTree.toString(0));
                                return String.format("[%s]", ((Node) node
                                                .getLeft()).getValue());
                            }
                        });
        private ReservedWord(String word, Applicator applicator)
        {
            this.word = word;
            this.applicator = applicator;
        }
        
        public String applyTo(Interpreter i, Blank node)
        {
            return applicator.apply(i, node);
        }
        
        private String     word;
        private Applicator applicator;
        
        private interface Applicator
        {
            public String apply(Interpreter i, Blank node);
        }
    }
}

package backend;

import frontend.Token;
import frontend.Token.Type;
import intermediate.IntermediateCode;
import intermediate.IntermediateCode.CodeTree;
import intermediate.IntermediateCode.CodeTree.Blank;
import intermediate.IntermediateCode.CodeTree.Node;
import intermediate.IntermediateCode.TreePart;
import intermediate.SymbolTable;
import java.util.ArrayList;
import backend.Interpreter.SymbolTableStack.NoSuchSymbolException;

public class Interpreter
{
    public Interpreter()
    {
        symbolTableStack = new SymbolTableStack();
    }
    
    public void interpret(IntermediateCode iCode)
    {
        System.out.println(iCode.getCodeTree().toString(0));
        System.out.println(">> " + interpret(iCode.getCodeTree()));
    }
    
    private String interpret(CodeTree tree)
    {
        String output = "ERROR";
        // System.out.println("INTERPRET!");
        // System.out.println(tree.toString(0));
        if (tree.getLeft() instanceof CodeTree)
        {
            tree.setLeft(consolidate((CodeTree) tree.getLeft()));
        }
        Node car = (Node) tree.getLeft();
        switch (car.getType())
        {
            case Number:
                System.out.printf("Attempt to apply non-procedure '%s'.\n",
                                car.getValue());
            break;
            case ReservedWord:
                // System.out.println("ReservedWord!");
                output = ReservedWord.getWord(car.getValue()).applyTo(this,
                                resolve(tree.getRight()));
            break;
            case Symbol:
                if (car.getValue().equals("'"))
                {
                    output = ReservedWord.getWord("quote").applyTo(this,
                                    resolve(tree.getRight()));
                }
                else
                {
                    System.out.printf("Attempt to apply non-procedure '%s'.\n",
                                    car.getValue());
                }
            break;
            case Word:
                // System.out.println("Word!");
                TreePart wordTree = null;
                try
                {
                    wordTree = symbolTableStack.getCodeTreeForSymbol(car
                                    .getValue());
                }
                catch (NoSuchSymbolException e)
                {
                    return e.getMessage();
                }
                // System.out.printf("Tied to code tree:\n%s\n",
                // wordTree.toString(4));
                CodeTree superTree = new CodeTree();
                superTree.setLeft(wordTree);
                superTree.setRight(tree.getRight());
                output = interpret(superTree);
            break;
            case Lambda:
                // System.out.println("Lambda!");
                output = ((Lambda) car).applyTo(this, resolve(tree.getRight()));
            break;
        }
        return output;
    }
    
    private Node consolidate(CodeTree tree)
    {
        // System.out.println("Consolidate!");
        // System.out.println(tree.toString(0));
        if (tree.getLeft() instanceof CodeTree)
        {
            tree.setLeft(consolidate((CodeTree) tree.getLeft()));
        }
        Node left = (Node) tree.getLeft();
        if (left.getValue().equals("lambda"))
        {
            return new Lambda((CodeTree) tree.getRight().getLeft(),
                            (CodeTree) tree.getRight().getRight().getLeft());
        }
        else
        {
            String content = interpret(tree);
            return new Node(
                            content,
                            content.matches(Token.Type.Number.getFormat()) ? Token.Type.Number
                                            : (content.matches(Token.Type.ReservedWord
                                                            .getFormat()) ? Token.Type.ReservedWord
                                                            : Token.Type.Word));
        }
    }
    
    private Blank resolve(Blank node)
    {
        // System.out.println("Resolving!");
        // System.out.println(node.toString(0));
        if (node.getLeft() instanceof Node)
        {
            switch (((Node) node.getLeft()).getType())
            {
                case Lambda:
                break;
                case Number:
                break;
                case ReservedWord:
                break;
                case Symbol:
                break;
                case Word:
                    try
                    {
                        String old = ((Node) node.getLeft()).getValue();
                        node.setLeft(this.symbolTableStack
                                        .getCodeTreeForSymbol(((Node) node
                                                        .getLeft()).getValue()));
                        // System.out.printf("'%s' was substituted out.\n",
                        // old);
                        // System.out.println("Replaced by:");
                        node.getLeft().toString(0);
                    }
                    catch (NoSuchSymbolException e)
                    {
                    }
                break;
            }
        }
        if (node.getRight() != null)
        {
            node.setRight(resolve(node.getRight()));
        }
        return node;
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
        
        public void addSymbolAtBase(String name, TreePart symbolTree)
        {
            symbolTables.get(0).getSymbols().put(name, symbolTree);
        }
        
        public void addSymbolTable(SymbolTable s)
        {
            symbolTables.add(s);
        }
        
        public void removeSymbolTable(SymbolTable s)
        {
            symbolTables.remove(s);
        }
        
        public TreePart getCodeTreeForSymbol(String value)
                        throws NoSuchSymbolException
        {
            for (int i = symbolTables.size() - 1; i >= 0; i--)
            {
                SymbolTable table = symbolTables.get(i);
                if (table.getSymbols().containsKey(value))
                {
                    return table.getSymbols().get(value);
                }
            }
            throw new NoSuchSymbolException(value);
        }
        
        private ArrayList<SymbolTable> symbolTables;
        
        public static class NoSuchSymbolException extends Exception
        {
            public NoSuchSymbolException(String symbol)
            {
                this.symbol = symbol;
            }
            
            @Override
            public String getMessage()
            {
                return String.format("The symbol '%s' does not exist.", symbol);
            }
            
            private String symbol;
        }
    }
    
    public static enum ReservedWord
    {
        let("let", null), letStar("let*", null), define("define",
                        new Applicator()
                        {
                            
                            @Override
                            public String apply(Interpreter i, Blank node)
                            {
                                if (node.getLeft() instanceof CodeTree)
                                {
                                    node.setLeft(i.consolidate((CodeTree) node
                                                    .getLeft()));
                                }
                                String symbolName = ((Node) node.getLeft())
                                                .getValue();
                                TreePart symbolTree = node.getRight().getLeft();
                                i.getSymbolTableStack().addSymbolAtBase(
                                                symbolName, symbolTree);
                                // System.out.printf(
                                // "Adding '%s' to the symTable:\n",
                                // symbolName);
                                // System.out.println(symbolTree.toString(0));
                                return String.format("[%s]", ((Node) node
                                                .getLeft()).getValue());
                            }
                        }), plus("+", new Applicator()
        {
            
            @Override
            public String apply(Interpreter i, Blank node)
            {
                if (node.getLeft() instanceof CodeTree)
                {
                    node.setLeft(i.consolidate((CodeTree) node.getLeft()));
                }
                double value = Double.parseDouble(((Node) node.getLeft())
                                .getValue());
                // System.out.println("Value: " + value);
                while (node.getRight() != null)
                {
                    node = node.getRight();
                    if (node.getLeft() instanceof CodeTree)
                    {
                        node.setLeft(i.consolidate((CodeTree) node.getLeft()));
                    }
                    value += Double.parseDouble(((Node) node.getLeft())
                                    .getValue());
                }
                return String.format("%f", value);
            }
        }), minus("-", new Applicator()
        {
            @Override
            public String apply(Interpreter i, Blank node)
            {
                if (node.getLeft() instanceof CodeTree)
                {
                    node.setLeft(i.consolidate((CodeTree) node.getLeft()));
                }
                double value = Double.parseDouble(((Node) node.getLeft())
                                .getValue());
                // System.out.println("Value: " - value);
                while (node.getRight() != null)
                {
                    node = node.getRight();
                    if (node.getLeft() instanceof CodeTree)
                    {
                        node.setLeft(i.consolidate((CodeTree) node.getLeft()));
                    }
                    value -= Double.parseDouble(((Node) node.getLeft())
                                    .getValue());
                }
                return String.format("%f", value);
            }
        }), times("*", new Applicator()
        {
            @Override
            public String apply(Interpreter i, Blank node)
            {
                if (node.getLeft() instanceof CodeTree)
                {
                    node.setLeft(i.consolidate((CodeTree) node.getLeft()));
                }
                double value = Double.parseDouble(((Node) node.getLeft())
                                .getValue());
                // System.out.println("Value: " - value);
                while (node.getRight() != null)
                {
                    node = node.getRight();
                    if (node.getLeft() instanceof CodeTree)
                    {
                        node.setLeft(i.consolidate((CodeTree) node.getLeft()));
                    }
                    value *= Double.parseDouble(((Node) node.getLeft())
                                    .getValue());
                }
                return String.format("%f", value);
            }
        }), divide("/", new Applicator()
        {
            @Override
            public String apply(Interpreter i, Blank node)
            {
                if (node.getLeft() instanceof CodeTree)
                {
                    node.setLeft(i.consolidate((CodeTree) node.getLeft()));
                }
                double value = Double.parseDouble(((Node) node.getLeft())
                                .getValue());
                // System.out.println("Value: " / value);
                while (node.getRight() != null)
                {
                    node = node.getRight();
                    if (node.getLeft() instanceof CodeTree)
                    {
                        node.setLeft(i.consolidate((CodeTree) node.getLeft()));
                    }
                    value /= Double.parseDouble(((Node) node.getLeft())
                                    .getValue());
                }
                return String.format("%f", value);
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
        
        public static ReservedWord getWord(String s)
        {
            for (ReservedWord rw : ReservedWord.values())
            {
                if (rw.word.equals(s))
                {
                    return rw;
                }
            }
            throw new IllegalArgumentException();
        }
        
        private String     word;
        private Applicator applicator;
        
        private interface Applicator
        {
            public String apply(Interpreter i, Blank node);
        }
    }
    
    public static class Lambda extends Node
    {
        public Lambda(CodeTree args, CodeTree function)
        {
            super("", Type.Lambda);
            this.args = args;
            this.function = function;
        }
        
        public String applyTo(Interpreter i, Blank node)
        {
            // System.out.println("Lambda applied to:");
            // System.out.println(node.toString(0));
            SymbolTable s = new SymbolTable();
            if (args.getLeft() != null)
            {
                String symbolName = ((Node) args.getLeft()).getValue();
                TreePart symbolValue = node.getLeft();
                s.getSymbols().put(symbolName, symbolValue);
                Blank argsRight = args.getRight();
                // System.out.println("ARGS RIGHT");
                // System.out.println(args.getRight());
                node = node.getRight();
                while (argsRight != null)
                {
                    // System.out.println("ARGSRIGHT not null");
                    symbolName = ((Node) argsRight.getLeft()).getValue();
                    symbolValue = node.getLeft();
                    // System.out.printf("Name: %s\n", symbolName);
                    s.getSymbols().put(symbolName, symbolValue);
                    argsRight = argsRight.getRight();
                    node = node.getRight();
                }
            }
            i.symbolTableStack.addSymbolTable(s);
            // System.out.println("SymTable added:");
            // System.out.println(s.getSymbols());
            String output = i.interpret(function);
            // System.out.printf("Lambda resolved: %s\n", output);
            i.symbolTableStack.removeSymbolTable(s);
            return output;
        }
        
        private CodeTree args;
        private CodeTree function;
    }
}

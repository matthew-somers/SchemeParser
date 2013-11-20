package backend;

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
        System.out.println(">> " + interpret(iCode.getCodeTree()).toString(0));
    }
    
    private TreePart interpret(CodeTree tree)
    {
        TreePart output = new Node("ERROR", Type.Word);
        // // System.out.println("Interpreting the following:");
        // // System.out.println(tree.toString(0));
        if(tree.getLeft() instanceof CodeTree)
        {
            tree.setLeft(consolidate((CodeTree) tree.getLeft()));
        }
        Node car = (Node) tree.getLeft();
        switch (car.getType())
        {
            case Number:
                System.out.printf("Attempt to apply non-procedure '%s'.\n", car.getValue());
                try
                {
                    throw new Exception();
                } catch (Exception e1)
                {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            break;
            case ReservedWord:
                // // System.out.println("ReservedWord!");
                output = ReservedWord.getWord(car.getValue()).applyTo(this, resolve(tree.getRight()));
            break;
            case Symbol:
                if(car.getValue().equals("'"))
                {
                    output = ReservedWord.getWord("quote").applyTo(this, resolve(tree.getRight()));
                } else
                {
                    System.out.printf("Attempt to apply non-procedure '%s'.\n", car.getValue());
                }
            break;
            case Word:
                // // System.out.println("Word!");
                TreePart wordTree = null;
                try
                {
                    wordTree = symbolTableStack.getCodeTreeForSymbol(car.getValue());
                } catch (NoSuchSymbolException e)
                {
                    return new Node(e.getMessage(), Type.Word);
                }
                // // System.out.printf("Tied to code tree:\n%s\n",
                // wordTree.toString(4));
                CodeTree superTree = new CodeTree();
                superTree.setLeft(wordTree);
                superTree.setRight(tree.getRight());
                output = interpret(superTree);
            break;
            case Lambda:
                // // System.out.println("Lambda!");
                output = ((Lambda) car).applyTo(this, resolve(tree.getRight()));
            break;
        }
        return output;
    }
    
    public Node consolidate(CodeTree tree)
    {
        // // System.out.println("Consolidate!");
        // // System.out.println(tree.toString(0));
        if(tree.getLeft() instanceof CodeTree)
        {
            tree.setLeft(consolidate((CodeTree) tree.getLeft()));
        }
        Node left = (Node) tree.getLeft();
        if(left.getValue().equals("lambda"))
        {
            return new Lambda((CodeTree) tree.getRight().getLeft(), (CodeTree) tree.getRight().getRight().getLeft());
        } else
        {
            TreePart content = interpret(tree);
            return TreePart.getNode(content, this);
            /*
             * return new Node( content,
             * content.matches(Token.Type.Number.getFormat()) ?
             * Token.Type.Number : (content.matches(Token.Type.ReservedWord
             * .getFormat()) ? Token.Type.ReservedWord : Token.Type.Word));
             */
        }
    }
    
    private Blank resolve(Blank node)
    {
        // // System.out.println("Resolving!");
        // // System.out.println(node.toString(0));
        if(node.getLeft() instanceof Node)
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
                        node.setLeft(this.symbolTableStack.getCodeTreeForSymbol(((Node) node.getLeft()).getValue()));
                        // // System.out.printf("'%s' was substituted out.\n",
                        // old);
                        // // System.out.println("Replaced by:");
                        // // System.out.println(node.getLeft().toString(0));
                    } catch (NoSuchSymbolException e)
                    {
                    }
                break;
            }
        }
        if(node.getRight() != null)
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
        
        public TreePart getCodeTreeForSymbol(String value) throws NoSuchSymbolException
        {
            for (int i = symbolTables.size() - 1; i >= 0; i--)
            {
                SymbolTable table = symbolTables.get(i);
                // // System.out.printf("Checking for '%s' on level '%d.'\n",
                // value,
                // i);
                if(table.getSymbols().containsKey(value))
                {
                    /*
                     * // System.out.printf(
                     * "Symbol '%s' found on level '%d' (%s)\n", value, i,
                     * table.getSymbols().get(value) instanceof Node ? ((Node)
                     * table .getSymbols().get(value)) .getValue() :
                     * "CodeTree");
                     */
                    TreePart clone = table.getSymbols().get(value).clone();
                    if(clone instanceof CodeTree)
                    {
                        if(((CodeTree) clone).getLeft() instanceof Node)
                        {
                            if(((Node) ((CodeTree) clone).getLeft()).getValue().equals("lambda"))
                            {
                                lastLambdaLookup = value;
                            }
                        }
                    }
                    return clone;
                }
            }
            // System.out.printf("No such symbol: '%s'\n", value);
            throw new NoSuchSymbolException(value);
        }
        
        @Override
        public String toString()
        {
            String output = "SymbolTableStack:\n";
            for (SymbolTable s : symbolTables)
            {
                output = String.format("%s\t%s\n", output, s.toString());
            }
            return output;
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
        
        public String getLastLambdaLookup()
        {
            return lastLambdaLookup;
        }
        
        private String lastLambdaLookup = "";
    }
    
    public static enum ReservedWord
    {
        let("let", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                CodeTree definitions = (CodeTree) node.getLeft();
                SymbolTable s = new SymbolTable();
                
                while (definitions != null && definitions.getLeft() != null)
                {
                    CodeTree definition = (CodeTree) definitions.getLeft();
                    String symName = ((Node) definition.getLeft()).getValue();
                    TreePart symVal = definition.getRight().getLeft();
                    s.getSymbols().put(symName, symVal);
                    definitions = definitions.getRight();
                }
                
                i.symbolTableStack.addSymbolTable(s);
                TreePart output;
                if(node.getRight().getLeft() instanceof CodeTree)
                {
                    output = i.interpret((CodeTree) node.getRight().getLeft());
                } else
                {
                    switch (((Node) node.getRight().getLeft()).getType())
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
                                String old = ((Node) node.getRight().getLeft()).getValue();
                                node.getRight().setLeft(
                                                i.symbolTableStack.getCodeTreeForSymbol(((Node) node.getRight()
                                                                .getLeft()).getValue()));
                                // //
                                // System.out.printf("'%s' was substituted out.\n",
                                // old);
                                // // System.out.println("Replaced by:");
                                node.getLeft().toString(0);
                            } catch (NoSuchSymbolException e)
                            {
                            }
                        break;
                    }
                    output = TreePart.getNode(node.getRight().getLeft(), i);
                }
                i.symbolTableStack.removeSymbolTable(s);
                return output;
            }
        }), letStar("let*", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                CodeTree definitions = (CodeTree) node.getLeft();
                SymbolTable s = new SymbolTable();
                
                while (definitions != null && definitions.getLeft() != null)
                {
                    CodeTree definition = (CodeTree) definitions.getLeft();
                    String symName = ((Node) definition.getLeft()).getValue();
                    TreePart symVal = definition.getRight().getLeft();
                    s.getSymbols().put(symName, symVal);
                    definitions = definitions.getRight();
                }
                
                i.symbolTableStack.addSymbolTable(s);
                TreePart output;
                if(node.getRight().getLeft() instanceof CodeTree)
                {
                    // System.out.println("letStar: codetree");
                    output = i.interpret((CodeTree) node.getRight().getLeft());
                    // System.out.println("end");
                } else
                {
                    switch (((Node) node.getRight().getLeft()).getType())
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
                                // String old = ((Node)
                                // node.getRight().getLeft())
                                // .getValue();
                                node.getRight().setLeft(
                                                i.symbolTableStack.getCodeTreeForSymbol(((Node) node.getRight()
                                                                .getLeft()).getValue()));
                                // //
                                // System.out.printf("'%s' was substituted out.\n",
                                // old);
                                // // System.out.println("Replaced by:");
                                // node.getLeft().toString(0);
                            } catch (NoSuchSymbolException e)
                            {
                            }
                        break;
                    }
                    output = TreePart.getNode(node.getRight().getLeft(), i);
                }
                i.symbolTableStack.removeSymbolTable(s);
                return output;
            }
        }), letRec("letrec", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                CodeTree definitions = (CodeTree) node.getLeft();
                SymbolTable s = new SymbolTable();
                
                while (definitions != null && definitions.getLeft() != null)
                {
                    CodeTree definition = (CodeTree) definitions.getLeft();
                    String symName = ((Node) definition.getLeft()).getValue();
                    TreePart symVal = definition.getRight().getLeft();
                    s.getSymbols().put(symName, symVal);
                    definitions = definitions.getRight();
                }
                
                i.symbolTableStack.addSymbolTable(s);
                TreePart output;
                if(node.getRight().getLeft() instanceof CodeTree)
                {
                    output = i.interpret((CodeTree) node.getRight().getLeft());
                } else
                {
                    switch (((Node) node.getRight().getLeft()).getType())
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
                                String old = ((Node) node.getRight().getLeft()).getValue();
                                node.getRight().setLeft(
                                                i.symbolTableStack.getCodeTreeForSymbol(((Node) node.getRight()
                                                                .getLeft()).getValue()));
                                // //
                                // System.out.printf("'%s' was substituted out.\n",
                                // old);
                                // // System.out.println("Replaced by:");
                                node.getLeft().toString(0);
                            } catch (NoSuchSymbolException e)
                            {
                            }
                        break;
                    }
                    output = TreePart.getNode(node.getRight().getLeft(), i);
                }
                i.symbolTableStack.removeSymbolTable(s);
                return output;
            }
        }), define("define", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                if(node.getLeft() instanceof CodeTree)
                {
                    node.setLeft(i.consolidate((CodeTree) node.getLeft()));
                }
                String symbolName = ((Node) node.getLeft()).getValue();
                TreePart symbolTree = node.getRight().getLeft();
                if(symbolTree instanceof CodeTree)
                {
                    if(((CodeTree) symbolTree).getLeft() instanceof Node)
                    {
                        if(!((Node) ((CodeTree) symbolTree).getLeft()).getValue().equals("lambda")
                                        && !((Node) ((CodeTree) symbolTree).getLeft()).getValue().equals("quote"))
                        {
                            symbolTree = i.interpret((CodeTree) symbolTree);
                        }
                    }
                }
                i.getSymbolTableStack().addSymbolAtBase(symbolName, symbolTree);
                // // System.out.printf(
                // "Adding '%s' to the symTable:\n",
                // symbolName);
                // // System.out.println(symbolTree.toString(0));
                return new Node(String.format("[%s]", ((Node) node.getLeft()).getValue()), Type.Word);
            }
        }), plus("+", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                if(node.getLeft() instanceof CodeTree)
                {
                    node.setLeft(i.consolidate((CodeTree) node.getLeft()));
                }
                double value = Double.parseDouble(((Node) node.getLeft()).getValue());
                // // System.out.println("Value: " + value);
                while (node.getRight() != null)
                {
                    node = node.getRight();
                    if(node.getLeft() instanceof CodeTree)
                    {
                        node.setLeft(i.consolidate((CodeTree) node.getLeft()));
                    }
                    value += Double.parseDouble(((Node) node.getLeft()).getValue());
                }
                return new Node(String.format("%f", value), Type.Number);
            }
        }), minus("-", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                if(node.getLeft() instanceof CodeTree)
                {
                    node.setLeft(i.consolidate((CodeTree) node.getLeft()));
                }
                double value = Double.parseDouble(((Node) node.getLeft()).getValue());
                // // System.out.println("Value: " - value);
                while (node.getRight() != null)
                {
                    node = node.getRight();
                    if(node.getLeft() instanceof CodeTree)
                    {
                        node.setLeft(i.consolidate((CodeTree) node.getLeft()));
                    }
                    value -= Double.parseDouble(((Node) node.getLeft()).getValue());
                }
                return new Node(String.format("%f", value), Type.Number);
            }
        }), times("*", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                if(node.getLeft() instanceof CodeTree)
                {
                    node.setLeft(i.consolidate((CodeTree) node.getLeft()));
                }
                double value = Double.parseDouble(((Node) node.getLeft()).getValue());
                // // System.out.println("Value: " - value);
                while (node.getRight() != null)
                {
                    node = node.getRight();
                    if(node.getLeft() instanceof CodeTree)
                    {
                        node.setLeft(i.consolidate((CodeTree) node.getLeft()));
                    }
                    value *= Double.parseDouble(((Node) node.getLeft()).getValue());
                }
                return new Node(String.format("%f", value), Type.Number);
            }
        }), divide("/", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                if(node.getLeft() instanceof CodeTree)
                {
                    node.setLeft(i.consolidate((CodeTree) node.getLeft()));
                }
                double value = Double.parseDouble(((Node) node.getLeft()).getValue());
                // // System.out.println("Value: " / value);
                while (node.getRight() != null)
                {
                    node = node.getRight();
                    if(node.getLeft() instanceof CodeTree)
                    {
                        node.setLeft(i.consolidate((CodeTree) node.getLeft()));
                    }
                    value /= Double.parseDouble(((Node) node.getLeft()).getValue());
                }
                return new Node(String.format("%f", value), Type.Number);
            }
        }), conditional("if", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                TreePart conditional = node.getLeft();
                TreePart ifTrue = node.getRight().getLeft();
                TreePart ifFalse = node.getRight().getRight().getLeft();
                
                Node conditionalPart;
                if(conditional instanceof CodeTree)
                {
                    conditionalPart = i.consolidate((CodeTree) conditional);
                } else
                {
                    conditionalPart = (Node) conditional;
                }
                TreePart value;
                if(conditionalPart.getValue().equals("#t"))
                {
                    value = ifTrue;
                } else if(conditionalPart.getValue().equals("#f"))
                {
                    value = ifFalse;
                } else
                {
                    return new Node(String.format("'%s' is not a boolean value.", conditionalPart.getValue()),
                                    Type.Word);
                }
                
                TreePart returnValue;
                if(value instanceof CodeTree)
                {
                    returnValue = i.interpret((CodeTree) value);
                } else
                {
                    returnValue = (Node) value;
                }
                
                return returnValue;
            }
        }), equals("=", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                // // System.out.printf("Equals: %s\n", node.toString(2));
                TreePart first = node.getLeft();
                TreePart second = node.getRight().getLeft();
                Node arg1 = first instanceof CodeTree ? i.consolidate((CodeTree) first) : (Node) first;
                Node arg2 = first instanceof CodeTree ? i.consolidate((CodeTree) second) : (Node) second;
                // // System.out.printf("(= '%s' '%s')\n", arg1.getValue(),
                // arg2.getValue());
                if(Double.parseDouble(arg1.getValue()) == Double.parseDouble(arg2.getValue()))
                {
                    return new Node("#t", Type.Symbol);
                }
                return new Node("#f", Type.Symbol);
            }
        }), lessthan("<", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                // // System.out.printf("Equals: %s\n", node.toString(2));
                TreePart first = node.getLeft();
                TreePart second = node.getRight().getLeft();
                Node arg1 = first instanceof CodeTree ? i.consolidate((CodeTree) first) : (Node) first;
                Node arg2 = first instanceof CodeTree ? i.consolidate((CodeTree) second) : (Node) second;
                // // System.out.printf("(= '%s' '%s')\n", arg1.getValue(),
                // arg2.getValue());
                if(Double.parseDouble(arg1.getValue()) < Double.parseDouble(arg2.getValue()))
                {
                    return new Node("#t", Type.Symbol);
                }
                return new Node("#f", Type.Symbol);
            }
        }), lessthanequalto("<=", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                // // System.out.printf("Equals: %s\n", node.toString(2));
                TreePart first = node.getLeft();
                TreePart second = node.getRight().getLeft();
                Node arg1 = first instanceof CodeTree ? i.consolidate((CodeTree) first) : (Node) first;
                Node arg2 = first instanceof CodeTree ? i.consolidate((CodeTree) second) : (Node) second;
                // // System.out.printf("(= '%s' '%s')\n", arg1.getValue(),
                // arg2.getValue());
                if(Double.parseDouble(arg1.getValue()) <= Double.parseDouble(arg2.getValue()))
                {
                    return new Node("#t", Type.Symbol);
                }
                return new Node("#f", Type.Symbol);
            }
        }), greaterthan(">", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                // // System.out.printf("Equals: %s\n", node.toString(2));
                TreePart first = node.getLeft();
                TreePart second = node.getRight().getLeft();
                Node arg1 = first instanceof CodeTree ? i.consolidate((CodeTree) first) : (Node) first;
                Node arg2 = first instanceof CodeTree ? i.consolidate((CodeTree) second) : (Node) second;
                // // System.out.printf("(= '%s' '%s')\n", arg1.getValue(),
                // arg2.getValue());
                if(Double.parseDouble(arg1.getValue()) > Double.parseDouble(arg2.getValue()))
                {
                    return new Node("#t", Type.Symbol);
                }
                return new Node("#f", Type.Symbol);
            }
        }), greaterthanequalto(">=", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                // // System.out.printf("Equals: %s\n", node.toString(2));
                TreePart first = node.getLeft();
                TreePart second = node.getRight().getLeft();
                Node arg1 = first instanceof CodeTree ? i.consolidate((CodeTree) first) : (Node) first;
                Node arg2 = first instanceof CodeTree ? i.consolidate((CodeTree) second) : (Node) second;
                // // System.out.printf("(= '%s' '%s')\n", arg1.getValue(),
                // arg2.getValue());
                if(Double.parseDouble(arg1.getValue()) >= Double.parseDouble(arg2.getValue()))
                {
                    return new Node("#t", Type.Symbol);
                }
                return new Node("#f", Type.Symbol);
            }
        }), car("car", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                TreePart output = i.interpret((CodeTree) (node.getLeft()));
                return ((CodeTree) output).getLeft();
            }
        }), cdr("cdr", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                node = ((CodeTree) (i.interpret((CodeTree) node.getLeft()))).getRight();
                CodeTree cdr = new CodeTree();
                cdr.setLeft(node != null ? node.getLeft() : null);
                cdr.setRight(node != null ? node.getRight() : null);
                return cdr;
            }
        }), caar("caar", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                TreePart car1 = ReservedWord.car.getApplicator().apply(i, node);
                Blank holder = new Blank();
                CodeTree top = new CodeTree();
                holder.setLeft(top);
                top.setLeft(new Node("quote", Type.ReservedWord));
                Blank subHolder = new Blank();
                subHolder.setLeft(car1);
                top.setRight(subHolder);
                TreePart car2 = ReservedWord.car.getApplicator().apply(i, holder);
                return car2;
            }
        }), cddr("cddr", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                TreePart car1 = ReservedWord.cdr.getApplicator().apply(i, node);
                Blank holder = new Blank();
                CodeTree top = new CodeTree();
                holder.setLeft(top);
                top.setLeft(new Node("quote", Type.ReservedWord));
                Blank subHolder = new Blank();
                subHolder.setLeft(car1);
                top.setRight(subHolder);
                TreePart car2 = ReservedWord.cdr.getApplicator().apply(i, holder);
                return car2;
            }
        }), cadr("cadr", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                TreePart car1 = ReservedWord.cdr.getApplicator().apply(i, node);
                Blank holder = new Blank();
                CodeTree top = new CodeTree();
                holder.setLeft(top);
                top.setLeft(new Node("quote", Type.ReservedWord));
                Blank subHolder = new Blank();
                subHolder.setLeft(car1);
                top.setRight(subHolder);
                TreePart car2 = ReservedWord.car.getApplicator().apply(i, holder);
                return car2;
            }
        }), cdar("cdar", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                TreePart car1 = ReservedWord.car.getApplicator().apply(i, node);
                Blank holder = new Blank();
                CodeTree top = new CodeTree();
                holder.setLeft(top);
                top.setLeft(new Node("quote", Type.ReservedWord));
                Blank subHolder = new Blank();
                subHolder.setLeft(car1);
                top.setRight(subHolder);
                TreePart car2 = ReservedWord.cdr.getApplicator().apply(i, holder);
                return car2;
            }
        }), quote("quote", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                return node.getLeft();
            }
        }), isNull("null?", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                // // System.out.println("Null?");
                TreePart arg = node.getLeft();
                if(arg instanceof Node)
                {
                    return new Node("#f", Type.Word);
                }
                try
                {
                    arg = i.interpret((CodeTree) arg);
                } catch (NullPointerException e)
                {
                    return new Node("#t", Type.Word);
                }
                if(arg instanceof Node)
                {
                    return new Node("#f", Type.Word);
                }
                CodeTree tree = (CodeTree) arg;
                if(tree.getLeft() == null)
                {
                    return new Node("#t", Type.Word);
                }
                return new Node("#f", Type.Word);
            }
        }), cond("cond", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                while (node != null)
                {
                    CodeTree currentElement = (CodeTree) node.getLeft();
                    Node left = TreePart.getNode(currentElement.getLeft(), i);
                    if(left.getValue().equals("#t") || left.getValue().equals("else"))
                    {
                        TreePart returnTree = currentElement.getRight().getLeft();
                        if(returnTree instanceof CodeTree)
                        {
                            returnTree = i.interpret((CodeTree) returnTree);
                        }
                        if(returnTree instanceof Node)
                        {
                            Node rT = (Node) returnTree;
                            if(rT.getType() == Type.Word)
                            {
                                try
                                {
                                    returnTree = i.interpret((CodeTree) i.getSymbolTableStack().getCodeTreeForSymbol(
                                                    rT.getValue()));
                                } catch (NoSuchSymbolException e)
                                {
                                    // System.out.println("Probable error at line 880");
                                    // e.printStackTrace();
                                }
                            }
                        }
                        return returnTree;
                    }
                    node = node.getRight();
                }
                return null;
            }
        }), areEqual("equal?", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                TreePart arg1 = node.getLeft();
                TreePart arg2 = node.getRight().getLeft();
                if(arg1 instanceof CodeTree)
                {
                    arg1 = i.interpret((CodeTree) arg1);
                }
                if(arg2 instanceof CodeTree)
                {
                    arg2 = i.interpret((CodeTree) arg2);
                }
                if(arg1 instanceof Node)
                {
                    if(arg2 instanceof Node)
                    {
                        if(((Node) arg1).getValue().equals(((Node) arg2).getValue()))
                        {
                            return new Node("#t", Type.Word);
                        }
                    }
                } else if(arg1 instanceof CodeTree)
                {
                    if(arg2 instanceof CodeTree)
                    {
                        if(compareTrees((CodeTree) arg1, (CodeTree) arg2))
                        {
                            return new Node("#t", Type.Word);
                        }
                    }
                }
                return new Node("#f", Type.Word);
            }
            
            private boolean compareTrees(CodeTree arg1, CodeTree arg2)
            {
                TreePart left1 = arg1.getLeft();
                TreePart left2 = arg2.getLeft();
                if(left1 instanceof Node)
                {
                    if(left2 instanceof Node)
                    {
                        if(((Node) left1).getValue().equals(((Node) left2).getValue()))
                        {
                            return true;
                        }
                    }
                }
                if(left1 instanceof CodeTree)
                {
                    if(left2 instanceof CodeTree)
                    {
                        if(compareTrees((CodeTree) left1, (CodeTree) left2))
                        {
                            Blank right1 = arg1.getRight();
                            Blank right2 = arg2.getRight();
                            if(right1 != null)
                            {
                                if(right2 != null)
                                {
                                    CodeTree r1 = new CodeTree();
                                    r1.setLeft(right1.getLeft());
                                    r1.setRight(right1.getRight());
                                    CodeTree r2 = new CodeTree();
                                    r2.setLeft(right2.getLeft());
                                    r2.setRight(right2.getRight());
                                    return compareTrees(r1, r2);
                                }
                            } else
                            {
                                if(right2 == null)
                                {
                                    return true;
                                }
                            }
                        }
                    }
                }
                return false;
            }
        }), and("and", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                // // System.out.println("And");
                // // System.out.println(node.toString(0));
                while (node != null)
                {
                    TreePart current = node.getLeft();
                    // // System.out.println(current.toString(0));
                    // // System.out.println(current instanceof Node);
                    /*
                     * if (current instanceof CodeTree) { // //
                     * System.out.println("TestInterpret!"); // //
                     * System.out.println(i.interpret((CodeTree) current)
                     * .toString(0)); }
                     */
                    Node currentNode = (Node) (current instanceof Node ? current : i.interpret((CodeTree) current));
                    if(!currentNode.getValue().equals("#t"))
                    {
                        return new Node("#f", Type.Word);
                    }
                    node = node.getRight();
                }
                return new Node("#t", Type.Word);
            }
        }), cons("cons", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                // // System.out.println("Cons: ");
                TreePart arg1 = node.getLeft();
                TreePart arg2 = node.getRight().getLeft();
                // // System.out.println(arg1.toString(0));
                // // System.out.println(arg2.toString(0));
                if(arg1 instanceof CodeTree)
                {
                    arg1 = i.interpret((CodeTree) arg1);
                }
                if(arg2 instanceof CodeTree)
                {
                    arg2 = i.interpret((CodeTree) arg2);
                }
                // // System.out.println(arg1.toString(0));
                // // System.out.println(arg2.toString(0));
                CodeTree tree = new CodeTree();
                tree.setLeft(arg1);
                tree.setRight(new Blank());
                tree.getRight().setLeft(((CodeTree) arg2).getLeft());
                tree.getRight().setRight(((CodeTree) arg2).getRight());
                return tree;
            }
        }), append("append", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                // // System.out.println("Append: ");
                // // System.out.println(node.toString(0));
                TreePart arg1 = node.getLeft();
                TreePart arg2 = node.getRight().getLeft();
                if(arg1 instanceof CodeTree)
                {
                    arg1 = i.interpret((CodeTree) arg1);
                }
                if(arg2 instanceof CodeTree)
                {
                    arg2 = i.interpret((CodeTree) arg2);
                }
                CodeTree tree = new CodeTree();
                CodeTree first = (CodeTree) arg1;
                CodeTree second = (CodeTree) arg2;
                if(first.getLeft() != null)
                {
                    tree.setLeft(first.getLeft());
                    tree.setRight(first.getRight());
                    Blank b = tree.getRight();
                    if(b == null)
                    {
                        if(second.getLeft() != null)
                        {
                            tree.setRight(new Blank());
                            tree.getRight().setLeft(second.getLeft());
                            tree.getRight().setRight(second.getRight());
                        }
                    } else
                    {
                        while (b.getRight() != null)
                        {
                            b = b.getRight();
                        }
                        if(second.getLeft() != null)
                        {
                            b.setRight(new Blank());
                            b.getRight().setLeft(second.getLeft());
                            b.getRight().setRight(second.getRight());
                        }
                    }
                } else if(second.getLeft() != null)
                {
                    tree.setLeft(second.getLeft());
                    tree.setRight(second.getRight());
                }
                return tree;
            }
        }), list("list", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                CodeTree tree = new CodeTree();
                if(node != null)
                {
                    if(node.getLeft() != null)
                    {
                        tree.setLeft(node.getLeft() instanceof CodeTree ? i.interpret((CodeTree) node.getLeft()) : node
                                        .getLeft());
                    }
                    node = node.getRight();
                    Blank b = new Blank();
                    if(node != null)
                    {
                        tree.setRight(b);
                    }
                    while (node != null)
                    {
                        if(node.getLeft() != null)
                        {
                            b.setLeft(node.getLeft() instanceof CodeTree ? i.interpret((CodeTree) node.getLeft())
                                            : node.getLeft());
                        }
                        node = node.getRight();
                        if(node != null)
                        {
                            b.setRight(new Blank());
                            b = b.getRight();
                        }
                    }
                }
                return tree;
            }
        }), not("not", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                TreePart arg1 = node.getLeft();
                if(arg1 instanceof CodeTree)
                {
                    arg1 = i.interpret((CodeTree) arg1);
                }
                if(arg1 instanceof Node)
                {
                    if(((Node) arg1).getValue().equals("#f"))
                    {
                        return new Node("#t", Type.Word);
                    }
                }
                return new Node("#f", Type.Word);
            }
        }), or("or", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                while (node != null)
                {
                    TreePart current = node.getLeft();
                    Node currentNode = TreePart.getNode(current, i);
                    if(currentNode.getValue().equals("#t"))
                    {
                        return new Node("#t", Type.Word);
                    }
                    node = node.getRight();
                }
                return new Node("#f", Type.Word);
            }
        }), isSymbol("symbol?", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                // // System.out.println("Symbol?");
                TreePart left = node.getLeft();
                if(left instanceof CodeTree)
                {
                    left = i.interpret((CodeTree) left);
                }
                if(left instanceof Node)
                {
                    if(((Node) left).getValue().matches("[a-zA-Z0-9]+"))
                    {
                        return new Node("#t", Type.Word);
                    }
                }
                return new Node("#f", Type.Word);
            }
        }), isInteger("integer?", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                // // System.out.println("Integer?");
                TreePart left = node.getLeft();
                if(left instanceof CodeTree)
                {
                    left = i.interpret((CodeTree) left);
                }
                if(left instanceof Node)
                {
                    try
                    {
                        Integer.parseInt(((Node) left).getValue());
                        return new Node("#t", Type.Word);
                    } catch (NumberFormatException e)
                    {
                    }
                }
                return new Node("#f", Type.Word);
            }
        }), isBoolean("boolean?", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                // // System.out.println("Boolean?");
                TreePart left = node.getLeft();
                if(left instanceof CodeTree)
                {
                    left = i.interpret((CodeTree) left);
                }
                if(left instanceof Node)
                {
                    if(((Node) left).getValue().matches("#f|#t"))
                    {
                        return new Node("#t", Type.Word);
                    }
                }
                return new Node("#f", Type.Word);
            }
        }), isChar("char?", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                // // System.out.println("Char?");
                TreePart left = node.getLeft();
                if(left instanceof CodeTree)
                {
                    left = i.interpret((CodeTree) left);
                }
                if(left instanceof Node)
                {
                    if(((Node) left).getValue().matches("#\\\\."))
                    {
                        return new Node("#t", Type.Word);
                    }
                }
                return new Node("#f", Type.Word);
            }
        }), isString("string?", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                // // System.out.println("String?");
                TreePart left = node.getLeft();
                if(left instanceof CodeTree)
                {
                    left = i.interpret((CodeTree) left);
                }
                if(left instanceof Node)
                {
                    if(((Node) left).getValue().matches("\".*\""))
                    {
                        return new Node("#t", Type.Word);
                    }
                }
                return new Node("#f", Type.Word);
            }
        }), isReal("real?", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                // // System.out.println("Real?");
                TreePart left = node.getLeft();
                if(left instanceof CodeTree)
                {
                    left = i.interpret((CodeTree) left);
                }
                if(left instanceof Node)
                {
                    try
                    {
                        String text = ((Node) left).getValue();
                        Double.parseDouble(text);
                        return new Node("#t", Type.Word);
                    } catch (NumberFormatException e)
                    {
                    }
                }
                return new Node("#f", Type.Word);
            }
        }), isPair("pair?", new Applicator() {
            @Override
            public TreePart apply(Interpreter i, Blank node)
            {
                // // System.out.println("Pair?");
                TreePart left = node.getLeft();
                if(left instanceof CodeTree)
                {
                    left = i.interpret((CodeTree) left);
                }
                if(left instanceof CodeTree)
                {
                    if(((CodeTree) left).getLeft() != null)
                    {
                        return new Node("#t", Type.Word);
                    }
                }
                return new Node("#f", Type.Word);
            }
        });
        
        private ReservedWord(String word, Applicator applicator)
        {
            this.word = word;
            this.applicator = applicator;
        }
        
        public TreePart applyTo(Interpreter i, Blank node)
        {
            return applicator.apply(i, node);
        }
        
        public Applicator getApplicator()
        {
            return applicator;
        }
        
        public static ReservedWord getWord(String s)
        {
            for (ReservedWord rw : ReservedWord.values())
            {
                if(rw.word.equals(s))
                {
                    // // System.out.printf("Acting on reserved word: '%s'\n",
                    // s);
                    return rw;
                }
            }
            // System.out.printf("Illegal reserved word: '%s'\n", s);
            throw new IllegalArgumentException();
        }
        
        private String     word;
        private Applicator applicator;
        
        private interface Applicator
        {
            public TreePart apply(Interpreter i, Blank node);
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
        
        public TreePart applyTo(Interpreter i, Blank node)
        {
            // // System.out.printf("Lambda (%s) applied to:\n", i
            // .getSymbolTableStack().getLastLambdaLookup());
            // // System.out.println(node.toString(0));
            SymbolTable s = new SymbolTable();
            if(args.getLeft() != null)
            {
                String symbolName = ((Node) args.getLeft()).getValue();
                TreePart symbolValue = node.getLeft() instanceof Node ? (Node) node.getLeft() : i
                                .interpret((CodeTree) node.getLeft());
                if(symbolValue instanceof CodeTree)
                {
                    CodeTree tree = new CodeTree();
                    tree.setLeft(new Node("quote", Type.ReservedWord));
                    tree.setRight(new Blank());
                    tree.getRight().setLeft(symbolValue);
                    symbolValue = tree;
                }
                // // System.out.printf("Name: %s\n%s\n", symbolName,
                // symbolValue.toString(0));
                s.getSymbols().put(symbolName, symbolValue);
                Blank argsRight = args.getRight();
                // // System.out.println("ARGS RIGHT");
                // // System.out.println(args.getRight());
                node = node.getRight();
                while (argsRight != null)
                {
                    // // System.out.println("ARGSRIGHT not null");
                    symbolName = ((Node) argsRight.getLeft()).getValue();
                    symbolValue = node.getLeft() instanceof Node ? (Node) node.getLeft() : i.interpret((CodeTree) node
                                    .getLeft());
                    if(symbolValue instanceof CodeTree)
                    {
                        CodeTree tree = new CodeTree();
                        tree.setLeft(new Node("quote", Type.ReservedWord));
                        tree.setRight(new Blank());
                        tree.getRight().setLeft(symbolValue);
                        symbolValue = tree;
                    }
                    // // System.out.printf("Name: %s\n%s\n", symbolName,
                    // symbolValue.toString(0));
                    s.getSymbols().put(symbolName, symbolValue);
                    argsRight = argsRight.getRight();
                    node = node.getRight();
                }
            }
            i.symbolTableStack.addSymbolTable(s);
            // // System.out.println(i.symbolTableStack.toString());
            // // System.out.println("SymTable added:");
            // // System.out.println(s.getSymbols());
            TreePart output = i.interpret(function);
            // // System.out.printf("Lambda resolved: %s\n",
            // output.toString(0));
            i.symbolTableStack.removeSymbolTable(s);
            return output;
        }
        
        private CodeTree args;
        private CodeTree function;
    }
}

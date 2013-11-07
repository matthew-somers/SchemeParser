package intermediate;

import intermediate.IntermediateCode.CodeTree.Node;
import java.util.ArrayList;
import backend.Interpreter;
import frontend.Token.Type;

public class IntermediateCode
{
    private ArrayList<CodeTree> trees;
    private SymbolTable         stable;
    
    public IntermediateCode()
    {
        trees = new ArrayList<CodeTree>();
        stable = new SymbolTable();
    }
    
    public void printTrees()
    {
        for (CodeTree tree : trees)
        {
            System.out.println("\n" + tree.toString(0));
        }
        
    }
    
    public void fillSymbolTable()
    {
        stable.clear();
        for (CodeTree tree : trees)
        {
            stable.analyzeTree(tree);
        }
        
        // stable.printSymbolTable();
    }
    
    public void addCodeTree(CodeTree tree)
    {
        trees.add(tree);
    }
    
    public CodeTree getCodeTree()
    {
        return trees.get(0);
    }
    
    public void reset()
    {
        trees.clear();
        stable.clear();
    }
    
    public static class CodeTree extends TreePart
    {
        protected TreePart left;
        protected Blank    right;
        
        public CodeTree()
        {
            left = null;
            right = null;
        }
        
        @Override
        public String toString(int indent)
        {
            String s = spaces(indent);
            s = s.concat("(");
            if (left != null)
            {
                s = s.concat((left instanceof CodeTree ? "\n" : "").concat(left
                                .toString(indent
                                                + (left instanceof CodeTree ? INDENT
                                                                : 0))));
            }
            if (right != null)
            {
                s = s.concat(right.toString(indent));
            }
            return s;
        }
        
        @Override
        public CodeTree clone()
        {
            CodeTree self = new CodeTree();
            if (getLeft() != null)
            {
                self.setLeft(getLeft().clone());
            }
            if (getRight() != null)
            {
                self.setRight(getRight().clone());
            }
            return self;
        }
        
        public TreePart getLeft()
        {
            return left;
        }
        
        public void setLeft(TreePart left)
        {
            this.left = left;
        }
        
        public Blank getRight()
        {
            return right;
        }
        
        public void setRight(Blank right)
        {
            this.right = right;
        }
        
        public static class Node extends TreePart
        {
            private String value;
            private Type   type;
            
            public Node(String value, Type type)
            {
                this.value = value;
                this.type = type;
            }
            
            @Override
            public Node clone()
            {
                Node self = new Node(value, type);
                return self;
            }
            
            public String getValue()
            {
                return value;
            }
            
            public Type getType()
            {
                return type;
            }
            
            @Override
            public String toString(int indent)
            {
                return value;
            }
        }
        
        public static class Blank extends CodeTree
        {
            @Override
            public String toString(int indent)
            {
                String s = " ";
                if (left != null)
                {
                    s = s.concat((left instanceof CodeTree ? "\n" : "").concat(left
                                    .toString(indent
                                                    + (left instanceof CodeTree ? INDENT
                                                                    : 0))));
                }
                if (right != null)
                {
                    s = s.concat(right.toString(indent));
                }
                else
                {
                    s = s.concat(")");
                }
                return s;
            }
            
            @Override
            public Blank clone()
            {
                Blank self = new Blank();
                if (getLeft() != null)
                {
                    self.setLeft(getLeft().clone());
                }
                if (getRight() != null)
                {
                    self.setRight(getRight().clone());
                }
                return self;
            }
        }
    }
    
    public static abstract class TreePart
    {
        public static final int INDENT = 4;
        
        public abstract String toString(int indentation);
        
        public static String spaces(int count)
        {
            String s = "";
            for (int i = 0; i < count; i++)
            {
                s = s.concat(" ");
            }
            return s;
        }
        
        @Override
        public abstract TreePart clone();
        
        public static Node getNode(TreePart t, Interpreter i)
        {
            if (t instanceof Node)
            {
                return (Node) t;
            }
            return i.consolidate((CodeTree) t);
        }
    }
}

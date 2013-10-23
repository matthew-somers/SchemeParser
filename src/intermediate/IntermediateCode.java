package intermediate;

import java.util.ArrayList;

public class IntermediateCode
{
    public IntermediateCode()
    {
        trees = new ArrayList<CodeTree>();
    }
    
    public void addCodeTree(CodeTree tree)
    {
        trees.add(tree);
    }
    
    private ArrayList<CodeTree> trees;
    
    public static class CodeTree extends TreePart
    {
        public CodeTree()
        {
            left = null;
            right = null;
        }
        
        @Override
        public String toString(int indent)
        {
            String s = spaces(indent);
            s = s.concat("Code Tree:\n");
            if (left != null)
            {
                s = s.concat(left.toString(indent + INDENT));
            }
            s = s.concat(spaces(indent).concat("----------\n"));
            if (right != null)
            {
                s = s.concat(right.toString(indent + INDENT));
            }
            return s;
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
        
        protected TreePart left;
        protected Blank    right;
        
        public static class Node extends TreePart
        {
            public Node(String value)
            {
                this.value = value;
            }
            
            @Override
            public String toString(int indent)
            {
                return spaces(indent).concat(value.concat("\n"));
            }
            
            private String value;
        }
        
        public static class Blank extends CodeTree
        {
            @Override
            public String toString(int indent)
            {
                String s = spaces(indent);
                s = s.concat("Blank\n");
                if (left != null)
                {
                    s = s.concat(left.toString(indent + INDENT));
                }
                s = s.concat(spaces(indent).concat("----------\n"));
                if (right != null)
                {
                    s = s.concat(right.toString(indent + INDENT));
                }
                return s;
            }
        }
    }
    
    public static abstract class TreePart
    {
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
        
        public static final int INDENT = 4;
    }
}

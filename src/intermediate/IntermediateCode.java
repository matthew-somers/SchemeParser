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
        public String toString()
        {
            String s = "Code Tree:\n";
            System.out.println("Printing tree.");
            if (left != null)
            {
                s = s.concat(left.toString());
            }
            else
            {
                System.out.println("Left is null.");
            }
            if (right != null)
            {
                s = s.concat(right.toString());
            }
            else
            {
                System.out.println("Right is null.");
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
            public String toString()
            {
                return value.concat("\n");
            }
            
            private String value;
        }
        
        public static class Blank extends CodeTree
        {
            @Override
            public String toString()
            {
                String s = "Blank\n";
                if (left != null)
                {
                    s = s.concat(left.toString());
                }
                else
                {
                    System.out.println("Left is null.");
                }
                if (right != null)
                {
                    s = s.concat(right.toString());
                }
                else
                {
                    System.out.println("Right is null.");
                }
                return s;
            }
        }
    }
    
    public static class TreePart
    {
    }
}

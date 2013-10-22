package frontend;

public class Token
{
    public static enum Type
    {
        Symbol("[()']", ""), Number("[0-9]", "[0-9]"), Word("[a-zA-Z\\+\\^]",
                        "[a-zA-Z0-9\\+\\-\\*\\?]"), ReservedWord(
                        "and|begin|cond|define|else|if|lambda|let|letrec|let\\*|not|or|quote|null\\?|member",
                        ""), List("", "");
        private Type(String initChars, String okChars)
        {
            this.initChars = initChars;
            this.okChars = okChars;
        }
        
        public String getInitChars()
        {
            return initChars;
        }
        
        public String getOkChars()
        {
            return okChars;
        }
        
        private String initChars;
        private String okChars;
    }
    
    public Token(Type type, String name)
    {
        this.type = type;
        this.name = name;
    }
    
    public Type getType()
    {
        return type;
    }
    
    public void setType(Type type)
    {
        this.type = type;
    }
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    @Override
    public String toString()
    {
        return String.format("Token: %12s: %s", type.toString(), name);
    }
    
    private Type   type;
    private String name;
}

package ast;

public enum CurrInstruction {
    VarDecl(""),
    VarDeclInt("int"),
    VarDeclBool("boolean"),
    VarDeclIntArray(""),
    VarDeclRef(""),
    ClassDecl(""),
    MethodDecl("");
    private final String name;
    private CurrInstruction(String name)
    {
        this.name=name;
    }

    public String getName() {
        return name;
    }
}

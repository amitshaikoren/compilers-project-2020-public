package ast;

public enum CurrInstruction {
    VarDecl(""),
    VarDeclInt("int"),
    VarDeclBool("boolean"),
    VarDeclIntArray("intArray"),
    VarDeclRef(""),
    ClassDecl(""),
    MethodDecl(""),
    Return("");
    private final String name;
    CurrInstruction(String name)
    {
        this.name=name;
    }

    public String getName() {
        return name;
    }
}

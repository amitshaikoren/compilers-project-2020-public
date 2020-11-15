package ast;

public class SymbolInfo {
    private boolean isMethod;
    private String decl;
    private String refType;

    public void setIsMethod(boolean isMethod) {
        this.isMethod = isMethod;
    }

    public void setDecl(String decl) {
        this.decl = decl;
    }

    public void setRefType(String refType){
        this.refType=refType;
    }
}

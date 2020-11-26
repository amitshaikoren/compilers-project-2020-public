package ast;

public class SymbolInfo {
    private boolean isMethod;
    private String decl;
    private String regName;

    public String getDecl() {
        return decl;
    }


    public String getRegName() {
        return regName;
    }

    public void setRegName(String regName) {
        this.regName = regName;
    }

    public String getRefType() {
        return refType;
    }

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

    public boolean getIsMethod(){
        return isMethod;
    }


}

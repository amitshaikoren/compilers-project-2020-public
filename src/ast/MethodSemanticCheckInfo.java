package ast;

import java.util.List;

public class MethodSemanticCheckInfo {
    private String name;
    private String returnType;
    private List<FormalArg> formalArgs;
    private String classDecl;

    public MethodSemanticCheckInfo(String name, String returnType, List<FormalArg> formalArgs,String classDecl) {
        this.name = name;
        this.returnType = returnType;
        this.formalArgs = formalArgs;
        this.classDecl=classDecl;
    }

    public String getClassDecl() {
        return classDecl;
    }

    public void setClassDecl(String classDecl) {
        this.classDecl = classDecl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public List<FormalArg> getFormalArgs() {
        return formalArgs;
    }

    public void setFormalArgs(List<FormalArg> formalArgs) {
        this.formalArgs = formalArgs;
    }
}
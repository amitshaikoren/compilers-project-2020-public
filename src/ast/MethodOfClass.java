package ast;

import java.util.ArrayList;

public class MethodOfClass {
    private String methodName;
    private String classAndMethod;
    private String decl;
    private ArrayList<String> formals;

    public MethodOfClass(String methodName,String classAndMethod) {
        this.methodName = methodName;
        this.classAndMethod=classAndMethod;
        this.formals=new ArrayList<>();
    }

    public String getClassAndMethod() {
        return classAndMethod;
    }

    public void setClassAndMethod(String classAndMethod) {
        this.classAndMethod = classAndMethod;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getDecl() {
        return decl;
    }

    public void setDecl(String decl) {
        this.decl = decl;
    }


    public ArrayList<String> getFormals() {
        return formals;
    }

    public void setFormals(String formals) {
        this.formals.add(formals);
    }
}

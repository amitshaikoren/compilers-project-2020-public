package ast;

public class MethodOfClass {
    private String methodName;
    private String classAndMethod;
    private String decl;

    public MethodOfClass(String methodName,String classAndMethod) {
        this.methodName = methodName;
        this.classAndMethod=classAndMethod;
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


}

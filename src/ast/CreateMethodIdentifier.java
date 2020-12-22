package ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreateMethodIdentifier implements Visitor {




    private Map<String, ArrayList<MethodOfClass>> MethodOfClasses;
    private String currClassName;
    private MethodOfClass currMethod;
    private  boolean formals;
    private boolean returnType;

    public Map<String, ArrayList<MethodOfClass>> getMethodOfClasses() {
        return MethodOfClasses;
    }

    public CreateMethodIdentifier(){
        MethodOfClasses = new HashMap<>();
    }
    @Override
    public void visit(Program program) {
        for (ClassDecl classdecl : program.classDecls()) {
            classdecl.accept(this);
        }
    }

    @Override
    public void visit(ClassDecl classDecl) {
        currClassName = classDecl.name();
        ArrayList<MethodOfClass> methods = new ArrayList<>();
        MethodOfClasses.put(classDecl.name(), methods);
        if (classDecl.superName() != null) {
            MethodOfClasses.get(currClassName).addAll(MethodOfClasses.get(classDecl.superName()));
        }
        for (var methodDecl : classDecl.methoddecls()) {
            int index = -1;
            int loopCount = 0;
            for (var p : MethodOfClasses.get(currClassName)) {
                if (p.getMethodName().equals(methodDecl.name())) {
                    index = loopCount;
                    break;
                }
                loopCount++;
            }
            if (index != -1) {
                MethodOfClasses.get(currClassName).set(index, new MethodOfClass(methodDecl.name(), classDecl.name() + "." + methodDecl.name()));
            } else//new method
            {
                MethodOfClasses.get(currClassName).add(new MethodOfClass(methodDecl.name(), classDecl.name() + "." + methodDecl.name()));
            }

        }
    }
    @Override
    public void visit(MainClass mainClass) {

    }

    @Override
    public void visit(MethodDecl methodDecl) {
        currMethod = null;
        for (var method : MethodOfClasses.get(currClassName)) {
            if (method.getMethodName().equals(methodDecl.name())) {
                currMethod = method;
                break;
            }
        }

        this.returnType=true;
        methodDecl.returnType().accept(this);
        this.returnType=false;
        this.formals = true;
        for (var formal : methodDecl.formals()) {
            formal.accept(this);
        }
        this.formals = false;
    }

    @Override
    public void visit(FormalArg formalArg) {
    formalArg.type().accept(this);

    }

    @Override
    public void visit(VarDecl varDecl) {

    }

    @Override
    public void visit(BlockStatement blockStatement) {

    }

    @Override
    public void visit(IfStatement ifStatement) {

    }

    @Override
    public void visit(WhileStatement whileStatement) {

    }

    @Override
    public void visit(SysoutStatement sysoutStatement) {

    }

    @Override
    public void visit(AssignStatement assignStatement) {

    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {

    }

    @Override
    public void visit(AndExpr e) {

    }

    @Override
    public void visit(LtExpr e) {

    }

    @Override
    public void visit(AddExpr e) {

    }

    @Override
    public void visit(SubtractExpr e) {

    }

    @Override
    public void visit(MultExpr e) {

    }

    @Override
    public void visit(ArrayAccessExpr e) {

    }

    @Override
    public void visit(ArrayLengthExpr e) {

    }

    @Override
    public void visit(MethodCallExpr e) {

    }

    @Override
    public void visit(IntegerLiteralExpr e) {

    }

    @Override
    public void visit(TrueExpr e) {

    }

    @Override
    public void visit(FalseExpr e) {

    }

    @Override
    public void visit(IdentifierExpr e) {

    }

    @Override
    public void visit(ThisExpr e) {

    }

    @Override
    public void visit(NewIntArrayExpr e) {

    }

    @Override
    public void visit(NewObjectExpr e) {

    }

    @Override
    public void visit(NotExpr e) {

    }

    @Override
    public void visit(IntAstType t) {
        if (formals) {
            currMethod.setFormals("int");
        }
        if (returnType){
            currMethod.setDecl("int");
        }
    }

    @Override
    public void visit(BoolAstType t) {
        if (formals) {
            currMethod.setFormals("bool");
        }
        if (returnType){
            currMethod.setDecl("bool");
        }
    }

    @Override
    public void visit(IntArrayAstType t) {
        if (formals) {
            currMethod.setFormals("intArr");
        }
        if (returnType){
            currMethod.setDecl("intArr");
        }
    }

    @Override
    public void visit(RefType t) {
        if (formals) {
            currMethod.setFormals(t.id());
        }
        if (returnType){
            currMethod.setDecl(t.id());
        }
    }
}

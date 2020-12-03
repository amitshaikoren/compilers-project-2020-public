package ast;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreateVtableVisitor implements Visitor {
    private StringBuilder builder;
    private int indent;
    private String currClassName;
    private Map<String, ArrayList<MethodOfClass>> funcOfClass;
    private MethodOfClass currMethod;

    public  CreateVtableVisitor()
    {
        this.indent=1;
        this.builder = new StringBuilder();
        funcOfClass=new HashMap<>();
    }

    public String getString() {
        return this.builder.toString();
    }

    private void appendWithIndent(String str) {
        this.builder.append("\t".repeat(this.indent));
        this.builder.append(str);
    }

    @Override
    public void visit(Program program) {
        for (ClassDecl classdecl : program.classDecls()) {
            classdecl.accept(this);
        }
    }

    @Override
    public void visit(ClassDecl classDecl) {
        currClassName=classDecl.name();
        ArrayList<MethodOfClass> methods=new ArrayList<>();
        funcOfClass.put(classDecl.name(),methods);
        if(classDecl.superName()!=null)
        {
            funcOfClass.get(currClassName).addAll(funcOfClass.get(classDecl.superName()));
        }
        for (var methodDecl : classDecl.methoddecls()) {
            int index=-1;
            int loopCount=0;
            for(var p : funcOfClass.get(currClassName))
            {
                if(p.getMethodName().equals(methodDecl.name()))
                {
                    index=loopCount;
                    break;
                }
                loopCount++;
            }
            if(index!=-1)
            {
                funcOfClass.get(currClassName).set(index,new MethodOfClass(methodDecl.name(),classDecl.name()+"."+methodDecl.name()));
            }
            else//new method
            {
                funcOfClass.get(currClassName).add(new MethodOfClass(methodDecl.name(),classDecl.name()+"."+methodDecl.name()));
            }
            methodDecl.accept(this);
        }
        this.builder.append("@."+classDecl.name()+"_vtable = global ["+funcOfClass.get(currClassName).size()+" x i8*][\n");
        int count=0;
        for (var method : funcOfClass.get(currClassName))
        {
            appendWithIndent(method.getDecl());
            if(count<funcOfClass.get(currClassName).size()-1)
            {
                this.builder.append(",\n");
            }
            else
            {
                this.builder.append("\n");
            }
            count++;
        }
        this.builder.append("]\n");
    }

    @Override
    public void visit(MainClass mainClass) {

    }

    @Override
    public void visit(MethodDecl methodDecl) {
        currMethod=null;
        for( var method : funcOfClass.get(currClassName)){
            if(method.getMethodName().equals(methodDecl.name()))
            {
                currMethod=method;
                break;
            }
        }
        currMethod.setDecl("i8* bitcast(");
        methodDecl.returnType().accept(this);
        currMethod.setDecl(currMethod.getDecl()+"(i8* ");
        for (var formal : methodDecl.formals())
        {
            formal.accept(this);
        }
        currMethod.setDecl(currMethod.getDecl()+")* @"+currMethod.getClassAndMethod()+" to i8*)");

    }

    @Override
    public void visit(FormalArg formalArg) {
        currMethod.setDecl(currMethod.getDecl()+", ");
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
        currMethod.setDecl(currMethod.getDecl()+"i32 ");
    }


    @Override
    public void visit(BoolAstType t) {
        currMethod.setDecl(currMethod.getDecl()+"i1 ");
    }

    @Override
    public void visit(IntArrayAstType t) {
        currMethod.setDecl(currMethod.getDecl()+"i32* ");
    }

    @Override
    public void visit(RefType t) {
//todo
    }
}

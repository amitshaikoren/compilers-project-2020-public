package ast;

import jdk.internal.net.http.common.Pair;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreateVtableVisitor implements Visitor {
    private StringBuilder builder;
    private int indent;
    private SymbolTable currSymbolTable;
    private LookupTable lookupTable;
    CurrInstruction currInstruction;
    private String currClassName;
    Map<String, ArrayList<Pair<String,String>>> funcOfClass;

    public  CreateVtableVisitor(LookupTable lookupTable)
    {
        this.lookupTable=lookupTable;
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
        ArrayList<Pair<String,String>> methods=new ArrayList<>();
        if(classDecl.superName()!=null)
        {
            methods.addAll(funcOfClass.get(classDecl.superName()));
        }
        this.builder.append("@."+classDecl.name()+"_vtable = global ["+classDecl.methoddecls().size()+" x i8*][\n");
        currClassName=classDecl.name();
        int count=0;
        for (var methodDecl : classDecl.methoddecls()) {
            int index=-1;
            int loopCount=0;
            for(var p : methods)
            {
                if(p.first.equals(methodDecl.name()))
                {
                    index=loopCount;
                    break;
                }
                loopCount++;
            }
            if(index!=-1)
            {
                methods.set(index,new Pair<>(methodDecl.name(),"@."+classDecl.name()+"."+methodDecl.name()));
            }
            else//new method
            {
                methods.add(new Pair<>(methodDecl.name(),"@."+classDecl.name()+"."+methodDecl.name()));
            }
            this.currSymbolTable=lookupTable.getSymbolTable(methodDecl);
            methodDecl.accept(this);
            if(count<classDecl.methoddecls().size()-1)
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
        funcOfClass.put(classDecl.name(),methods);
    }

    @Override
    public void visit(MainClass mainClass) {

    }

    @Override
    public void visit(MethodDecl methodDecl) {
        //funcOfClass.get(currClassName).get()
        appendWithIndent("i8* bitcast(");
        currInstruction=CurrInstruction.MethodDecl;
        methodDecl.returnType().accept(this);
        this.builder.append("(i8* ");
        for (var formal : methodDecl.formals())
        {
            this.currSymbolTable=lookupTable.getSymbolTable(formal);
            formal.accept(this);
        }
        this.builder.append(")* @"+currClassName+"."+methodDecl.name()+" to i8*)");

    }

    @Override
    public void visit(FormalArg formalArg) {
        currInstruction=CurrInstruction.MethodDecl;
        this.builder.append(", ");
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

    private void printType(String type) {
        if(type.equals("int"))
        {
            this.builder.append("i32 ");
        }
        if(type.equals("bool"))
        {
            this.builder.append("i1 ");
        }
        if(type.equals("intArr"))
        {
            this.builder.append("i32* ");
        }
        //todo: add for reftype
    }

    private void printPointerType(String type) {
        if(type.equals("int"))
        {
            this.builder.append("i32* ");
        }
        if(type.equals("bool"))
        {
            this.builder.append("i1* ");
        }
        if(type.equals("intArr"))
        {
            this.builder.append("i32** ");
        }
        //todo: add for reftype
    }
    @Override
    public void visit(IntAstType t) {
        if(this.currInstruction==currInstruction.VarDecl)
        {
            currInstruction=currInstruction.VarDeclInt;
            this.builder.append("= alloca i32");
        }
        if(this.currInstruction==currInstruction.MethodDecl)
        {
            this.builder.append("i32 ");
        }
    }


    @Override
    public void visit(BoolAstType t) {
        if(this.currInstruction==currInstruction.VarDecl)
        {
            currInstruction=currInstruction.VarDeclBool;
            this.builder.append("= alloca i1");
        }
        if(this.currInstruction==currInstruction.MethodDecl)
        {
            this.builder.append("i1 ");
        }
    }

    @Override
    public void visit(IntArrayAstType t) {
        if(this.currInstruction==currInstruction.VarDecl)
        {
            currInstruction=currInstruction.VarDeclIntArray;
            this.builder.append("= alloca i32*");
        }
        if(this.currInstruction==currInstruction.MethodDecl)
        {
            this.builder.append("i32* ");
        }
    }

    @Override
    public void visit(RefType t) {

    }
}

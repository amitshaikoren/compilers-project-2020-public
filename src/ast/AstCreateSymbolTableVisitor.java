package ast;

import java.util.List;

public class AstCreateSymbolTableVisitor implements Visitor{
    protected SymbolTable programScopeSymbolTable;//to start
    private String fieldName;
    private String fieldType;
    private String MethodName;
    private String MethodRetType;
    private SymbolTable fatherScope;
    private String Scope;
    private SymbolTable buildSymbolTable()
    {
        //create new symboltable with name this.scope and parentSymbolTable is father
        SymbolTable newScope=new SymbolTable(this.Scope,this.fatherScope);
        return newScope;
    }
    private void updateSymbolTable(SymbolTable symbolScope,boolean isMethod)
    {
        //insert field/method to symboltable scope
    }
    private SymbolTable lookup(String scopeName)
    {
        //find the symboltable in the lookup map
        return programScopeSymbolTable;//todo: change!!!
    }
    @Override
    public void visit(Program program) {
        program.mainClass().accept(this);
        for (ClassDecl classdecl : program.classDecls()) {
            classdecl.accept(this);
        }
    }

    @Override
    public void visit(ClassDecl classDecl) {
        this.Scope=classDecl.name();
        if (classDecl.superName() != null) {
            this.fatherScope=lookup(classDecl.superName());
            buildSymbolTable();
        }
        else
        {
            this.fatherScope=programScopeSymbolTable;
            buildSymbolTable();
        }
        for (var fieldDecl : classDecl.fields()) {
            fieldDecl.accept(this);
            updateSymbolTable(lookup(classDecl.name()),false);
        }
        for (var methodDecl : classDecl.methoddecls()) {
            this.fatherScope=lookup(classDecl.name());
            methodDecl.accept(this);
        }
    }

    @Override
    public void visit(MainClass mainClass) {

    }

    @Override
    public void visit(MethodDecl methodDecl) {
        methodDecl.returnType().accept(this); //the rettype sopposed to update
        this.MethodName=methodDecl.name();
        updateSymbolTable(this.fatherScope,true);
        this.Scope=methodDecl.name();
        buildSymbolTable();
        for (var formal : methodDecl.formals()) {
            formal.accept(this);//in here to update each var in his symbol scope
        }
        for (var varDecl : methodDecl.vardecls()) {
            varDecl.accept(this);
        }
        for (var stmt : methodDecl.body()) {
            stmt.accept(this);
        }
        methodDecl.ret().accept(this);
    }

    @Override
    public void visit(FormalArg formalArg) {

    }

    @Override
    public void visit(VarDecl varDecl) {
        varDecl.type().accept(this);
        this.fieldName=varDecl.name();
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
        this.fieldType="IntAstType";
    }

    @Override
    public void visit(BoolAstType t) {
        this.fieldType="BoolAstType";
    }

    @Override
    public void visit(IntArrayAstType t) {
        this.fieldType="IntArrayAstType";
    }

    @Override
    public void visit(RefType t) {
    this.fieldType="RefType";
    }
}

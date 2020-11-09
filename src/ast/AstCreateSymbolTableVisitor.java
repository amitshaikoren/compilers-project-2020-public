package ast;

import java.util.List;

public class AstCreateSymbolTableVisitor implements Visitor{
    protected SymbolTable programSymbolTable;//to start
    private SymbolTable fatherSymbolTable;
    private SymbolTable currSymbolTable;

    private void buildSymbolTable(SymbolTable parent)
    {
        this.currSymbolTable = new SymbolTable(parent);
    }

    private void updateSymbolTable(SymbolTable symbolTable, boolean isMethod)
    {
        symbolTable.updateEntries();
    }

    private void updateLookupTable(LookupTable lookupTable){
    }

    private SymbolTable lookup(String scopeName)
    {
        //find the symboltable in the lookup map
        return programScopeSymbolTable;//todo: change!!!
    }
    @Override
    public void visit(Program program) {
        program.mainClass().accept(this);

        //assuming classDecls list is ordered (meaning classes that don't extend come first)
        for (ClassDecl classdecl : program.classDecls()) {
            classdecl.accept(this);
        }
    }

    @Override
    public void visit(ClassDecl classDecl) {



        if (classDecl.superName() != null) {

            SymbolTable parentSymbolTable = lookupTable.get(classDecl)

            buildSymbolTable();



        } else {

            buildSymbolTable(programSymbolTable);

        }

        for (var fieldDecl : classDecl.fields()) {
            currSymbolTable.buildSymbolInfo();
            fieldDecl.accept(this);
            updateSymbolTable(lookup(classDecl.name()),false);
            updateLookupTable();
        }

        for (var methodDecl : classDecl.methoddecls()) {
            this.fatherScope=lookup(classDecl.name());
            methodDecl.accept(this);
            updateLookupTable();
        }
    }

    @Override
    public void visit(MainClass mainClass) {

    }

    @Override
    public void visit(MethodDecl methodDecl) {
        methodDecl.returnType().accept(this); //the rettype sopposed to update
        this.methodName=methodDecl.name();
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
        currSymbolTable.setCurrSymbolName(varDecl.name());
        varDecl.type().accept(this);
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
        currSymbolTable.setSymbolInfoType("IntAstType");

    }

    @Override
    public void visit(BoolAstType t) {
        currSymbolTable.setSymbolInfoType("BoolAstType");
    }

    @Override
    public void visit(IntArrayAstType t) {
        currSymbolTable.setSymbolInfoType("IntArrayAstType");
    }

    @Override
    public void visit(RefType t) {
        currSymbolTable.setSymbolInfoType("RefType");
    }
}

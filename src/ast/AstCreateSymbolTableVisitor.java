package ast;

import java.util.List;

public class AstCreateSymbolTableVisitor implements Visitor{
    protected SymbolTable programSymbolTable;//to start
    private SymbolTable fatherSymbolTable;
    private SymbolTable currSymbolTable;
    private LookupTable varLookupTable;
    private LookupTable methodLookupTable;


    private void updateInfo(AstNode astNode, boolean isMethod){
        this.currSymbolTable.buildSymbolInfo();
        this.currSymbolTable.setSymbolInfoIsMethod(isMethod);
        astNode.accept(this);
        updateSymbolTable(isMethod);
        if(isMethod){updateMethodLookupTable(astNode, this.currSymbolTable);}
        else{updateVarLookupTable(astNode, this.currSymbolTable);}
    }

    public AstCreateSymbolTableVisitor(LookupTable varLookupTable, LookupTable methodLookupTable){
        this.varLookupTable = varLookupTable;
        this.methodLookupTable = methodLookupTable;
    }

    private void buildSymbolTable(SymbolTable parent)
    {
        this.currSymbolTable = new SymbolTable(parent);
    }

    private void updateSymbolTable(boolean isMethod)
    {
        currSymbolTable.updateEntries();
    }

    private void updateVarLookupTable(AstNode astNode, SymbolTable symbolTable){
        this.varLookupTable.updateLookupTable(astNode, symbolTable);
    }

    private void updateMethodLookupTable(AstNode astNode, SymbolTable symbolTable){
        this.methodLookupTable.updateLookupTable(astNode, symbolTable);
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
            buildSymbolTable();

        } else {
            buildSymbolTable(programSymbolTable);
        }

        for (var fieldDecl : classDecl.fields()) {
            updateInfo(fieldDecl, false);
        }

        for (var methodDecl : classDecl.methoddecls()) {
            updateInfo(methodDecl, true);
        }
    }

    @Override
    public void visit(MainClass mainClass) {

    }

    @Override
    public void visit(MethodDecl methodDecl) {
        methodDecl.returnType().accept(this);
        this.currSymbolTable.setCurrSymbolName(methodDecl.name());
        updateSymbolTable(true);


        buildSymbolTable(currSymbolTable);

        for (var formal : methodDecl.formals()) {
            updateInfo(formal, false);
        }

        for (var varDecl : methodDecl.vardecls()) {
            updateInfo(varDecl, false);
        }

        for (var stmt : methodDecl.body()) {
            stmt.accept(this);
        }

        methodDecl.ret().accept(this);
    }

    @Override
    public void visit(FormalArg formalArg) {
        currSymbolTable.setCurrSymbolName(formalArg.name());
        formalArg.type().accept(this);
    }

    @Override
    public void visit(VarDecl varDecl) {
        currSymbolTable.setCurrSymbolName(varDecl.name());
        varDecl.type().accept(this);
    }

    @Override
    public void visit(BlockStatement blockStatement) {
        for(var stmt : blockStatement.statements()){
            stmt.accept(this);
        }
    }

    @Override
    public void visit(IfStatement ifStatement) {
        ifStatement.cond().accept(this);
        ifStatement.elsecase().accept(this);
        ifStatement.thencase().accept(this);
    }

    @Override
    public void visit(WhileStatement whileStatement) {
        whileStatement.cond().accept(this);
        whileStatement.body().accept(this);
    }

    @Override
    public void visit(SysoutStatement sysoutStatement) {
        sysoutStatement.arg().accept(this);
    }

    @Override
    public void visit(AssignStatement assignStatement) {
        assignStatement.rv().accept(this);
    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {
        assignArrayStatement.index().accept(this);
        assignArrayStatement.rv().accept(this);
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
        currSymbolTable.setSymbolInfoDecl("int");

    }

    @Override
    public void visit(BoolAstType t) {
        currSymbolTable.setSymbolInfoDecl("bool");
    }

    @Override
    public void visit(IntArrayAstType t) {
        currSymbolTable.setSymbolInfoDecl("intArr");
    }

    @Override
    public void visit(RefType t) {
        currSymbolTable.setSymbolInfoDecl("ref");
    }
}

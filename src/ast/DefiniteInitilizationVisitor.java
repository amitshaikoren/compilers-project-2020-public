package ast;

import java.io.PrintWriter;

public class DefiniteInitilizationVisitor implements Visitor{

    private DefiniteInitializationDict currInitilizationDict;
    private boolean ifCase;
    private PrintWriter outfile;

    public DefiniteInitilizationVisitor(PrintWriter outfile){
        this.currInitilizationDict = new DefiniteInitializationDict();
        this.outfile=outfile;
    }
    public void RaiseError(){
        throw new RuntimeException();

    };

    @Override
    public void visit(Program program) {
        for (ClassDecl classdecl : program.classDecls()) {
            classdecl.accept(this);
        }
    }

    @Override
    public void visit(ClassDecl classDecl) {
        for(var methodDecl : classDecl.methoddecls()){
            methodDecl.accept(this);
        }
    }

    @Override
    public void visit(MainClass mainClass) {

    }

    @Override
    public void visit(MethodDecl methodDecl) {
        for (var varDecl : methodDecl.vardecls()) {
            currInitilizationDict.AddVar(varDecl.name(), false);
            varDecl.accept(this);
        }

        for (var stmt : methodDecl.body()) {
            stmt.accept(this);
        }

    }

    @Override
    public void visit(FormalArg formalArg) {

    }

    @Override
    public void visit(VarDecl varDecl) {

    }

    @Override
    public void visit(BlockStatement blockStatement) {
        for (var stmt : blockStatement.statements()) {
            stmt.accept(this);
        }
    }

    @Override
    public void visit(IfStatement ifStatement) {
        currInitilizationDict.IfSplit();

        currInitilizationDict = currInitilizationDict.getIfBlock();
        ifStatement.thencase().accept(this);
        currInitilizationDict = currInitilizationDict.getOuterBlock();

        currInitilizationDict = currInitilizationDict.getElseBlock();
        ifStatement.elsecase().accept(this);
        currInitilizationDict = currInitilizationDict.getOuterBlock();

        currInitilizationDict.IfElseUnion();
    }

    @Override
    public void visit(WhileStatement whileStatement) {

    }

    @Override
    public void visit(SysoutStatement sysoutStatement) {

    }

    @Override
    public void visit(AssignStatement assignStatement) {
        currInitilizationDict.ChangeVarState(assignStatement.lv(),true);
        assignStatement.rv().accept(this);

    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {
        currInitilizationDict.ChangeVarState(assignArrayStatement.lv(),true);
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
    if (!currInitilizationDict.get(e.id())){
        RaiseError();
    }
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

    }

    @Override
    public void visit(BoolAstType t) {

    }

    @Override
    public void visit(IntArrayAstType t) {

    }

    @Override
    public void visit(RefType t) {

    }
}

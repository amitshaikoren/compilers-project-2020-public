package ast;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class DefiniteInitilizationVisitor implements Visitor{

    private DefiniteInitializationDict currInitilizationDict;
    private PrintWriter outfile;
    private DefiniteInitializationDict classInitilizationDict;
    private Map<String,DefiniteInitializationDict > classInitilizationDictMap = new HashMap<>();


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
        classInitilizationDict=new DefiniteInitializationDict();
        if (classDecl.superName()!=null){
            classInitilizationDict =classInitilizationDictMap.get(classDecl.superName()).copy();
        }
        for (var fieldDecl : classDecl.fields()) {
            classInitilizationDict.AddVar(fieldDecl.name(),true);
        }
        classInitilizationDictMap.put(classDecl.name(),classInitilizationDict);

        for(var methodDecl : classDecl.methoddecls()){
            currInitilizationDict = classInitilizationDict.copy();
            methodDecl.accept(this);
        }
    }

    @Override
    public void visit(MainClass mainClass) {

    }

    @Override
    public void visit(MethodDecl methodDecl) {
        for (var formal : methodDecl.formals()) {
            currInitilizationDict.AddVar(formal.name(), true);
            formal.accept(this);
        }

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
    private void visitBinaryExpr(BinaryExpr e) {

        e.e1().accept(this);
        e.e2().accept(this);


    }
    @Override
    public void visit(AndExpr e) {
        visitBinaryExpr(e);
    }

    @Override
    public void visit(LtExpr e) {
        visitBinaryExpr(e);

    }

    @Override
    public void visit(AddExpr e) {
        visitBinaryExpr(e);

    }

    @Override
    public void visit(SubtractExpr e) {
        visitBinaryExpr(e);

    }

    @Override
    public void visit(MultExpr e) {
        visitBinaryExpr(e);

    }

    @Override
    public void visit(ArrayAccessExpr e) {
        e.arrayExpr().accept(this);
        e.indexExpr().accept(this);

    }

    @Override
    public void visit(ArrayLengthExpr e) {
        e.arrayExpr().accept(this);

    }

    @Override
    public void visit(MethodCallExpr e) {
        e.ownerExpr().accept(this);

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
        e.lengthExpr().accept(this);

    }

    @Override
    public void visit(NewObjectExpr e) {

    }

    @Override
    public void visit(NotExpr e) {
        e.e().accept(this);

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

package ast;

public class AstRenamingVisitor implements Visitor {
    private String originalName;
    private String newName;
    private LookupTable lookupTable;
    private SymbolTable symbolTableOfOriginalName;
    private boolean isMethod;
    private boolean changedVarDecl;



    public AstRenamingVisitor(String originalName, String newName, LookupTable lookupTable, SymbolTable symbolTableOfOriginalName, boolean isMethod){
        this.originalName = originalName;
        this.newName = newName;
        this.lookupTable = lookupTable;
        this.isMethod=isMethod;
        this.changedVarDecl=false;
        this.symbolTableOfOriginalName = symbolTableOfOriginalName;
    }


    @Override
    public void visit(Program program) {

    }

    @Override
    public void visit(ClassDecl classDecl) {
        if (isMethod){
            for (var methodDecl : classDecl.methoddecls()) {

            }
        }
        else{
            for (var fieldDecl : classDecl.fields()) {
                fieldDecl.accept(this);
            }
        }

    }

    @Override
    public void visit(MainClass mainClass) {

    }

    @Override
    public void visit(MethodDecl methodDecl) {

    }

    @Override
    public void visit(FormalArg formalArg) {

    }

    @Override
    public void visit(VarDecl varDecl) {
    if( varDecl.name().equals(this.originalName) && !changedVarDecl){
        varDecl.setName(this.newName);
        this.changedVarDecl=true;
    }
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

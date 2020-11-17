package ast;


public class AstCreateSymbolTableVisitor implements Visitor {
    protected SymbolTable programSymbolTable = new SymbolTable(null,null);//to start
    private SymbolTable fatherSymbolTable;
    private SymbolTable currSymbolTable;
    private LookupTable lookupTable;


    private void updateInfo(AstNode astNode, boolean isMethod) {
        this.currSymbolTable.buildSymbolInfo();
        this.currSymbolTable.setSymbolInfoIsMethod(isMethod);
        astNode.accept(this);
        updateSymbolTable(isMethod);
        if (isMethod) {
            updateLookupTable(astNode, this.fatherSymbolTable);

        } else {
            updateLookupTable(astNode, this.currSymbolTable);
        }
    }

    public AstCreateSymbolTableVisitor(LookupTable lookupTable) {
        this.lookupTable = lookupTable;
    }

    private void visitBinaryExpr(BinaryExpr e) {
        e.e1().accept(this);
        e.e2().accept(this);
    }


    private void buildSymbolTable(SymbolTable parent, String nameOfClass) {
        this.currSymbolTable = new SymbolTable(parent,nameOfClass);
    }

    private void updateSymbolTable(boolean isMethod) {
        currSymbolTable.updateEntries();
    }


    private void updateLookupTable(AstNode astNode, SymbolTable symbolTable) {
        this.lookupTable.updateLookupTable(astNode, symbolTable);
    }

    private void updateClassDeclMap(String name, AstNode astnode) {
        this.lookupTable.updateclassDeclMap(name, astnode);
    }


    @Override
    public void visit(Program program) {
        program.mainClass().accept(this);

        //assuming classDecls list is ordered (meaning classes that don't extend come first)
        for (ClassDecl classdecl : program.classDecls()) {
            updateClassDeclMap(classdecl.name(), classdecl);
            classdecl.accept(this);
        }


    }

    @Override
    public void visit(ClassDecl classDecl) {

        if (classDecl.superName() != null) {
            AstNode astNode = lookupTable.getClassDeclName(classDecl.superName());
            buildSymbolTable(lookupTable.getSymbolTable(astNode),classDecl.name());

        } else {
            buildSymbolTable(programSymbolTable,classDecl.name());
        }

        for (var fieldDecl : classDecl.fields()) {
            updateInfo(fieldDecl, false);
        }

        for (var methodDecl : classDecl.methoddecls()) {
            this.fatherSymbolTable = this.currSymbolTable;
            updateInfo(methodDecl, true);
            this.currSymbolTable = this.fatherSymbolTable;
        }
        this.fatherSymbolTable = this.currSymbolTable.getFatherSymbolTable();
    }

    @Override
    public void visit(MainClass mainClass) {

    }

    @Override
    public void visit(MethodDecl methodDecl) {
        methodDecl.returnType().accept(this);
        this.currSymbolTable.setCurrSymbolName(methodDecl.name());
        updateSymbolTable(true);


        buildSymbolTable(currSymbolTable,null);

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
        for (var stmt : blockStatement.statements()) {
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
        //empty function
    }

    @Override
    public void visit(TrueExpr e) {
        //empty function

    }

    @Override
    public void visit(FalseExpr e) {
        //empty function

    }

    @Override
    public void visit(IdentifierExpr e) {
        //empty function

    }

    @Override
    public void visit(ThisExpr e) {
        //empty function

    }

    @Override
    public void visit(NewIntArrayExpr e) {
        e.lengthExpr().accept(this);
    }

    @Override
    public void visit(NewObjectExpr e) {
        //empty function
    }

    @Override
    public void visit(NotExpr e) {
        e.e().accept(this);

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
        currSymbolTable.setCurrSymbolInfoRefType(t.id());
    }
}

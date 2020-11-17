package ast;

import java.util.Iterator;

public class AstRenamingVisitor implements Visitor {
    private String originalName;
    private String newName;
    private LookupTable lookupTable;
    private SymbolTable symbolTableOfOriginalName;
    private SymbolTable currSymbolTable;
    private boolean isMethod;



    public AstRenamingVisitor(String originalName, String newName, LookupTable lookupTable, SymbolTable symbolTableOfOriginalName, boolean isMethod){
        this.originalName = originalName;
        this.newName = newName;
        this.lookupTable = lookupTable;
        this.isMethod=isMethod;
        this.symbolTableOfOriginalName = symbolTableOfOriginalName;
    }

    private void visitBinaryExpr(BinaryExpr e)
    {
        e.e1().accept(this);
        e.e2().accept(this);
    }

    private boolean nameResolution(SymbolTable symbolTableOfDecl)
    {
     if(symbolTableOfDecl==symbolTableOfOriginalName)
     {
         return true;
     }
     if (isMethod){
         // fill latter
     }
     else
     {
        if( symbolTableOfDecl.isInVarEntries(originalName))
        {
            return  false;
        }
     }
     SymbolTable fatherSymbolTable=symbolTableOfDecl.getFatherSymbolTable();
      while (fatherSymbolTable!=null)
      {
          if(isMethod && fatherSymbolTable.isInMethodEntries(originalName))
          {
              if(fatherSymbolTable==symbolTableOfOriginalName)
              {
                  return true;
              }
                  return false;
          }
          if(!isMethod && fatherSymbolTable.isInVarEntries(originalName))
          {
              if(fatherSymbolTable==symbolTableOfOriginalName)
              {
                  return true;
              }
              return false;
          }
          fatherSymbolTable=fatherSymbolTable.getFatherSymbolTable();
      }
      return false; //error
    }

    /*renameAstNode*/
    private void renameAstNode(VarDecl astNode)
    {
        astNode.setName(this.newName);
    }
    private void renameAstNode(FormalArg astNode)
    {
        astNode.setName(this.newName);
    }
    private void renameAstNode(AssignStatement astNode)
    {
        astNode.setLv(this.newName);
    }
    private void renameAstNode(AssignArrayStatement astNode)
    {
        astNode.setLv(this.newName);
    }
    private void renameAstNode(MethodCallExpr astNode)
    {
        astNode.setMethodId(this.newName);
    }
    private void renameAstNode( IdentifierExpr astNode)
    {
        astNode.setId(this.newName);
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

        for (var fieldDecl : classDecl.fields()) {
            this.currSymbolTable=lookupTable.getSymbolTable(fieldDecl);
            fieldDecl .accept(this);
        }

        for (var methodDecl : classDecl.methoddecls()) {
            this.currSymbolTable=lookupTable.getSymbolTable(methodDecl);
            methodDecl .accept(this);
        }
    }

    @Override
    public void visit(MainClass mainClass) {

    }

    @Override
    public void visit(MethodDecl methodDecl) {
        if(methodDecl.name().equals(originalName)){
            if(nameResolution(currSymbolTable)){
                methodDecl.setName(newName);
            }
        }
        for (var formal : methodDecl.formals()) {
            this.currSymbolTable=lookupTable.getSymbolTable(formal);
            formal.accept(this);
        }

        for (var varDecl : methodDecl.vardecls()) {
            this.currSymbolTable=lookupTable.getSymbolTable(varDecl);
            varDecl.accept(this);
        }

        for (var stmt : methodDecl.body()) {
            stmt.accept(this);
        }

        methodDecl.ret().accept(this);
    }

    @Override
    public void visit(FormalArg formalArg) {
        if(this.originalName.equals(formalArg.name()))
        {
            if(nameResolution(currSymbolTable))
            {
                renameAstNode(formalArg);
            }
        }
    }

    @Override
    public void visit(VarDecl varDecl) {
        if(this.originalName.equals(varDecl.name()))
        {
            if(nameResolution(currSymbolTable))
            {
                renameAstNode(varDecl);
            }
        }
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
        if(this.originalName.equals(assignStatement.lv()))
        {
            if(nameResolution(currSymbolTable))
            {
                renameAstNode(assignStatement);
            }
        }
        assignStatement.rv().accept(this);
    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {
        if(this.originalName.equals( assignArrayStatement.lv()))
        {
            if(nameResolution(currSymbolTable))
            {
                renameAstNode( assignArrayStatement);
            }
        }
        assignArrayStatement.index().accept(this);
        assignArrayStatement.rv().accept(this);
    }

    @Override
    public void visit(AndExpr e) {

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
        if(this.originalName.equals( e.methodId()))
        {
            if(nameResolution(currSymbolTable))
            {
                renameAstNode(e);
            }
        }
        String delim = "";
        for(Iterator var3 = e.actuals().iterator(); var3.hasNext(); delim = ", ") {
            Expr arg = (Expr) var3.next();
            arg.accept(this);
        }
    }

    @Override
    public void visit(IntegerLiteralExpr e) {
        //no need
    }

    @Override
    public void visit(TrueExpr e) {
        //no need
    }

    @Override
    public void visit(FalseExpr e) {
        //no need

    }

    @Override
    public void visit(IdentifierExpr e) {
        if(this.originalName.equals( e.id()))
        {
            if(nameResolution(currSymbolTable))
            {
                renameAstNode(e);
            }
        }
    }

    @Override
    public void visit(ThisExpr e) {
        //no need
    }

    @Override
    public void visit(NewIntArrayExpr e) {
        e.lengthExpr().accept(this);
    }

    @Override
    public void visit(NewObjectExpr e) {
        //no need
    }

    @Override
    public void visit(NotExpr e) {
        e.e().accept(this);
    }

    @Override
    public void visit(IntAstType t) {
        //no need
    }

    @Override
    public void visit(BoolAstType t) {
        //no need
    }

    @Override
    public void visit(IntArrayAstType t) {
        //no need
    }

    @Override
    public void visit(RefType t) {
        //no need
    }
}

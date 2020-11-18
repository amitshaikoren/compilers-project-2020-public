package ast;

import java.util.Iterator;

public class AstRenamingVisitor implements Visitor {
    private String originalName;
    private String newName;
    private LookupTable lookupTable;
    private SymbolTable symbolTableOfOriginalName;
    private SymbolTable currSymbolTable;
    private boolean isMethod;
    private String currMethodRefType;



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
         if(symbolTableOfDecl.isInMethodEntries(originalName))
         {
             return  false;
         }
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

    //RENAMING ASTNODE METHODS
    private void renameAstNode(VarDecl astNode)
    {
        if(this.originalName.equals(astNode.name()) && !isMethod) {
            if(nameResolution(currSymbolTable)) {
                astNode.setName(this.newName);
            }
        }
    }

    private void renameAstNode(FormalArg astNode)
    {
        if(this.originalName.equals(astNode.name()) && !isMethod) {
            if(nameResolution(currSymbolTable)) {
                astNode.setName(this.newName);
            }
        }
    }
    private void renameAstNode(AssignStatement astNode) {
        if (this.originalName.equals(astNode.lv()) && !isMethod) {
            if (nameResolution(currSymbolTable)) {
                astNode.setLv(this.newName);
            }
        }
    }

    private void renameAstNode(AssignArrayStatement astNode)
        {
            if (this.originalName.equals(astNode.lv()) && !isMethod) {
                if (nameResolution(currSymbolTable)) {
                    astNode.setLv(this.newName);
                }
            }
        }

    private void renameAstNodeMethod(MethodCallExpr astNode, SymbolTable st) {
        if(this.originalName.equals(astNode.methodId()) && isMethod)
        {
            if(nameResolution(st))
            {
                astNode.setMethodId(this.newName);
            }
        }
    }

    private void renameAstNode(IdentifierExpr astNode)
    {
        if(this.originalName.equals(astNode.id()) && !isMethod )
        {
            if(nameResolution(currSymbolTable))
            {
                astNode.setId(this.newName);
            }
        }
    }


    @Override
    public void visit(Program program) {

        //assuming classDecls list is ordered (meaning classes that don't extend come first)
        for (ClassDecl classdecl : program.classDecls()) {

            classdecl.accept(this);
        }

        program.mainClass().accept(this);

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
        mainClass.mainStatement().accept(this);
    }

    @Override
    public void visit(MethodDecl methodDecl) {
        if(methodDecl.name().equals(originalName) && this.isMethod){
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
        renameAstNode(formalArg);
    }

    @Override
    public void visit(VarDecl varDecl) {
        renameAstNode(varDecl);
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
        renameAstNode(assignStatement);
        assignStatement.rv().accept(this);
    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {
        renameAstNode(assignArrayStatement);
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
        SymbolTable st;

        //if called from new, we get currMethodRefType != null
        if(this.currMethodRefType != null) {
            AstNode node = lookupTable.getClassDeclName(this.currMethodRefType);
            st = lookupTable.getSymbolTable(node);
        }
        else{
            st=this.currSymbolTable;
        }

        renameAstNodeMethod(e, st);

        //to allow next change name to not be affected by previous change name
        this.currMethodRefType=null;

        String delim = "";
        for(Iterator var3 = e.actuals().iterator(); var3.hasNext(); delim = ", ") {
            Expr arg = (Expr) var3.next();
            arg.accept(this);
        }
    }

    @Override
    public void visit(IntegerLiteralExpr e) {
        //no needRenamed and restructured code.
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
        renameAstNode(e);
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
        this.currMethodRefType = e.classId();
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
    }


}


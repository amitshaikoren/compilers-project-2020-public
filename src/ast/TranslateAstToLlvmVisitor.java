package ast;

public class TranslateAstToLlvmVisitor implements Visitor{

    String code="\n" +
            "@.Simple_vtable = global [1 x i8*] [i8* bitcast (i32 (i8*)* @Simple.bar to i8*)]\n" +
            "\n" +
            "        declare i8* @calloc(i32, i32)\n" +
            "declare i32 @printf(i8*, ...)\n" +
            "declare void @exit(i32)\n" +
            "\n" +
            "@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n" +
            "@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\n" +
            "        define void @print_int(i32 %i) {\n" +
            "        %_str = bitcast [4 x i8]* @_cint to i8*\n" +
            "        call i32 (i8*, ...) @printf(i8* %_str, i32 %i)\n" +
            "\tret void\n" +
            "            }\n" +
            "            define void @throw_oob() {\n" +
            "        %_str = bitcast [15 x i8]* @_cOOB to i8*\n" +
            "        call i32 (i8*, ...) @printf(i8* %_str)\n" +
            "\tcall void @exit(i32 1)\n" +
            "\tret void\n" +
            "            }\n" +
            "            define i32 @main() {\n" +
            "        %_0 = call i8* @calloc(i32 8, i32 8)\n" +
            "\t%_1 = bitcast i8* %_0 to i8***\n" +
            "            %_2 = getelementptr [1 x i8*], [1 x i8*]* @.Simple_vtable, i32 0, i32 0\n" +
            "        store i8** %_2, i8*** %_1\n" +
            "        %_3 = bitcast i8* %_0 to i8***\n" +
            "        %_4 = load i8**, i8*** %_3\n" +
            "        %_5 = getelementptr i8*, i8** %_4, i32 0\n" +
            "        %_6 = load i8*, i8** %_5\n" +
            "        %_7 = bitcast i8* %_6 to i32 (i8*)*\n" +
            "        %_8 = call i32 %_7(i8* %_0)\n" +
            "        call void (i32) @print_int(i32 %_8)\n" +
            "\tret i32 0\n" +
            "            }\n" +
            "\n" +
            "            define i32 @Simple.bar(i8* %this) {";

    static int countOfReg=-1;
    static  int countOfIf=-1;
    private int indent ;
    private StringBuilder builder;
    CurrInstruction currInstruction;
    private SymbolTable currSymbolTable;
    private LookupTable lookupTable;
    ExprTranslation currExpr;
    ExprTranslation fatherExpr;

    public TranslateAstToLlvmVisitor(LookupTable lookupTable)
    {
        this.lookupTable=lookupTable;
        this.indent=1;
        this.builder = new StringBuilder();
        this.currExpr=null;
        this.fatherExpr=null;
    }
    public String getString() {
        return this.builder.toString();
    }

    private SymbolTable getSTnameResolution(String name)
    {
        if(currSymbolTable.isInVarEntries(name))
        {
            return currSymbolTable;
        }
        SymbolTable fatherSymbolTable=currSymbolTable.getFatherSymbolTable();
        while (fatherSymbolTable!=null)
        {
            if(fatherSymbolTable.isInVarEntries(name))
            {
                return fatherSymbolTable;
            }
            fatherSymbolTable=fatherSymbolTable.getFatherSymbolTable();
        }
        return null;//error
    }

    private void appendWithIndent(String str) {
        this.builder.append("\t".repeat(this.indent));
        this.builder.append(str);
    }

    private void updateVarTypeInSymbolTable(String type,String name,String regName) {
        currSymbolTable.getSymbolinfo(name,false).setRegName(regName);
    }

    private static String getNextRegister()
    {
     countOfReg++;
     String reg;
     reg="%_"+Integer.toString(countOfReg);
     return reg;
    }
    private static String getNextIfStatment()
    {
        countOfIf++;
        String ifStatment="if"+Integer.toString(countOfIf);
        return ifStatment;
    }

    @Override
    public void visit(Program program) {
        this.builder.append(code);
        this.builder.append("\n");
        program.mainClass().accept(this);
        for (ClassDecl classdecl : program.classDecls()) {
            classdecl.accept(this);
        }
        this.builder.append("}");
    }

    @Override
    public void visit(ClassDecl classDecl) {
        for (var fieldDecl : classDecl.fields()) {
            this.currSymbolTable=lookupTable.getSymbolTable(fieldDecl);
            fieldDecl.accept(this);
        }
        for (var methodDecl : classDecl.methoddecls()) {
            this.currSymbolTable=lookupTable.getSymbolTable(methodDecl);
            methodDecl.accept(this);
        }

    }

    @Override
    public void visit(MainClass mainClass) {

    }

    @Override
    public void visit(MethodDecl methodDecl) {
        for (var formal : methodDecl.formals())
        {
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
        currInstruction=currInstruction.MethodDecl;
        appendWithIndent("ret ");
        methodDecl.returnType().accept(this);
        methodDecl.ret().accept(this);
        this.builder.append(currExpr.getResult()+"\n");
    }

    @Override
    public void visit(FormalArg formalArg) {

    }

    @Override
    public void visit(VarDecl varDecl) {
        this.currInstruction=currInstruction.VarDecl;
        this.appendWithIndent("");
        this.builder.append("%"+varDecl.name());
        varDecl.type().accept(this);
        this.builder.append(" ");
        //this.builder.append(varDecl.name());
        this.builder.append("\n");
        updateVarTypeInSymbolTable(currInstruction.getName(),varDecl.name(),"%"+varDecl.name());
    }

    @Override
    public void visit(BlockStatement blockStatement) {

    }

    @Override
    public void visit(IfStatement ifStatement) {
        ifStatement.cond().accept(this);
        String ifStatment = getNextIfStatment();
        String elseStatment = getNextIfStatment();
        String backToCodeStatment = getNextIfStatment();
        appendWithIndent("br i1 "+currExpr.getResult()+", label %"+ifStatment+" , label %"+elseStatment+"\n");
        this.indent--;
        appendWithIndent(ifStatment+":"+"\n");
        this.indent++;
        ifStatement.thencase().accept(this);
        appendWithIndent("br label %"+backToCodeStatment+"\n");
        this.indent--;
        appendWithIndent(elseStatment+":"+"\n");
        this.indent++;
        ifStatement.elsecase().accept(this);
        appendWithIndent("br label %"+backToCodeStatment+"\n");
        this.indent--;
        appendWithIndent(backToCodeStatment+":"+"\n");
        this.indent++;


    }

    @Override
    public void visit(WhileStatement whileStatement) {

    }

    @Override
    public void visit(SysoutStatement sysoutStatement) {
        sysoutStatement.arg().accept(this);
        appendWithIndent("call void (i32) @print_int(i32 ");
        this.builder.append(currExpr.getResult());
        currExpr=null;
        this.builder.append(")\n");
    }

    @Override
    public void visit(AssignStatement assignStatement) {
        assignStatement.rv().accept(this);
        this.appendWithIndent("");
        this.builder.append("store ");
        SymbolTable stOfDecl=getSTnameResolution(assignStatement.lv());
        printType(stOfDecl.getSymbolinfo(assignStatement.lv(),false).getDecl());
        this.builder.append(currExpr.getResult());
        currExpr=null;
        this.builder.append(", ");
        printPointerType(stOfDecl.getSymbolinfo(assignStatement.lv(),false).getDecl());
        this.builder.append("%"+assignStatement.lv());
        this.builder.append("\n");
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
        //todo: add for reftype and intarray
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
        //todo: add for reftype and intarray
    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {

    }
    private void visitBinaryExpr(BinaryExpr e, String infixSymbol) {
        ExprTranslation exp,e1,e2;
        exp=new ExprTranslation(null,null,null,null);
        currExpr=exp;
        e.e1().accept(this);
        exp.setE1(currExpr);
        currExpr=exp;
        e.e2().accept(this);
        exp.setE2(currExpr);
        String reg=getNextRegister();
        appendWithIndent(reg+" = "+infixSymbol+" ");
        //todo: check if we can assume that its always int
        printType("int");
        this.builder.append(exp.getE1().getResult()+", ");
        this.builder.append(exp.getE2().getResult()+"\n");
        exp.setResult(reg);
        currExpr=exp;
    }
    @Override
    public void visit(AndExpr e) {

    }

    @Override
    public void visit(LtExpr e) {
        visitBinaryExpr(e,"icmp slt");
    }

    @Override
    public void visit(AddExpr e) {
        visitBinaryExpr(e,"add");

    }

    @Override
    public void visit(SubtractExpr e) {
        visitBinaryExpr(e,"sub");

    }

    @Override
    public void visit(MultExpr e) {
        visitBinaryExpr(e,"mul");

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
        ExprTranslation exp;
        if(currExpr==null)
        {
            exp=new ExprTranslation(null,null,null,Integer.toString(e.num()));
        }
        else
        {
            exp=new ExprTranslation(fatherExpr,null,null,Integer.toString(e.num()));
        }
        currExpr=exp;
    }

    @Override
    public void visit(TrueExpr e) {
        ExprTranslation exp;
        if(currExpr==null)
        {
            exp=new ExprTranslation(null,null,null,Integer.toString(1));
        }
        else
        {
            exp=new ExprTranslation(fatherExpr,null,null,Integer.toString(1));
        }
        currExpr=exp;
    }

    @Override
    public void visit(FalseExpr e) {
        ExprTranslation exp;
        if(currExpr==null)
        {
            exp=new ExprTranslation(null,null,null,Integer.toString(0));
        }
        else
        {
            exp=new ExprTranslation(fatherExpr,null,null,Integer.toString(0));
        }
        currExpr=exp;
    }

    @Override
    public void visit(IdentifierExpr e) {
        ExprTranslation exp;
        String reg=getNextRegister();
        appendWithIndent(reg+" = load ");
        SymbolTable stOfDecl=getSTnameResolution(e.id());
        printType(stOfDecl.getSymbolinfo(e.id(),false).getDecl());
        this.builder.append(", ");
        printPointerType(stOfDecl.getSymbolinfo(e.id(),false).getDecl());
        this.builder.append(" %"+e.id()+"\n");
        if(currExpr==null)
        {
            exp=new ExprTranslation(null,null,null,reg);
        }
        else
        {
            exp=new ExprTranslation(fatherExpr,null,null,reg);
        }
        currExpr=exp;
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
        if(this.currInstruction==currInstruction.VarDecl)
        {
            currInstruction=currInstruction.VarDeclInt;
            appendWithIndent("= alloca i32");
        }
        if(this.currInstruction==currInstruction.MethodDecl)
        {
            appendWithIndent("i32 ");
        }
    }


    @Override
    public void visit(BoolAstType t) {
        if(this.currInstruction==currInstruction.VarDecl)
        {
            currInstruction=currInstruction.VarDeclBool;
            appendWithIndent("= alloca i1");
        }
        if(this.currInstruction==currInstruction.MethodDecl)
        {
            appendWithIndent("i1 ");
        }
    }

    @Override
    public void visit(IntArrayAstType t) {

    }

    @Override
    public void visit(RefType t) {

    }
}

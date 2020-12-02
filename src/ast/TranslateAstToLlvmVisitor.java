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
    private static String getNextStatment(String infixSymbol)
    {
        countOfIf++;
        String ifStatment=infixSymbol+Integer.toString(countOfIf);
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
    //todo: do it
    }

    @Override
    public void visit(MethodDecl methodDecl) {
        //todo : declere func
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
//todo : do it
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
        for (var stmt : blockStatement.statements()) {
            stmt.accept(this);
        }
    }

    @Override
    public void visit(IfStatement ifStatement) {
        ifStatement.cond().accept(this);
        String ifStatment = getNextStatment("if");
        String elseStatment = getNextStatment("if");
        String backToCodeStatment = getNextStatment("if");
        appendWithIndent("br i1 "+currExpr.getResult()+", label %"+ifStatment+" , label %"+elseStatment+"\n");
        this.builder.append(ifStatment+":"+"\n");
        ifStatement.thencase().accept(this);
        appendWithIndent("br label %"+backToCodeStatment+"\n");
        this.builder.append(elseStatment+":"+"\n");
        ifStatement.elsecase().accept(this);
        appendWithIndent("br label %"+backToCodeStatment+"\n");
        this.builder.append(backToCodeStatment+":"+"\n");


    }

    @Override
    public void visit(WhileStatement whileStatement) {
        whileStatement.cond().accept(this);
        String whileStatment = getNextStatment("loop");
        String backToCodeStatment = getNextStatment("backtocode");
        appendWithIndent("br i1 "+currExpr.getResult()+", label %"+whileStatment+" , label %"+backToCodeStatment+"\n");
        this.builder.append(whileStatment+":"+"\n");
       whileStatement.body().accept(this);
        appendWithIndent("br label %"+whileStatement+"\n");
        this.builder.append(backToCodeStatment+":"+"\n");

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
        if(type.equals("intArr"))
        {
            this.builder.append("i32* ");
        }
        //todo: add for reftype
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
        if(type.equals("intArr"))
        {
            this.builder.append("i32** ");
        }
        //todo: add for reftype
    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {
        String loadArray = getNextRegister();
        appendWithIndent(loadArray+" = load i32* , i32** %"+assignArrayStatement.lv()+"\n");
        String checkPositive = getNextRegister();
        assignArrayStatement.index().accept(this);
        appendWithIndent(checkPositive+" = icmp slt i32 "+currExpr.getResult()+" , 0\n");
        String negativeIndex=getNextStatment("arr_alloc");
        String positiveIndex=getNextStatment("arr_alloc");
        appendWithIndent("br i1 "+checkPositive+" , label %"+negativeIndex+", label %"+positiveIndex+"\n");
        this.builder.append(negativeIndex+":\n");
        appendWithIndent("call void @throw_obb()\n");
        appendWithIndent("br label %"+positiveIndex+"\n");
        this.builder.append(positiveIndex+":\n");
        String arrayPointer=getNextRegister();
        appendWithIndent(arrayPointer+" = getelementptr i32, i32* "+loadArray+", i32 0 \n");
        String arrayLength=getNextRegister();
        appendWithIndent(arrayLength+" = load i32, i32* "+arrayPointer+"\n");
        String checkIndexLessThenLength=getNextRegister();
        appendWithIndent(checkIndexLessThenLength+" = icmp sle i32 "+arrayLength+","+currExpr.getResult()+"\n");
        String badIndex=getNextStatment("arr_alloc");
        String goodIndex=getNextStatment("arr_alloc");
        appendWithIndent("br i1 "+checkIndexLessThenLength+", label %"+badIndex+", label %"+goodIndex+"\n");
        this.builder.append(badIndex+":\n");
        appendWithIndent("call void @throw_obb()\n");
        appendWithIndent("br label %"+goodIndex+"\n");
        this.builder.append(goodIndex+":\n");
        String fixIndex=getNextRegister();
        appendWithIndent(fixIndex+" = add i32 "+currExpr.getResult()+ ",1 \n");
        String pointerOfIndex=getNextRegister();
        appendWithIndent(pointerOfIndex+ " = getelementptr i32,i32* "+loadArray +", i32 "+fixIndex+"\n");
        currExpr=null;
        assignArrayStatement.rv().accept(this);
        appendWithIndent("store i32 "+currExpr.getResult()+" , i32* "+pointerOfIndex+"\n");
        currExpr=null;





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
        ExprTranslation exp,e1,e2;
        exp=new ExprTranslation(null,null,null,null);
        currExpr=exp;
        e.e1().accept(this);
        exp.setE1(currExpr);
        currExpr=exp;
        String labelf = getNextStatment("andcond");
        String label1=getNextStatment("andcond");
        String labelt=getNextStatment("andcond");
        String backlabel=getNextStatment("andcond");
        appendWithIndent("br label %"+labelf+"\n");
        this.builder.append(labelf+":\n");
        appendWithIndent("br i1 "+exp.getE1().getResult()+", "+"label %"+label1+" ,label %"+backlabel+"\n");
        this.builder.append(label1+":\n");
        e.e2().accept(this);
        exp.setE2(currExpr);
        appendWithIndent("br label %"+labelt+"\n");
        this.builder.append(labelt+":\n");
        appendWithIndent("br label %"+backlabel+"\n");
        this.builder.append(backlabel+":\n");
        String reg=getNextRegister();
        appendWithIndent(reg+" = "+"phi i1 [0,%"+labelf+"], ["+exp.getE2().getResult()+",%"+labelt+"]\n");
        exp.setResult(reg);
        currExpr=exp;
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
        e.arrayExpr().accept(this);
        String loadArray=currExpr.getResult();
        String checkPositive = getNextRegister();
        e.indexExpr().accept(this);
        appendWithIndent(checkPositive+" = icmp slt i32 "+currExpr.getResult()+" , 0\n");
        String negativeIndex=getNextStatment("arr_alloc");
        String positiveIndex=getNextStatment("arr_alloc");
        appendWithIndent("br i1 "+checkPositive+" , label %"+negativeIndex+", label %"+positiveIndex+"\n");
        this.builder.append(negativeIndex+":\n");
        appendWithIndent("call void @throw_obb()\n");
        appendWithIndent("br label %"+positiveIndex+"\n");
        this.builder.append(positiveIndex+":\n");
        String arrayPointer=getNextRegister();
        appendWithIndent(arrayPointer+" = getelementptr i32, i32* "+loadArray+", i32 0 \n");
        String arrayLength=getNextRegister();
        appendWithIndent(arrayLength+" = load i32, i32* "+arrayPointer+"\n");
        String checkIndexLessThenLength=getNextRegister();
        appendWithIndent(checkIndexLessThenLength+" = icmp sle i32 "+arrayLength+","+currExpr.getResult()+"\n");
        String badIndex=getNextStatment("arr_alloc");
        String goodIndex=getNextStatment("arr_alloc");
        appendWithIndent("br i1 "+checkIndexLessThenLength+", label %"+badIndex+", label %"+goodIndex+"\n");
        this.builder.append(badIndex+":\n");
        appendWithIndent("call void @throw_obb()\n");
        appendWithIndent("br label %"+goodIndex+"\n");
        this.builder.append(goodIndex+":\n");
        String fixIndex=getNextRegister();
        appendWithIndent(fixIndex+" = add i32 "+currExpr.getResult()+ ",1 \n");
        String pointerOfIndex=getNextRegister();
        appendWithIndent(pointerOfIndex+ " = getelementptr i32,i32* "+loadArray +", i32 "+fixIndex+"\n");
        String value = getNextRegister();
        appendWithIndent(value+" = load i32 ,i32* "+pointerOfIndex+"\n");
        currExpr.setResult(value);
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
        e.lengthExpr().accept(this);
        String regValidLength=getNextRegister();
        appendWithIndent(regValidLength+" = icmp slt i32 "+currExpr.getResult()+", 0 \n");
        String negativeLength= getNextStatment("arr_alloc");
        String positiveLength=getNextStatment("arr_alloc");
        appendWithIndent("br i1 "+regValidLength+", label %"+negativeLength+" ,label %"+positiveLength+"\n");
        this.builder.append(negativeLength+":\n");
        appendWithIndent("call void @throw_oob()\n");
        appendWithIndent("br label %"+positiveLength+"\n");
        this.builder.append(positiveLength+":\n");
        String lengthSize = getNextRegister();
        appendWithIndent(lengthSize+"= add i32 "+currExpr.getResult()+" ,1\n");
        String allocateArr = getNextRegister();
        appendWithIndent(allocateArr+" =call i8* @calloc(i32 4, i32 "+lengthSize+")\n");
        String castReturnedPointer=getNextRegister();
        appendWithIndent(castReturnedPointer+" = bitcast i8* "+allocateArr+" to i32*\n");
        appendWithIndent("store i32  "+currExpr.getResult()+", i32* "+castReturnedPointer+"\n");
        currExpr.setResult(castReturnedPointer);


    }

    @Override
    public void visit(NewObjectExpr e) {

    }

    @Override
    public void visit(NotExpr e) {
        e.e().accept(this);
        String notE = getNextRegister();
        appendWithIndent(notE +" = sub 1 i1"+currExpr.getResult()+"\n");
        currExpr.setResult(notE);

    }

    @Override
    public void visit(IntAstType t) {
        if(this.currInstruction==currInstruction.VarDecl)
        {
            currInstruction=currInstruction.VarDeclInt;
            this.builder.append("= alloca i32");
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
            this.builder.append("= alloca i1");
        }
        if(this.currInstruction==currInstruction.MethodDecl)
        {
            appendWithIndent("i1 ");
        }
    }

    @Override
    public void visit(IntArrayAstType t) {
        if(this.currInstruction==currInstruction.VarDecl)
        {
            currInstruction=currInstruction.VarDeclIntArray;
            this.builder.append("= alloca i32*");
        }
        if(this.currInstruction==currInstruction.MethodDecl)
        {
            appendWithIndent("i32* ");
        }
    }

    @Override
    public void visit(RefType t) {

    }
}

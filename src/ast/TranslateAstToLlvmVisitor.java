package ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class TranslateAstToLlvmVisitor implements Visitor{

    String code=
            "declare i8* @calloc(i32, i32)\n" +
            "declare i32 @printf(i8*, ...)\n" +
            "declare void @exit(i32)\n" +
            "\n" +
            "@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n" +
            "@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\n" +
            "define void @print_int(i32 %i) {\n" +
            "        %_str = bitcast [4 x i8]* @_cint to i8*\n" +
            "        call i32 (i8*, ...) @printf(i8* %_str, i32 %i)\n" +
            "\tret void\n" +
            "            }\n" +
            "define void @throw_oob() {\n" +
            "        %_str = bitcast [15 x i8]* @_cOOB to i8*\n" +
            "        call i32 (i8*, ...) @printf(i8* %_str)\n" +
            "\tcall void @exit(i32 1)\n" +
            "\tret void\n" +
            "            }\n";

    private static int countOfReg=-1;
    private static int countOfIf=-1;
    private int indent ;
    private StringBuilder builder;
    private CurrInstruction currInstruction;
    private SymbolTable currSymbolTable;
    private LookupTable lookupTable;
    private ExprTranslation currExpr;
    private ExprTranslation fatherExpr;
    private Map<String,ClassMap> classOfMaps;
    private boolean defineFunc;
    private String currentClass;
    private  Map<String, ArrayList<MethodOfClass>> funcOfClass;

    public TranslateAstToLlvmVisitor(LookupTable lookupTable,Map<String,ClassMap> classOfMaps,Map<String, ArrayList<MethodOfClass>> funcOfClass)
    {
        this.lookupTable=lookupTable;
        this.indent=1;
        this.builder = new StringBuilder();
        this.currExpr=null;
        this.fatherExpr=null;
        this.classOfMaps=classOfMaps;
        this.defineFunc=false;
        this.funcOfClass=funcOfClass;

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
    private SymbolTable getmethodnameResolution(String name,SymbolTable other)
    {
        if(other.isInMethodEntries(name))
        {
            return other;
        }
        SymbolTable fatherSymbolTable=other.getFatherSymbolTable();
        while (fatherSymbolTable!=null)
        {
            if(fatherSymbolTable.isInMethodEntries(name))
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
    }

    @Override
    public void visit(ClassDecl classDecl) {

        for (var fieldDecl : classDecl.fields()) {
            this.currSymbolTable=lookupTable.getSymbolTable(fieldDecl);
            //fieldDecl.accept(this);
        }
        for (var methodDecl : classDecl.methoddecls()) {
            this.currSymbolTable=lookupTable.getSymbolTable(methodDecl);
            methodDecl.accept(this);
        }

    }

    @Override
    public void visit(MainClass mainClass) {
    this.builder.append("define i32 @main(){\n");
    mainClass.mainStatement().accept(this);
    appendWithIndent("ret i32 0\n ");
        this.builder.append("}\n");
    }

    @Override
    public void visit(MethodDecl methodDecl) {
        this.countOfIf=-1;
        this.countOfReg=-1;
        this.defineFunc=true;
        this.currSymbolTable=lookupTable.getSymbolTable(methodDecl);
        this.builder.append("define ");
        printType(this.currSymbolTable.getSymbolinfo(methodDecl.name(),true).getDecl());
        this.builder.append("@"+this.currSymbolTable.getNameOfClass()+"."+methodDecl.name()+"(i8* %this");
        for (var formal : methodDecl.formals())
        {
            this.currSymbolTable=lookupTable.getSymbolTable(formal);
            formal.accept(this);
        }
        this.builder.append("){\n");
        this.defineFunc=false;
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
        this.currInstruction=currInstruction.Return;
        methodDecl.ret().accept(this);
        appendWithIndent("ret ");
        methodDecl.returnType().accept(this);
        this.builder.append(currExpr.getResult()+"\n");
        this.builder.append("}\n");

    }

    @Override
    public void visit(FormalArg formalArg) {
        if (defineFunc){
            this.builder.append(",");
            printType(currSymbolTable.getSymbolinfo(formalArg.name(),false).getDecl());
            this.builder.append("%."+formalArg.name());
            return;
        }
        this.currInstruction=currInstruction.VarDecl;
        appendWithIndent("%"+formalArg.name());
        formalArg.type().accept(this);
        this.builder.append("\n");
        appendWithIndent("store ");
        printType(currSymbolTable.getSymbolinfo(formalArg.name(),false).getDecl());
        this.builder.append("%."+formalArg.name());
        currExpr=null;
        this.builder.append(", ");
        printPointerType(currSymbolTable.getSymbolinfo(formalArg.name(),false).getDecl());
        this.builder.append("%"+formalArg.name());
        this.builder.append("\n");
        updateVarTypeInSymbolTable(currInstruction.getName(),formalArg.name(),"%"+formalArg.name());
    }

    @Override
    public void visit(VarDecl varDecl) {
        this.currInstruction=currInstruction.VarDecl;
        this.appendWithIndent("");
        this.builder.append("%"+varDecl.name());
        varDecl.type().accept(this);
        this.builder.append(" ");
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
        String ifStatment = getNextStatment("loop");
        appendWithIndent("br label %"+ifStatment+"\n");
        this.builder.append(ifStatment+":"+"\n");
        whileStatement.cond().accept(this);
        String WhileStat = getNextStatment("loop");

        String backToCodeStatment = getNextStatment("loop");
        appendWithIndent("br i1 "+currExpr.getResult()+", label %"+WhileStat+" , label %"+backToCodeStatment+"\n");
        this.builder.append(WhileStat+":"+"\n");
        whileStatement.body().accept(this);
        appendWithIndent("br label %"+ifStatment+"\n");
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
        SymbolTable stOfDecl=getSTnameResolution(assignStatement.lv());
        if(this.currSymbolTable==stOfDecl&&currSymbolTable.getMethodEntries().size()==0) {
            this.builder.append("store ");
            printType(stOfDecl.getSymbolinfo(assignStatement.lv(), false).getDecl());
            this.builder.append(currExpr.getResult());
            currExpr = null;
            this.builder.append(", ");
            printPointerType(stOfDecl.getSymbolinfo(assignStatement.lv(), false).getDecl());
            this.builder.append("%" + assignStatement.lv());
            this.builder.append("\n");
        }
        else{
            String pointerToVtable =getNextRegister();
            appendWithIndent(pointerToVtable + " = getelementptr i8,i8* %this, i32 ");

            this.builder.append(this.classOfMaps.get(stOfDecl.getNameOfClass()).getVarMap().get(assignStatement.lv())+"\n");
            String bitCast=getNextRegister();
            appendWithIndent(bitCast+" = bitcast i8* "+pointerToVtable+" to ");
            printPointerType(stOfDecl.getSymbolinfo(assignStatement.lv(), false).getDecl());
            this.builder.append("\n");
            appendWithIndent("store ");
            printType(stOfDecl.getSymbolinfo(assignStatement.lv(), false).getDecl());
            this.builder.append(" "+currExpr.getResult()+", ");
            printPointerType(stOfDecl.getSymbolinfo(assignStatement.lv(), false).getDecl());
            this.builder.append(bitCast+"\n");

        }
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
        if(type.equals("ref")){
            this.builder.append("i8* ");
        }
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
        if(type.equals("ref")){
            this.builder.append("i8** ");
        }

    }

    @Override
    public void visit(AssignArrayStatement assignArrayStatement) {
        SymbolTable stOfDecl = getSTnameResolution(assignArrayStatement.lv());
        String loadArray;
        if (currSymbolTable== stOfDecl  && currSymbolTable.getMethodEntries().size()==0) {
             loadArray = getNextRegister();
            appendWithIndent(loadArray + " = load i32* , i32** %" + assignArrayStatement.lv() + "\n");
        }
        else{
            String pointerToVtable = getNextRegister();
            appendWithIndent(pointerToVtable + " = getelementptr i8,i8* %this, i32 ");

            this.builder.append(" " + this.classOfMaps.get(stOfDecl.getNameOfClass()).getVarMap().get(assignArrayStatement.lv()) + "\n");
            String bitCast = getNextRegister();
            appendWithIndent(bitCast + " = bitcast i8* " + pointerToVtable + " to ");
            printPointerType(stOfDecl.getSymbolinfo(assignArrayStatement.lv(), false).getDecl());
            this.builder.append("\n");
            String result = getNextRegister();
            appendWithIndent(result + " = load ");
            printType(stOfDecl.getSymbolinfo(assignArrayStatement.lv(), false).getDecl());
            this.builder.append(", ");
            printPointerType(stOfDecl.getSymbolinfo(assignArrayStatement.lv(), false).getDecl());
            this.builder.append(bitCast + "\n");
            loadArray=result;

        }
        String checkPositive = getNextRegister();
        assignArrayStatement.index().accept(this);
        appendWithIndent(checkPositive+" = icmp slt i32 "+currExpr.getResult()+" , 0\n");
        String negativeIndex=getNextStatment("arr_alloc");
        String positiveIndex=getNextStatment("arr_alloc");
        appendWithIndent("br i1 "+checkPositive+" , label %"+negativeIndex+", label %"+positiveIndex+"\n");
        this.builder.append(negativeIndex+":\n");
        appendWithIndent("call void @throw_oob()\n");
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
        appendWithIndent("call void @throw_oob()\n");
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
        appendWithIndent("call void @throw_oob()\n");
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
        appendWithIndent("call void @throw_oob()\n");
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
        e.arrayExpr().accept(this);
            String result=getNextRegister();
            appendWithIndent(result+" = load i32 , i32* "+ currExpr.getResult()+"\n");
            currExpr.setResult(result);


    }

    @Override
    public void visit(MethodCallExpr e) {
    e.ownerExpr().accept(this);
    String bitCast= getNextRegister();
    appendWithIndent(bitCast+ " = bitcast i8* "+currExpr.getResult()+" to i8***\n");
    String loadVtable=getNextRegister();
    appendWithIndent(loadVtable+" = load i8**, i8*** "+bitCast+"\n");
    String offset= this.classOfMaps.get(currentClass).getMethodMap().get(e.methodId());
    String pointerToOffset=getNextRegister();
    appendWithIndent(pointerToOffset+" = getelementptr i8*,i8** "+loadVtable+" ,i32 "+offset+"\n");
    String readFromArray=getNextRegister();
    appendWithIndent(readFromArray+" = load i8*, i8** "+pointerToOffset+"\n");
    String castPointer=getNextRegister();
    appendWithIndent(castPointer+" = bitcast i8* " + readFromArray + " to ");
    SymbolTable ofObject=lookupTable.getSymbolTable(lookupTable.getClassDeclName(currentClass));
    SymbolTable stOfDecl=getmethodnameResolution(e.methodId(),ofObject);
    String retType =stOfDecl.getSymbolinfo(e.methodId(),true).getDecl();
    printType(retType);
    this.builder.append("(i8* ");
    ArrayList<String> formalArgs=new ArrayList<>();
    for (var p : funcOfClass.get(currentClass)) {
       if (p.getMethodName().equals(e.methodId())){
           formalArgs= (ArrayList<String>) p.getFormals().clone();
           for ( var formal : p.getFormals()){
               this.builder.append(","+formal);

           }
           break;
       }
    }
    this.builder.append(")*\n");
    String prevResult = currExpr.getResult();
        for ( int i = 0; i<e.actuals().size();i++){
            e.actuals().get(i).accept(this);
            formalArgs.set(i,formalArgs.get(i)+" "+currExpr.getResult());

        }
        String calltoFunc=getNextRegister();
        appendWithIndent(calltoFunc+" = call ");
    printType(retType);
    this.builder.append(castPointer+"(i8* "+prevResult);
    for ( int i = 0; i<e.actuals().size();i++){
        this.builder.append(", " +formalArgs.get(i)+" ");
    }
        this.builder.append(")\n");
    currExpr.setResult(calltoFunc);

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

        SymbolTable stOfDecl = getSTnameResolution(e.id());
        if (stOfDecl.getSymbolinfo(e.id(),false).getRefType()!=null){
            this.currentClass=stOfDecl.getSymbolinfo(e.id(),false).getRefType();
        }
        if (stOfDecl == currSymbolTable&&currSymbolTable.getMethodEntries().size()==0) {
            String reg = getNextRegister();
            appendWithIndent(reg + " = load ");
            printType(stOfDecl.getSymbolinfo(e.id(), false).getDecl());
            this.builder.append(", ");
            printPointerType(stOfDecl.getSymbolinfo(e.id(), false).getDecl());
            this.builder.append(" %" + e.id() + "\n");
            if (currExpr == null) {
                exp = new ExprTranslation(null, null, null, reg);
            } else {
                exp = new ExprTranslation(fatherExpr, null, null, reg);
            }
            currExpr = exp;
        } else {
            if (currInstruction == currInstruction.Return) {
                String pointerToVtable = getNextRegister();
                appendWithIndent(pointerToVtable + " = getelementptr i8,i8* %this, i32 ");

                this.builder.append(" " + this.classOfMaps.get(stOfDecl.getNameOfClass()).getVarMap().get(e.id()) + "\n");
                String bitCast = getNextRegister();
                appendWithIndent(bitCast + " = bitcast i8* " + pointerToVtable + " to ");
                printPointerType(stOfDecl.getSymbolinfo(e.id(), false).getDecl());
                this.builder.append("\n");
                String result = getNextRegister();
                appendWithIndent(result + " = load ");
                printType(stOfDecl.getSymbolinfo(e.id(), false).getDecl());
                this.builder.append(", ");
                printPointerType(stOfDecl.getSymbolinfo(e.id(), false).getDecl());
                this.builder.append(bitCast + "\n");
                currExpr.setResult(result);
            } else {
                String pointerToVtable = getNextRegister();
                appendWithIndent(pointerToVtable + " = getelementptr i8,i8* %this, i32 ");
                this.builder.append(this.classOfMaps.get(stOfDecl.getNameOfClass()).getVarMap().get(e.id()) + "\n");
                String bitCast = getNextRegister();
                appendWithIndent(bitCast + " = bitcast i8* " + pointerToVtable + " to ");
                printPointerType(stOfDecl.getSymbolinfo(e.id(), false).getDecl());
                this.builder.append("\n");
                String loadArray = getNextRegister();
                appendWithIndent(loadArray + " = load ");
                printType(stOfDecl.getSymbolinfo(e.id(), false).getDecl());
                this.builder.append(", ");
                printPointerType(stOfDecl.getSymbolinfo(e.id(), false).getDecl());
                this.builder.append(bitCast + "\n");
                String result = loadArray;
                if (currExpr == null) {
                    exp = new ExprTranslation(null, null, null, result);
                } else {
                    exp = new ExprTranslation(fatherExpr, null, null, result);
                }
                currExpr = exp;
            }

        }
    }

    @Override
    public void visit(ThisExpr e) {
        ExprTranslation exp;

        if(currExpr==null)
        {
            exp=new ExprTranslation(null,null,null,"%this");
        }
        else
        {
            exp=new ExprTranslation(fatherExpr,null,null,"%this");
        }
        currExpr=exp;

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
        currentClass=e.classId();
      String allocate = getNextRegister();
        ExprTranslation exp;
        if(currExpr==null)
        {
            exp=new ExprTranslation(null,null,null,allocate);
        }
        else
        {
            exp=new ExprTranslation(fatherExpr,null,null,allocate);
        }
        currExpr=exp;
      int numOfFileds =this.classOfMaps.get(e.classId()).getVarMap().size()*4+8;//todo change
      appendWithIndent(allocate+" = call i8* @calloc(i32 1, i32 "+numOfFileds+")\n");
       String castPointer = getNextRegister();
       appendWithIndent(castPointer+" = bitcast i8* "+ allocate+" to i8***\n");
       int numOfMethods=this.classOfMaps.get(e.classId()).getMethodMap().size();
       String pointerToVtable=getNextRegister();
       appendWithIndent(pointerToVtable+ " = getelementptr ["+numOfMethods + " x i8*], ["+numOfMethods + " x i8*]* @."+e.classId()+"_vtable, i32 0, i32 0\n");
       appendWithIndent("store i8** "+ pointerToVtable +", i8*** "+castPointer+"\n");

    }

    @Override
    public void visit(NotExpr e) {
        e.e().accept(this);
        String notE = getNextRegister();
        appendWithIndent(notE +" = sub i1 1, "+currExpr.getResult()+"\n");
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
        if(this.currInstruction==currInstruction.Return)
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
        if(this.currInstruction==currInstruction.Return)
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
        if(this.currInstruction==currInstruction.Return)
        {
            appendWithIndent("i32* ");
        }
    }

    @Override
    public void visit(RefType t) {
        if(this.currInstruction==currInstruction.VarDecl)
        {
            currInstruction=currInstruction.VarDeclIntArray;
            this.builder.append("= alloca i8*");
        }
        if(this.currInstruction==currInstruction.MethodDecl)
        {
            appendWithIndent("i8* ");
        }
        if(this.currInstruction==currInstruction.Return)
        {
            appendWithIndent("i8* ");
        }
    }
}

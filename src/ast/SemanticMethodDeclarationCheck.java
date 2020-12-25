package ast;

import java.io.PrintWriter;
import java.util.*;

public class SemanticMethodDeclarationCheck implements Visitor{

    Map<String,Set<String>> childrenHierarchyMap;
    Map<String,Set<String>> fathersHierarchyMap;
    Map<String, ArrayList<MethodOfClass>> methodOfClasses;
    private PrintWriter outfile;
    private ExprTranslation currExpr;



    public SemanticMethodDeclarationCheck(LookupTable lookupTable,Map<String,Set<String>> childrenHierarchyMap, Map<String,Set<String>> fathersHierarchyMap, Map<String, ArrayList<MethodOfClass>> methodOfClasses,PrintWriter outfile){
        this.childrenHierarchyMap = childrenHierarchyMap;
        this.fathersHierarchyMap = fathersHierarchyMap;
        this.methodOfClasses= methodOfClasses;
        this.lookupTable=lookupTable;
        this.outfile=outfile;


    }

    //STATE VARIABLES

    private Map<String, Set<MethodSemanticCheckInfo>> classMethods = new HashMap<>();
    private String currClassCheck;
    private boolean updatingMethodFields;
    private String currRetType;
    private String oldArgType;
    private String newArgType;
    private String retType;
    private String returnType;
    private boolean updatingOldArgType;
    private boolean updatingNewArgType;
    private boolean checkRetType;
    private boolean checkReturnType;
    private boolean arraylength;
    private SymbolTable currSymbolTable;
    private LookupTable lookupTable;
    private String currExprOwnerType;
    private boolean checkExprOwner;
    private boolean methodActualCheck;
    private String currMethodActual;



    public void RaiseError(){
        outfile.write("ERROR\n");
        outfile.flush();
        outfile.close();
        System.exit(0);


    };


    public SemanticMethodDeclarationCheck(LookupTable lookupTable){
        this.updatingMethodFields=false;
        this.updatingOldArgType=false;
        this.updatingNewArgType=false;
        this.lookupTable=lookupTable;

    }

    public Set<String> getFathers(String className){
        return fathersHierarchyMap.get(className);
    }

    public Set<String> getChildren(String className){
        return childrenHierarchyMap.get(className);
    }

    private SymbolTable getSTnameResolution(SymbolTable symbolTableOfDecl,String name)
    {
        if(symbolTableOfDecl.isInVarEntries(name))
        {
            return symbolTableOfDecl;
        }
        SymbolTable fatherSymbolTable=symbolTableOfDecl.getFatherSymbolTable();
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

    //Returns type of reference identifier
    private String findType(IdentifierExpr e)
    {
        SymbolTable stOfDecl=getSTnameResolution(currSymbolTable,e.id());
        if (stOfDecl==null){ //(14)
            RaiseError();
        }
        return stOfDecl.getSymbolinfo(e.id(), false).getRefType();
    }

    //Returns type of reference method
    private String findType(MethodCallExpr e)
    {
        SymbolTable stOfDecl=getSTnameResolution(currSymbolTable,e.methodId());
        if (stOfDecl==null){ //(14)
            RaiseError();
        }
        return stOfDecl.getSymbolinfo(e.methodId(), true).getRefType();
    }

    private String findType(String id)
    {
        SymbolTable stOfDecl=getSTnameResolution(currSymbolTable, id);
        if (stOfDecl==null){ //(14)
            RaiseError();
        }
        return stOfDecl.getSymbolinfo(id, false).getRefType();
    }

    private boolean isItLegalMethod(MethodSemanticCheckInfo oldMethod,MethodSemanticCheckInfo newMethod) {
        //check retType
        if(!oldMethod.getReturnType().equals(newMethod.getReturnType()))
        {
            if(     oldMethod.getReturnType().equals("int") && !newMethod.getReturnType().equals("int")||
                    oldMethod.getReturnType().equals("intArr") && !newMethod.getReturnType().equals("intArr")||
                    oldMethod.getReturnType().equals("bool") && !newMethod.getReturnType().equals("bool")){

                RaiseError();
            }
            if(     !oldMethod.getReturnType().equals("int") && newMethod.getReturnType().equals("int")||
                    !oldMethod.getReturnType().equals("intArr") && newMethod.getReturnType().equals("intArr")||
                    !oldMethod.getReturnType().equals("bool") && newMethod.getReturnType().equals("bool")){

                RaiseError();
            }
            if(!getFathers(oldMethod.getReturnType()).contains(newMethod.getReturnType()))
            {
                return false;
            }
        }

        //checkFormals
        if(oldMethod.getFormalArgs().size()!=newMethod.getFormalArgs().size())
        {
            return false;
        }
        for (int i=0;i<oldMethod.getFormalArgs().size();i++)
        {
            FormalArg oldArg= oldMethod.getFormalArgs().get(i);
            FormalArg newArg= newMethod.getFormalArgs().get(i);
            updatingOldArgType=true;
            oldArg.type().accept(this);
            updatingOldArgType=false;
            updatingNewArgType=true;
            newArg.type().accept(this);
            updatingNewArgType=false;
            if (!oldArgType.equals(newArgType))
            {
                return false;
            }
        }
        return true;
    }



    @Override
    public void visit(Program program) {

        for (ClassDecl classdecl : program.classDecls()) {
            classdecl.accept(this);
        }
        program.mainClass().accept(this);

    }

    @Override
    public void visit(ClassDecl classDecl) {
        currClassCheck = classDecl.name();
        String superClassName = classDecl.superName();
        if(superClassName == null) {
            classMethods.put(currClassCheck, new HashSet<>());
        }

        // class extends, and we take its set of field names.
        else{
            classMethods.put(currClassCheck, classMethods.get(superClassName));
        }

        for(var methodDecl : classDecl.methoddecls()){
            this.currSymbolTable=lookupTable.getSymbolTable(classDecl);
            updatingMethodFields = true;
            methodDecl.accept(this);
        }
        updatingMethodFields=false;
    }

    @Override
    public void visit(MainClass mainClass) {
        mainClass.mainStatement().accept(this);
    }

    @Override
    public void visit(MethodDecl methodDecl) {
        checkReturnType=true;
        methodDecl.returnType().accept(this);
        checkReturnType=false;

        for(var method : classMethods.get(currClassCheck))
        {
            if(method.getName().equals(methodDecl.name()))
            {
                MethodSemanticCheckInfo newMethod=new MethodSemanticCheckInfo(methodDecl.name(),currRetType,methodDecl.formals(),currClassCheck);
                if(method.getClassDecl().equals(newMethod.getClassDecl()))
                {
                    //The same name cannot be used for the same method in one class - overloading is not supported. (5)
                    RaiseError();
                }
                if(isItLegalMethod(method,newMethod))
                {
                    classMethods.get(currClassCheck).remove(method);
                    classMethods.get(currClassCheck).add(newMethod);
                    return;
                }
                else
                {
                    //An overriding method matches the ancestor's method signature(6)
                    RaiseError();
                }
            }
        }
        for (var varDecl : methodDecl.vardecls()) {
            this.currSymbolTable=lookupTable.getSymbolTable(varDecl);
            varDecl.accept(this);
        }
        //new method
        MethodSemanticCheckInfo newMethod=new MethodSemanticCheckInfo(methodDecl.name(),currRetType,methodDecl.formals(),currClassCheck);
        classMethods.get(currClassCheck).add(newMethod);
        checkRetType=true;
        methodDecl.ret().accept(this);
        checkRetType=false;
        if(     retType.equals("int") && !returnType.equals("int")||
                retType.equals("intArr") && !returnType.equals("intArr")||
                retType.equals("bool") && !returnType.equals("bool")){

            RaiseError();
        }
        if(     !retType.equals("int") && returnType.equals("int")||
                !retType.equals("intArr") && returnType.equals("intArr")||
                !retType.equals("bool") && returnType.equals("bool")){

            RaiseError();
        }
        if (!retType.equals(returnType)){
            //The static type of e in return e is valid according to the definition of the current method. Note subtyping!(18)
            if(!getFathers(retType).contains(returnType)) {

                RaiseError();
            }

        }
        for (var formal : methodDecl.formals()) {
            this.currSymbolTable=lookupTable.getSymbolTable(formal);
            formal.accept(this);
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

    }

    @Override
    public void visit(WhileStatement whileStatement) {

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
        assignArrayStatement.rv().accept(this);

    }
    private void visitBinaryExpr(BinaryExpr e) {
        ExprTranslation exp,e1,e2;
        exp=new ExprTranslation(null,null,null,null);
        currExpr=exp;
        e.e1().accept(this);
        exp.setE1(currExpr);
        currExpr=exp;
        e.e2().accept(this);
        exp.setE2(currExpr);
        currExpr=exp;

    }
    @Override
    public void visit(AndExpr e) {
        visitBinaryExpr(e);
        if(checkRetType){
            retType="bool";
        }
        if (checkReturnType){
            retType="bool";
        }
        if(checkExprOwner){
            currExprOwnerType="bool";
        }

    }

    @Override
    public void visit(LtExpr e) {
        visitBinaryExpr(e);
        if(checkRetType){
            retType="bool";
        }
        if (checkReturnType){
            retType="bool";
        }
        if(checkExprOwner){
            currExprOwnerType="bool";
        }
    }

    @Override
    public void visit(AddExpr e) {
        visitBinaryExpr(e);

        if (!currExpr.getE1().getResult().equals("int")||
                !currExpr.getE2().getResult().equals("int")){ //(21)
            RaiseError();
        }
        else {
            currExpr.setResult("int"); //(17)
        }
        if(checkRetType){
            retType="int";
        }
        if (checkReturnType){
            retType="int";
        }
        if(checkExprOwner){
            currExprOwnerType="int";
        }
    }

    @Override
    public void visit(SubtractExpr e) {
        visitBinaryExpr(e);

        if (!currExpr.getE1().getResult().equals("int")||
                !currExpr.getE2().getResult().equals("int")){ //(21)
            RaiseError();
        }
        else {
            currExpr.setResult("int"); //(17)
        }
        if(checkRetType){
            retType="int";
        }
        if (checkReturnType){
            retType="int";
        }
        if(checkExprOwner){
            currExprOwnerType="int";
        }
    }

    @Override
    public void visit(MultExpr e) {
        visitBinaryExpr(e);

        if (!currExpr.getE1().getResult().equals("int")||
                !currExpr.getE2().getResult().equals("int")){ //(21)
            RaiseError();
        }
        else {
            currExpr.setResult("int"); //(17)
        }
        if(checkRetType){
            retType="int";
        }
        if (checkReturnType){
            retType="int";
        }
        if(checkExprOwner){
            currExprOwnerType="int";
        }
    }

    @Override
    public void visit(ArrayAccessExpr e) {
        ExprTranslation exp;

        if (currExpr == null) {
            exp = new ExprTranslation(null, null, null, "int");
        } else {
            exp = new ExprTranslation(currExpr, null, null, "int");
        }
        currExpr = exp;
        if(checkRetType){ //(18)
            retType="int";
        }
        if (checkReturnType){ //(18)
            returnType="int";
        }
        if (checkExprOwner){
            currExprOwnerType="int";
        }
    }

    @Override
    public void visit(ArrayLengthExpr e) {
         arraylength = true;
        e.arrayExpr().accept(this);
        arraylength = false;
        if(checkRetType){ //(18)
            retType="int";
        }
        if (checkReturnType){ //(18)
            returnType="int";
        }
        if (checkExprOwner){
            currExprOwnerType="int";
        }
        ExprTranslation exp;

        if (currExpr == null) {
            exp = new ExprTranslation(null, null, null, "int");
        } else {
            exp = new ExprTranslation(currExpr, null, null, "int");
        }
        currExpr = exp;
    }

    @Override
    public void visit(MethodCallExpr e) { //(11)

        //Checking that the owner expression is valid (it is refrence, and f is defined in its class)
        if(checkExprOwner){ //(12)
            RaiseError();
        }
        checkExprOwner = true;
        e.ownerExpr().accept(this);
        checkExprOwner = false;

        if( currExprOwnerType.equals("int")||
            currExprOwnerType.equals("intArr")||
            currExprOwnerType.equals("bool")){

                RaiseError();

        }

        if(currExprOwnerType.equals("this")){
            boolean iserror=true;
            for ( var method :methodOfClasses.get(currClassCheck) ) {


                if (!method.getMethodName().equals(e.methodId())) {
                    iserror=false;
                    break;
                }
            }
            if (iserror){
                RaiseError();
            }

        }

        // ExprOwner is a refrence variable
        else{
            boolean error=true;
            for (var something : methodOfClasses.get(currExprOwnerType))
            {
                if (something.getMethodName().equals(e.methodId())){
                    error=false;
                    break;
                }
            }
            if (error){
                RaiseError();
            }
        }

        MethodOfClass currmethod=null;
        //Checking that method actual parameters are valid
        if (currExprOwnerType.equals("this")){
            currExprOwnerType=currClassCheck;
        }
        for ( var check : methodOfClasses.get(currExprOwnerType)){
            if (check.getMethodName().equals(e.methodId())){
                currmethod=check;
                break;
            }
        }
        int index=0;
        if(e.actuals().size()!=currmethod.getFormals().size()){
            RaiseError();
        }
        for(var actual : e.actuals()){
            methodActualCheck = true;
            actual.accept(this);
            methodActualCheck = false;
            if(     currMethodActual.equals("int") && !currmethod.getFormals().get(index).equals("int")||
                    currMethodActual.equals("intArr") && !currmethod.getFormals().get(index).equals("intArr")||
                    currMethodActual.equals("bool") && !currmethod.getFormals().get(index).equals("bool")){

                RaiseError();
            }
            if (!currMethodActual.equals(currmethod.getFormals().get(index))) {
                if (!getFathers(currMethodActual).contains(currmethod.getFormals().get(index))) {
                    RaiseError();

                }
            }
            index++;


        }
        if(checkRetType){
            retType=currmethod.getDecl();
        }
        ExprTranslation exp;
        String type=null;
        for ( var check : methodOfClasses.get(currExprOwnerType)){
            if (check.getMethodName().equals(e.methodId())){
                type=check.getDecl();
            }
        }
        if(type == null){
            RaiseError();
        }
        if (currExpr == null) {
            exp = new ExprTranslation(null, null, null, type);
        } else {
            exp = new ExprTranslation(currExpr, null, null, type);
        }
        currExpr = exp;
        if(arraylength){
                if (!type.equals("intArr")){
                    RaiseError();
                }
        }
    }

    @Override
    public void visit(IntegerLiteralExpr e) {
        if(checkRetType){ //(18)
            retType="int";
        }
        if (checkReturnType){ //(18)
            returnType="int";
        }
        if (checkExprOwner){
            currExprOwnerType="int";
        }
        ExprTranslation exp;

        if (currExpr == null) {
            exp = new ExprTranslation(null, null, null, "int");
        } else {
            exp = new ExprTranslation(currExpr, null, null, "int");
        }
        currExpr = exp;

        if(methodActualCheck){
            currMethodActual="int";
        }
    }

    @Override
    public void visit(TrueExpr e) {
        ExprTranslation exp;

        if (currExpr == null) {
            exp = new ExprTranslation(null, null, null, "bool");
        } else {
            exp = new ExprTranslation(currExpr, null, null, "bool");
        }
        currExpr = exp;
        if(checkRetType){//(18)
            retType="bool";
        }
        if(methodActualCheck){
            currMethodActual="bool";
        }
    }

    @Override
    public void visit(FalseExpr e) {
        ExprTranslation exp;

        if (currExpr == null) {
            exp = new ExprTranslation(null, null, null, "bool");
        } else {
            exp = new ExprTranslation(currExpr, null, null, "bool");
        }
        currExpr = exp;
        if(checkRetType){ //(18)
            retType="boolean";
        }
        if(methodActualCheck){
            currMethodActual="bool";
        }
    }

    @Override
    public void visit(IdentifierExpr e) {
        String type=findType(e);
        if(checkRetType){//(18)
            retType =type;
        }
        if(checkExprOwner){//(11)
            currExprOwnerType = type;
        }
        if (arraylength && !checkExprOwner ){
            if (!type.equals("intArr"))
            {
                RaiseError();
            }
        }
        if (methodActualCheck){
            currMethodActual=type;
        }
        ExprTranslation exp;

        if (currExpr == null) {
            exp = new ExprTranslation(null, null, null, type);
        } else {
            exp = new ExprTranslation(currExpr, null, null, type);
        }
        currExpr = exp;
    }


    @Override
    public void visit(ThisExpr e) {
        if(checkExprOwner){ //(11)
            currExprOwnerType = "this";
        }
        if(checkRetType){ //(18)
            retType=currClassCheck;
        }
        if (checkReturnType){ //(18)
            returnType=currClassCheck;
        }
        if(methodActualCheck){
            currMethodActual=currClassCheck;
        }


    }

    @Override
    public void visit(NewIntArrayExpr e) {

    }

    @Override
    public void visit(NewObjectExpr e) {
        if(checkExprOwner) { //(11)
            currExprOwnerType = e.classId();
        }
        if(checkRetType){
            retType=e.classId();
        }
        if (checkReturnType){
            retType=e.classId();
        }
        if(methodActualCheck){
            currMethodActual=e.classId();
        }
    }

    @Override
    public void visit(NotExpr e) {

    }

    @Override
    public void visit(IntAstType t) {
        if(updatingMethodFields)
        {
            currRetType="int";
        }
        if(updatingNewArgType)
        {
            newArgType="int";
        }
        if(updatingOldArgType)
        {
            oldArgType="int";
        }
        if(checkRetType){//(18)
            retType="int";
        }
        if (checkReturnType){//(18)
            returnType="int";
        }
        if(methodActualCheck){ //(11)
            currMethodActual="int";
        }
    }

    @Override
    public void visit(BoolAstType t) {
        if(updatingMethodFields)
        {
            currRetType="bool";
        }
        if(updatingNewArgType)
        {
            newArgType="bool";
        }
        if(updatingOldArgType)
        {
            oldArgType="bool";
        }
        if(checkRetType){//(18)
            retType="bool";
        }
        if (checkReturnType){//(18)
            returnType="bool";
        }
        if(methodActualCheck){ //(11)
            currMethodActual="bool";
        }
    }

    @Override
    public void visit(IntArrayAstType t) {
        if(updatingMethodFields)
        {
            currRetType="intArray";
        }
        if(updatingNewArgType)
        {
            newArgType="intArray";
        }
        if(updatingOldArgType)
        {
            oldArgType="intArray";
        }
        if(checkRetType){//(18)
            retType="intArr";
        }
        if (checkReturnType){//(18)
            returnType="intArr";
        }
        if(methodActualCheck){ //(11)
            currMethodActual="intArr";
        }
    }

    @Override
    public void visit(RefType t) {
        if(updatingMethodFields)
        {
            currRetType=t.id();
        }
        if(updatingNewArgType)
        {
            newArgType=t.id();
        }
        if(updatingOldArgType)
        {
            oldArgType=t.id();
        }
        if(checkRetType){ //(18)
            retType=t.id();
        }
        if (checkReturnType){//(18)
            returnType=t.id();
        }
        if(methodActualCheck){ //(11)
            currMethodActual=t.id();
        }
    }
}

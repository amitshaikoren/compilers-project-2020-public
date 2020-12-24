package ast;

import java.io.PrintWriter;
import java.util.*;

public class SemanticMethodDeclarationCheck implements Visitor{

    Map<String,Set<String>> childrenHierarchyMap;
    Map<String,Set<String>> fathersHierarchyMap;
    Map<String, ArrayList<MethodOfClass>> methodOfClasses;
    private PrintWriter outfile;



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
            updatingOldArgType=true;
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
        if (!retType.equals(returnType)){
            //The static type of e in return e is valid according to the definition of the current method. Note subtyping!(18)
            if(!getFathers(retType).contains(returnType)) {

                RaiseError();
            }

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
            if(!classMethods.get(currClassCheck).contains(e.methodId())){
                RaiseError();
            }
        }

        // ExprOwner is a refrence variable
        else{
            boolean error=true;
            for (var something : classMethods.get(currExprOwnerType))
            {
                if (something.getName().equals(e.methodId())){
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
        for ( var check : methodOfClasses.get(currExprOwnerType)){
            if (check.getMethodName().equals(e.methodId())){
                currmethod=check;
                break;
            }
        }
        int index=0;
        for(var actual : e.actuals()){
            methodActualCheck = true;
            actual.accept(this);
            methodActualCheck = false;
            if(     currMethodActual.equals("int") && !currmethod.getFormals().get(index).equals("int")||
                    currMethodActual.equals("intArr") && !currmethod.getFormals().get(index).equals("intArr")||
                    currMethodActual.equals("bool") && !currmethod.getFormals().get(index).equals("bool")){

                RaiseError();
            }
            if (!getFathers(currMethodActual).contains(currmethod.getFormals().get(index))){
                RaiseError();

            }
            index++;


        }
        if(checkRetType){
            retType=currmethod.getDecl();
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
    }

    @Override
    public void visit(TrueExpr e) {

        if(checkRetType){//(18)
            retType="boolean";
        }
    }

    @Override
    public void visit(FalseExpr e) {
        if(checkRetType){ //(18)
            retType="boolean";
        }
    }

    @Override
    public void visit(IdentifierExpr e) {
        if(checkRetType){//(18)
            retType =findType(e);
        }
        if(checkExprOwner){//(11)
            currExprOwnerType = findType(e);
        }
    }


    @Override
    public void visit(ThisExpr e) {
        if(checkExprOwner){ //(11)
            currExprOwnerType = "this";
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
            retType="boolean";
        }
        if (checkReturnType){//(18)
            returnType="boolean";
        }
        if(methodActualCheck){ //(11)
            currMethodActual="boolean";
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

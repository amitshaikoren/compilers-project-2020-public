package ast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SemanticMethodDeclarationCheck implements Visitor{

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

    public SemanticMethodDeclarationCheck(LookupTable lookupTable){
        this.updatingMethodFields=false;
        this.updatingOldArgType=false;
        this.updatingNewArgType=false;
        this.lookupTable=lookupTable;

    }

    public void RaiseError(){}

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

    private boolean isItLegalMethod(MethodSemanticCheckInfo oldMethod,MethodSemanticCheckInfo newMethod) {
        //check retType
        if(!oldMethod.getReturnType().equals(newMethod.getReturnType()))
        {
            if(!SemanticCheckClassHierarchy.findFathers(oldMethod.getReturnType()).contains(newMethod.getReturnType()))
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
        //new method
        MethodSemanticCheckInfo newMethod=new MethodSemanticCheckInfo(methodDecl.name(),currRetType,methodDecl.formals(),currClassCheck);
        classMethods.get(currClassCheck).add(newMethod);
        checkRetType=true;
        methodDecl.ret().accept(this);
        checkRetType=false;
        if (!retType.equals(retType)){
            //The static type of e in return e is valid according to the definition of the current method. Note subtyping!(18)
            if(!SemanticCheckClassHierarchy.findFathers(retType).contains(returnType)) {

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
            if(!classMethods.get(currExprOwnerType).contains(e.methodId())){
                RaiseError();
            }
        }

        //Checking that method formal arguments are valid
        if(){

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
    }
}

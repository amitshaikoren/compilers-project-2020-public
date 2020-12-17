package ast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SemanticMethodDeclarationCheck implements Visitor{

    //STATE VARIABLES

    private Map<String, Set<MethodSemanticCheckInfo>> classMethods = new HashMap<>();
    private String currClassCheck;
    private boolean updatingMethodFields=false;
    private String currRetType;
    private String oldArgType;
    private String newArgType;
    private boolean updatingOldArgType=false;
    private boolean updatingNewArgType=false;



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
        methodDecl.returnType().accept(this);
        for(var method : classMethods.get(currClassCheck))
        {
            if(method.getName().equals(methodDecl.name()))
            {
                MethodSemanticCheckInfo newMethod=new MethodSemanticCheckInfo(methodDecl.name(),currRetType,methodDecl.formals(),currClassCheck);
                if(method.getClassDecl().equals(newMethod.getClassDecl()))
                {
                    //The same name cannot be used for the same method in one class - overloading is not supported. (5)
                    SemanticClassAndVarCheckVisitor.RaiseError();
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
                    SemanticClassAndVarCheckVisitor.RaiseError();
                }
            }
        }
        //new method
        MethodSemanticCheckInfo newMethod=new MethodSemanticCheckInfo(methodDecl.name(),currRetType,methodDecl.formals(),currClassCheck);
        classMethods.get(currClassCheck).add(newMethod);

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
    }
}

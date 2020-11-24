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

    static int count=-1;
    private int indent = 0;
    private StringBuilder builder = new StringBuilder();

    public String getString() {
        return this.builder.toString();
    }

    private void appendWithIndent(String str) {
        this.builder.append("\t".repeat(this.indent));
        this.builder.append(str);
    }

    private static String getNextRegister()
    {
     count++;
     String reg;
     reg="%_"+Integer.toString(count);
     return reg;
    }

    @Override
    public void visit(Program program) {
        this.builder.append(code);
        program.mainClass().accept(this);
        for (ClassDecl classdecl : program.classDecls()) {
            classdecl.accept(this);
        }
        this.builder.append("}");
    }

    @Override
    public void visit(ClassDecl classDecl) {
        for (var fieldDecl : classDecl.fields()) {
            fieldDecl.accept(this);
        }
        for (var methodDecl : classDecl.methoddecls()) {
            methodDecl.accept(this);
        }

    }

    @Override
    public void visit(MainClass mainClass) {

    }

    @Override
    public void visit(MethodDecl methodDecl) {
        methodDecl.returnType().accept(this);
        for (var formal : methodDecl.formals())
        {
            formal.accept(this);
        }
        for (var varDecl : methodDecl.vardecls()) {
            varDecl.accept(this);
        }
        for (var stmt : methodDecl.body()) {
            stmt.accept(this);
        }
        methodDecl.ret().accept(this);
    }

    @Override
    public void visit(FormalArg formalArg) {

    }

    @Override
    public void visit(VarDecl varDecl) {
        this.appendWithIndent("");
        this.builder.append("%"+varDecl.name());
        varDecl.type().accept(this);

        this.builder.append(" ");
        //this.builder.append(varDecl.name());
        this.builder.append(";\n");
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

        this.builder.append("= alloca i32");
    }

    @Override
    public void visit(BoolAstType t) {

    }

    @Override
    public void visit(IntArrayAstType t) {

    }

    @Override
    public void visit(RefType t) {

    }
}

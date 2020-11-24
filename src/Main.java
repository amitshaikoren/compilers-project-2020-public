import ast.*;

import java.io.*;


public class Main {

    public static SymbolTable searchRootDeclOfMethod(SymbolTable symbolTable,MethodDecl method){
        SymbolTable fatherSymoblTable = symbolTable.getFatherSymbolTable();

        while(fatherSymoblTable != null){
            if (fatherSymoblTable.getSymbolinfo(method.name(), true) != null){
                symbolTable = fatherSymoblTable;
            }
            fatherSymoblTable = fatherSymoblTable.getFatherSymbolTable();
        }

        return symbolTable;
    }

    private static boolean nameIsEquals(AstNode astNode, String originalName) {
        if (astNode instanceof MethodDecl) {
            return ((MethodDecl)astNode).name().equals(originalName);
        }
        if (astNode instanceof FormalArg) {
            return ((FormalArg)astNode).name().equals(originalName);
        }
        if (astNode instanceof VarDecl) {
            return ((VarDecl)astNode).name().equals(originalName);
        }
        return false;
    }

    public static void main(String[] args) {
        try {
            var inputMethod = args[0];
            var action = args[1];
            var filename = args[args.length - 2];
            var outfilename = args[args.length - 1];

            Program prog;

            if (inputMethod.equals("parse")) {
                throw new UnsupportedOperationException("TODO - Ex. 4");
            } else if (inputMethod.equals("unmarshal")) {
                AstXMLSerializer xmlSerializer = new AstXMLSerializer();
                prog = xmlSerializer.deserialize(new File(filename));
            } else {
                throw new UnsupportedOperationException("unknown input method " + inputMethod);
            }

            var outFile = new PrintWriter(outfilename);
            try {

                if (action.equals("marshal")) {
                    AstXMLSerializer xmlSerializer = new AstXMLSerializer();
                    xmlSerializer.serialize(prog, outfilename);
                } else if (action.equals("print")) {
                    AstPrintVisitor astPrinter = new AstPrintVisitor();
                    astPrinter.visit(prog);
                    outFile.write(astPrinter.getString());

                } else if (action.equals("semantic")) {
                    throw new UnsupportedOperationException("TODO - Ex. 3");

                } else if (action.equals("compile")) {
                    TranslateAstToLlvmVisitor translator = new TranslateAstToLlvmVisitor();
                    translator.visit(prog);
                    outFile.write(translator.getString());

                } else if (action.equals("rename")) {
                    var type = args[2];
                    var originalName = args[3];
                    var originalLine = args[4];
                    var newName = args[5];

                    boolean isMethod;
                    if (type.equals("var")) {
                        isMethod = false;
                    } else if (type.equals("method")) {
                        isMethod = true;
                    } else {
                        throw new IllegalArgumentException("unknown rename type " + type);
                    }

                    LookupTable lookupTable = new LookupTable();

                    AstCreateSymbolTableVisitor symbolTableVistor  = new AstCreateSymbolTableVisitor(lookupTable);
                    symbolTableVistor.visit(prog);

                    SymbolTable symbolTableOfOriginalName=null;
                    MethodDecl methodDecl=null;
                    for(var astNode : lookupTable.getLookupTable().keySet()) {
                        boolean flag=nameIsEquals(astNode,originalName);
                        if (astNode.lineNumber!=null && astNode.lineNumber.equals(Integer.valueOf(originalLine))&& flag) {
                            symbolTableOfOriginalName = lookupTable.getSymbolTable(astNode);
                            if((symbolTableOfOriginalName.isInMethodEntries(originalName)&&isMethod)||(symbolTableOfOriginalName.isInVarEntries(originalName)&&!isMethod))
                            {
                                if(astNode instanceof MethodDecl)
                                    methodDecl=(MethodDecl)astNode;
                                break;
                            }
                        }
                    }
                    if(isMethod)
                    {
                        symbolTableOfOriginalName =searchRootDeclOfMethod(symbolTableOfOriginalName,methodDecl);
                    }
                    AstRenamingVisitor renamingVisitor = new AstRenamingVisitor(originalName, newName, lookupTable, symbolTableOfOriginalName, isMethod);
                    renamingVisitor.visit(prog);
                    AstXMLSerializer xmlSerializer = new AstXMLSerializer();
                    xmlSerializer.serialize(prog, outfilename);
                } else {
                    throw new IllegalArgumentException("unknown command line action " + action);
                }
            } finally {
                outFile.flush();
                outFile.close();
            }

        } catch (FileNotFoundException e) {
            System.out.println("Error reading file: " + e);
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("General error: " + e);
            e.printStackTrace();
        }
    }


}

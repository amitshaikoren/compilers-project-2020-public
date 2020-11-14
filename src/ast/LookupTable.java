package ast;

import java.util.HashMap;
import java.util.Map;

public class LookupTable {
    private Map<AstNode, SymbolTable> lookupTable = new HashMap<>();
    private Map<String, AstNode> classDeclMap = new HashMap<>();


    public void updateLookupTable(AstNode astNode, SymbolTable symbolTable){
        lookupTable.put(astNode, symbolTable);
    }
    public Map<AstNode, SymbolTable> getLookupTable() {
        return lookupTable;
    }
    public void updateclassDeclMap(String name, AstNode astNode){
        classDeclMap.put(name, astNode);
    }
    public AstNode getClassDeclName(String name){
        return  classDeclMap.get(name);
    }
    public SymbolTable getSymbolTable(AstNode astNode){
        return  lookupTable.get(astNode);
    }

}

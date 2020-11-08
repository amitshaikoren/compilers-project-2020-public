package ast;

import java.util.Map;

public class SymbolTable {
    String name;
    private Map<String, SymbolInfo> entries;
    private SymbolTable parentSymbolTable;
    public SymbolTable(String name,SymbolTable parentSymbolTable)
    {
        this.name=name;
        this.parentSymbolTable=parentSymbolTable;
    }
}



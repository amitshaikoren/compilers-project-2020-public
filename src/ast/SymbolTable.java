package ast;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private SymbolInfo currSymbolInfo;
    private String currSymbolName;
    private String nameOfClass;
    private Map<String, SymbolInfo> entries = new HashMap<>();
    private SymbolTable fatherSymbolTable;



    public String getNameOfClass() {
        return nameOfClass;
    }

    public SymbolTable getFatherSymbolTable() {
        return fatherSymbolTable;
    }

    public  SymbolInfo getSymbolinfo(String Name){
        return this.entries.get(Name);
    }

    public SymbolTable(SymbolTable fatherSymbolTable,String nameOfClass)
    {
        this.fatherSymbolTable=fatherSymbolTable;
        this.nameOfClass=nameOfClass;
    }

    public void updateEntries(){
        if (this.currSymbolName != null && this.currSymbolInfo != null) {
            entries.put(this.currSymbolName, this.currSymbolInfo);
        }
    }

    public void buildSymbolInfo() {
        this.currSymbolInfo = new SymbolInfo();
    }

    public void setCurrSymbolName(String symbolName){
        this.currSymbolName = symbolName;
    }

    //SYMBOL INFO UPDATE METHODS

    public void setSymbolInfoIsMethod(boolean isMethod){
        this.currSymbolInfo.setIsMethod(isMethod);
    }

    public void setSymbolInfoDecl(String decl){
        this.currSymbolInfo.setDecl(decl);
    }
    public  void setCurrSymbolInfoRefType(String refType){
        this.currSymbolInfo.setRefType(refType);
    }

}



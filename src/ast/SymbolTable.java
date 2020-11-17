package ast;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private SymbolInfo currSymbolInfo;
    private String currSymbolName;
    private String nameOfClass;
    private Map<String, SymbolInfo> methodEntries = new HashMap<>();
    private Map<String, SymbolInfo> varEntries = new HashMap<>();
    private SymbolTable fatherSymbolTable;


    public String getNameOfClass() {
        return nameOfClass;
    }

    public SymbolTable getFatherSymbolTable() {
        return fatherSymbolTable;
    }

    public SymbolInfo getSymbolinfo(String Name, boolean isMethod){
        if(isMethod){
            return this.methodEntries.get(Name);
        }
        return this.varEntries.get(Name);
    }
    public boolean isInMethodEntries(String name)
    {
        return (this.methodEntries.get(name)!=null);
    }
    public boolean isInVarEntries(String name)
    {
        return (this.varEntries.get(name)!=null);
    }

    public SymbolTable(SymbolTable fatherSymbolTable, String nameOfClass)
    {
        this.fatherSymbolTable=fatherSymbolTable;
        this.nameOfClass=nameOfClass;
    }

    public void updateEntries(){
        if (this.currSymbolName != null && this.currSymbolInfo != null){
            if(currSymbolInfo.getIsMethod()) {
                methodEntries.put(this.currSymbolName, this.currSymbolInfo);
            }
            else{
                varEntries.put(this.currSymbolName, this.currSymbolInfo);
            }
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



package ast;

import java.util.Set;

public class SemanticCheckClass {
    private String name;
    private SemanticCheckClass superClass;
    private Set<SemanticCheckClass> children;

    public SemanticCheckClass getSuperClass(){
        return superClass;
    }

    public String getName() {
        return name;
    }


    public SemanticCheckClass getClass(String classA){

        if(this.name.equals(classA)){
            return this;
        }

        if(children.isEmpty()){
            return null;
        }

        for(var child : children){
            SemanticCheckClass potentialClass = child.getClass(classA);
            if(potentialClass != null){
                return potentialClass;
            }

        }
        return null;
    }
}

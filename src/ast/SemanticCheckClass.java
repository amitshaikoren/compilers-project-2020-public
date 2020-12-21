package ast;

import java.util.HashSet;
import java.util.Set;

public class SemanticCheckClass {
    private String name;
    private SemanticCheckClass superClass;
    private Set<SemanticCheckClass> children;

    public SemanticCheckClass getSuperClass(){
        return superClass;
    }

    public Set<SemanticCheckClass> getChildren() {
        return children;
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

    public Set<SemanticCheckClass> findChildrenRec(){
        Set<SemanticCheckClass> childrenSet = new HashSet<>();
        childrenSet.add(this);
        if(!this.getChildren().isEmpty()) {
            for (var child : this.getChildren()) {
                childrenSet.addAll(child.findChildrenRec());
            }
        }
        return childrenSet;
    }

}

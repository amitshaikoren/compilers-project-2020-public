package ast;

public class ExprTranslation {
   private ExprTranslation father;
   private ExprTranslation e1;
   private ExprTranslation e2;
   private String result;

    public ExprTranslation(ExprTranslation father, ExprTranslation e1, ExprTranslation e2, String result) {
        this.father = father;
        this.e1 = e1;
        this.e2 = e2;
        this.result = result;
    }


    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }



 /*   String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
*/
}

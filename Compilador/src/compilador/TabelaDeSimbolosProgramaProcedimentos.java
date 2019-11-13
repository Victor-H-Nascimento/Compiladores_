/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compilador;

/**
 *
 * @author 16000465
 */
public class TabelaDeSimbolosProgramaProcedimentos extends TabelaDeSimbolos {
    
   private int label;

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }
   
    public TabelaDeSimbolosProgramaProcedimentos(String lexema,int label) {
        this.setLexema(lexema);
        this.setEscopo(true);
        this.setLabel(label);
    }
    
    public TabelaDeSimbolosProgramaProcedimentos(String lexema) {// so para o programa
        this.setLexema(lexema);
        this.setEscopo(true);
        this.setLabel(label);
    }
    
}

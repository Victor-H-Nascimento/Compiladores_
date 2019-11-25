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
public class TabelaDeSimbolosFuncoes extends TabelaDeSimbolos {

    String tipo;
    private int label;

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public TabelaDeSimbolosFuncoes(String lexema,int label) {
        this.setLexema(lexema);
        this.setEscopo(true);
        this.setLabel(label);
    }
    
    

}

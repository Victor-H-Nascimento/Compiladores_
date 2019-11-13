/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compilador;

/**
 *
 * @author victor
 */
public class Token {
    private String simbolo;
    private String lexema;
    private int linhaCodigo;

    public Token() {
    }

    public String getSimbolo() {
        return simbolo;
    }

    public void setSimbolo(String simbolo) {
        this.simbolo = simbolo;
    }

    public String getLexema() {
        return lexema;
    }

    public void setLexema(String lexema) {
        this.lexema = lexema;
    }

    public int getLinhaCodigo() {
        return linhaCodigo;
    }

    public void setLinhaCodigo(int linhaCodigo) {
        this.linhaCodigo = linhaCodigo;
    }
    
    
}

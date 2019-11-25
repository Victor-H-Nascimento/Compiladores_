/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compilador;

import java.util.ArrayList;

/**
 *
 * @author victor
 */
public class FuncoesPosFixa {

    private final SimbolosToken simbolos;
    private final Operando resultado;

    //construtor
    public FuncoesPosFixa() {
        this.resultado = new Operando();
        this.simbolos = new SimbolosToken();
    }

    public void trataUnitario(ArrayList<ElementosPosFixa> filaPosFixa, int i) {// I -> I

        resultado.setTipo(simbolos.getInteiro());

        Operando aux = (Operando) filaPosFixa.get(i - 1);

        if (aux.getTipo().contentEquals(simbolos.getInteiro())) {
            filaPosFixa.remove(i);
            filaPosFixa.remove(i - 1);
            filaPosFixa.add(i - 1, resultado);
        } else {
            System.out.println("Erro de Compatibilidade");
        }

    }

    public void trataNaoUnitario(ArrayList<ElementosPosFixa> filaPosFixa, int i) {// B -> B
        resultado.setTipo(simbolos.getBooleano());
        Operando aux = (Operando) filaPosFixa.get(i - 1);

        if (aux.getTipo().contentEquals(simbolos.getBooleano())) {
            filaPosFixa.remove(i);
            filaPosFixa.remove(i - 1);
            filaPosFixa.add(i - 1, resultado);
        } else {
            System.out.println("Erro de Compatibilidade");
        }

    }

    public void trataMultDivSomaSub(ArrayList<ElementosPosFixa> filaPosFixa, int i) {// II -> I
        resultado.setTipo(simbolos.getInteiro());
        Operando numerador = (Operando) filaPosFixa.get(i - 1);
        Operando operando = (Operando) filaPosFixa.get(i - 2);

        if (numerador.getTipo().contentEquals(simbolos.getInteiro()) && operando.getTipo().contentEquals(simbolos.getInteiro())) {
            filaPosFixa.remove(i);
            filaPosFixa.remove(i - 1);
            filaPosFixa.remove(i - 2);
            filaPosFixa.add(i - 2, resultado);
        } else {
            System.out.println("Erro de Compatibilidade");
        }

    }

    public void trataRelacionais(ArrayList<ElementosPosFixa> filaPosFixa, int i) {// II -> B
        resultado.setTipo(simbolos.getBooleano());
        Operando numerador = (Operando) filaPosFixa.get(i - 1);
        Operando operando = (Operando) filaPosFixa.get(i - 2);

        if (numerador.getTipo().contentEquals(simbolos.getInteiro()) && operando.getTipo().contentEquals(simbolos.getInteiro())) {
            filaPosFixa.remove(i);
            filaPosFixa.remove(i - 1);
            filaPosFixa.remove(i - 2);
            filaPosFixa.add(i - 2, resultado);
        } else {
            System.out.println("Erro de Compatibilidade");
        }

    }

    public void trataEOu(ArrayList<ElementosPosFixa> filaPosFixa, int i) {// BB -> B
        resultado.setTipo(simbolos.getBooleano());
        Operando numerador = (Operando) filaPosFixa.get(i - 1);
        Operando operando = (Operando) filaPosFixa.get(i - 2);

        if (numerador.getTipo().contentEquals(simbolos.getBooleano()) && operando.getTipo().contentEquals(simbolos.getBooleano())) {
            filaPosFixa.remove(i);
            filaPosFixa.remove(i - 1);
            filaPosFixa.remove(i - 2);
            filaPosFixa.add(i - 2, resultado);
        } else {
            System.out.println("Erro de Compatibilidade");
        }

    }

    public void trataIgualDiferente(ArrayList<ElementosPosFixa> filaPosFixa, int i) {// BB -> B || II -> I
        resultado.setTipo(simbolos.getBooleano());
        Operando numerador = (Operando) filaPosFixa.get(i - 1);
        Operando operando = (Operando) filaPosFixa.get(i - 2);

        if (numerador.getTipo().contentEquals(simbolos.getBooleano()) && operando.getTipo().contentEquals(simbolos.getBooleano())) {
            filaPosFixa.remove(i);
            filaPosFixa.remove(i - 1);
            filaPosFixa.remove(i - 2);
            filaPosFixa.add(i - 2, resultado);
        } else {
            if (numerador.getTipo().contentEquals(simbolos.getInteiro()) && operando.getTipo().contentEquals(simbolos.getInteiro())) {
                filaPosFixa.remove(i);
                filaPosFixa.remove(i - 1);
                filaPosFixa.remove(i - 2);
                filaPosFixa.add(i - 2, resultado);
            } else {
                System.out.println("Erro de Compatibilidade");
            }
        }

    }

}

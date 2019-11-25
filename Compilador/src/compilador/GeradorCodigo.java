/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compilador;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author victor
 */
public class GeradorCodigo {

    
    private String texto = "";


    public GeradorCodigo() throws IOException {
       // FileWriter arq = new FileWriter("saidaCompilador.txt");
        //gravarArq = new PrintWriter(arq);
    }

    public void geraLDC(int valor) {
        adicionaFila("LDC ".concat(Integer.toString(valor)));
    }

    public void geraLDV(int valor) {
        adicionaFila("LDV ".concat(Integer.toString(valor)));
    }

    public void geraADD() {
        adicionaFila("ADD");
    }

    public void geraSUB() {
        adicionaFila("SUB");
    }

    public void geraMULT() {
        adicionaFila("MULT");
    }

    public void geraDIVI() {
        adicionaFila("DIVI");
    }

    public void geraINV() {
        adicionaFila("INV");
    }

    public void geraAND() {
        adicionaFila("AND");
    }

    public void geraOR() {
        adicionaFila("OR");
    }

    public void geraNEG() {
        adicionaFila("NEG");
    }

    public void geraCME() {
        adicionaFila("CME");
    }

    public void geraCMA() {
        adicionaFila("CMA");
    }

    public void geraCEQ() {
        adicionaFila("CEQ");
    }

    public void geraCDIF() {
        adicionaFila("CDIF");
    }

    public void geraCMEQ() {
        adicionaFila("CMEQ");
    }

    public void geraCMAQ() {
        adicionaFila("CMAQ");
    }

    public void geraSTART() {
        adicionaFila("START");
    }

    public void geraHLT() throws IOException {
        adicionaFila("HLT");
        EscreveAssembly();
    }

    public void geraSTR(int valor) {
        adicionaFila("STR ".concat(Integer.toString(valor)));
    }

    public void geraJMP(int valor) {
        adicionaFila("JMP L".concat(Integer.toString(valor)));
    }

    public void geraJMPF(int valor) {
        adicionaFila("JMPF L".concat(Integer.toString(valor)));
    }

    public void geraNULL(int valor) {
        adicionaFila("L".concat(Integer.toString(valor)).concat(" NULL"));
    }

    public void geraRD() {
        adicionaFila("RD");
    }

    public void geraPRN() {
        adicionaFila("PRN");
    }

    public void geraALLOC(int indiceInicial, int quantidade) {
        adicionaFila("ALLOC ".concat(Integer.toString(indiceInicial)).concat(",").concat(Integer.toString(quantidade)));
    }

    public void geraDALLOC(int indiceInicial, int quantidade) {
         adicionaFila("DALLOC ".concat(Integer.toString(indiceInicial)).concat(",").concat(Integer.toString(quantidade)));
    }

    public void geraCALL(int valor) {
        adicionaFila("CALL L".concat(Integer.toString(valor)));
    }

    public void geraRETURN() {
        adicionaFila("RETURN");
    }
    
    public void geraRETURNF(int indiceInicial, int quantidade) {
         adicionaFila("RETURNF ".concat(Integer.toString(indiceInicial)).concat(",").concat(Integer.toString(quantidade)));
    }
    
    public void geraRETURNF() {
         adicionaFila("RETURNF");
    }

    public void adicionaFila(String linha) {
        texto = texto.concat(linha).concat("\n");
        System.out.println(linha);
    }
    
    
    public void EscreveAssembly() throws IOException {
      
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("build/classes/maquina/virtual/saidaAssembly.txt"))) {
            writer.write(texto);
        }
    }

}

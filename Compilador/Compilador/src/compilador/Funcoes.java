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
public class Funcoes {

    //atributos
    SimbolosToken simbolos = new SimbolosToken();
    private final ArrayList<String> listaArquivo = new ArrayList();
    private char[] listaAuxiliar = null;
    private final ArrayList<String> listaCaracter = new ArrayList();
    private int linhaDeCodigo = 0;
    private boolean ultimoCaracterLido = false;
    private  boolean erroExclamacao = false;

    //funcoes Lista de Arquivo
    public void leArquivo(String linhaArquivo) {
        listaArquivo.add(linhaArquivo);
    }

    public int getLinhaCodigo() {
        return linhaDeCodigo;
    }

    public boolean estaVazia() {
        return listaArquivo.isEmpty() && listaCaracter.isEmpty() && ultimoCaracterLido;
    }
    
    public boolean getErroExclamacao() {
        return erroExclamacao;
    }

    public String leCaracter() {

        String aux = null;

        if (!listaArquivo.isEmpty() && listaCaracter.isEmpty()) {
          
            listaAuxiliar = listaArquivo.get(0).toCharArray();
            listaArquivo.remove(0);

            for (char item : listaAuxiliar) {
                listaCaracter.add(Character.toString(item));
            }
            
            listaCaracter.add("\n");
            
            linhaDeCodigo++;
            
            if (!listaCaracter.isEmpty()) {
                aux = listaCaracter.get(0);
            listaCaracter.remove(0);

            }
            
        } else if (!listaArquivo.isEmpty() && !listaCaracter.isEmpty()) {
            aux = listaCaracter.get(0);
            listaCaracter.remove(0);

        } else if (listaArquivo.isEmpty() && !listaCaracter.isEmpty()) {
            aux = listaCaracter.get(0);
            listaCaracter.remove(0);


        } else if (listaArquivo.isEmpty() && listaCaracter.isEmpty()) {
            ultimoCaracterLido = true;
            return "%";
        }

        return aux;
    }

    public String trataDigito(String caracter, Funcoes c, Arquivo arq, Token token) {

        String novoCaracter = c.leCaracter();

        while (novoCaracter.contains("0") || novoCaracter.contains("1") || novoCaracter.contains("2") || novoCaracter.contains("3") || novoCaracter.contains("4") || novoCaracter.contains("5") || novoCaracter.contains("6") || novoCaracter.contains("7") || novoCaracter.contains("8") || novoCaracter.contains("9")) {
            caracter = caracter.concat(novoCaracter);
            novoCaracter = c.leCaracter();
        }
        token.setSimbolo(simbolos.getNumero());
        token.setLexema(caracter);
        token.setLinhaCodigo(linhaDeCodigo);
        return novoCaracter;
    }

    public String trataIdentificador(String caracter, Funcoes c, Arquivo arq, Token token) {
        token.setLinhaCodigo(linhaDeCodigo);
        String novoCaracter = c.leCaracter();
        char[] auxCaracter = novoCaracter.toCharArray();
        String id = caracter;

        while (((int) auxCaracter[0] >= 65 && (int) auxCaracter[0] <= 90) || ((int) auxCaracter[0] >= 97 && (int) auxCaracter[0] <= 122) || ((int) auxCaracter[0] >= 48 && (int) auxCaracter[0] <= 57) || novoCaracter.contains("_")) {
            id = id.concat(novoCaracter);
            novoCaracter = c.leCaracter();
            auxCaracter = novoCaracter.toCharArray();

        }
        token.setLexema(id);

        switch (id) {

            case "programa":
                token.setSimbolo(simbolos.getPrograma());
                break;

            case "se":
                token.setSimbolo(simbolos.getSe());
                break;

            case "entao":
                token.setSimbolo(simbolos.getEntao());
                break;

            case "senao":
                token.setSimbolo(simbolos.getSenao());
                break;

            case "enquanto":
                token.setSimbolo(simbolos.getEnquanto());
                break;

            case "faca":
                token.setSimbolo(simbolos.getFaca());
                break;

            case "inicio":
                token.setSimbolo(simbolos.getInicio());
                break;

            case "fim":
                token.setSimbolo(simbolos.getFim());
                break;

            case "escreva":
                token.setSimbolo(simbolos.getEscreva());
                break;

            case "leia":
                token.setSimbolo(simbolos.getLeia());
                break;

            case "var":
                token.setSimbolo(simbolos.getVar());
                break;

            case "inteiro":
                token.setSimbolo(simbolos.getInteiro());
                break;

            case "booleano":
                token.setSimbolo(simbolos.getBooleano());
                break;

            case "verdadeiro":
                token.setSimbolo(simbolos.getVerdadeiro());
                break;

            case "falso":
                token.setSimbolo(simbolos.getFalso());
                break;

            case "procedimento":
                token.setSimbolo(simbolos.getProcedimento());
                break;

            case "funcao":
                token.setSimbolo(simbolos.getFuncao());
                break;

            case "div":
                token.setSimbolo(simbolos.getDivisao());
                break;

            case "e":
                token.setSimbolo(simbolos.getE());
                break;

            case "ou":
                token.setSimbolo(simbolos.getOu());
                break;

            case "nao":
                token.setSimbolo(simbolos.getNao());
                break;

            default:
                token.setSimbolo(simbolos.getIdentificador());
                break;

        }

        return novoCaracter;
    }

    public String trataAtribuicao(String caracter, Funcoes c, Arquivo arq, Token token) {

        String novoCaracter = c.leCaracter();

        if (novoCaracter.contains("=")) {
            caracter = caracter.concat(novoCaracter);
            token.setSimbolo(simbolos.getAtribuicao());
            novoCaracter = c.leCaracter();
        } else {
            token.setSimbolo(simbolos.getDoisPontos());
        }

        token.setLexema(caracter);
        token.setLinhaCodigo(linhaDeCodigo);

        return novoCaracter;
    }

    public String trataOperadorAritmetico(String caracter, Funcoes c, Arquivo arq, Token token) {

        token.setLexema(caracter);
        token.setLinhaCodigo(linhaDeCodigo);
        String novoCaracter = c.leCaracter();

        switch (caracter) {

            case "+":
                token.setSimbolo(simbolos.getMais());
                break;

            case "-":
                token.setSimbolo(simbolos.getMenos());
                break;

            case "*":
                token.setSimbolo(simbolos.getMultiplicacao());
                break;
        }

        return novoCaracter;
    }

    public String trataOperadorRelacional(String caracter, Funcoes c, Arquivo arq, Token token) {

        token.setLinhaCodigo(linhaDeCodigo);
        String novoCaracter = c.leCaracter();

        if (novoCaracter.contains("=")) {
            String relacional = caracter.concat(novoCaracter);

            switch (caracter) {

                case ">":
                    token.setSimbolo(simbolos.getMaiorIgual());
                    token.setLexema(relacional);
                    break;

                case "<":
                    token.setSimbolo(simbolos.getMenorIgual());
                    token.setLexema(relacional);
                    break;

                case "!":
                    token.setSimbolo(simbolos.getDiferente());
                    token.setLexema(relacional);
                    break;
            }

            novoCaracter = c.leCaracter();// le mais um caracter caso o o igual ter sido processado pelo switch acima
        } else {
            switch (caracter) {

                case ">":
                    token.setSimbolo(simbolos.getMaior());
                    token.setLexema(caracter);
                    break;

                case "<":
                    token.setSimbolo(simbolos.getMenor());
                    token.setLexema(caracter);
                    break;

                case "=":
                    token.setSimbolo(simbolos.getIgual());
                    token.setLexema(caracter);
                    break;

                default:
                    System.out.println("Erro no Trata Operador Relacional com 1 caracter");
                    System.err.println("Erro Lexico 4");
                    erroExclamacao = true;
                    break;

            }
        }

        return novoCaracter;
    }

    public String trataPontuacao(String caracter, Funcoes c, Arquivo arq, Token token) {
        token.setLexema(caracter);
        token.setLinhaCodigo(linhaDeCodigo);

        String novoCaracter = c.leCaracter();

        switch (caracter) {

            case ";":
                token.setSimbolo(simbolos.getPontoVirgula());
                break;

            case ".":
                token.setSimbolo(simbolos.getPonto());
                break;

            case ",":
                token.setSimbolo(simbolos.getVirgula());
                break;

            case "(":
                token.setSimbolo(simbolos.getAbreParenteses());
                break;

            case ")":
                token.setSimbolo(simbolos.getFechaParenteses());
                break;
        }

        return novoCaracter;
    }

}

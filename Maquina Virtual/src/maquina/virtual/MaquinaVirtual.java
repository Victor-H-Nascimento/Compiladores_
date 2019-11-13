package maquina.virtual;

import java.util.ArrayList;

public class MaquinaVirtual {

    private String linha;
    public Funcoes funcoes = new Funcoes();
    public ArrayList<ListaAuxiliar> filaJMP = new ArrayList();
    private final Arquivo arq = new Arquivo();


    public void leArquivo(String arquivoSelecionado) {
        arq.Read(arquivoSelecionado, funcoes);
        arq.EnderecaJMP(funcoes, filaJMP);
    }

    public String executarFuncoes() {
        String nomeFuncao = null;
        String primeiroParametro, segundoParametro;
        String aux;

        //do {
        String retorno = funcoes.fila.get(funcoes.getI()).toString();
        linha = String.valueOf(retorno);
        System.out.println("Lido " + linha);

        if (linha.contains(" ")) {

            if (linha.contains(",")) {
                //2 parametros

                nomeFuncao = linha.split(" ")[0];
                aux = linha.split(" ")[1];
                primeiroParametro = aux.split(",")[0];
                segundoParametro = aux.split(",")[1];
                System.out.println(nomeFuncao);
                System.out.println(primeiroParametro);
                System.out.println(segundoParametro);

                switch (nomeFuncao) {
                    case "ALLOC":
                        funcoes.ALLOC(Integer.parseInt(primeiroParametro), Integer.parseInt(segundoParametro));
                        break;
                    case "DALLOC":
                        funcoes.DALLOC(Integer.parseInt(primeiroParametro), Integer.parseInt(segundoParametro));
                        break;
                          case "RETURNF":
                        funcoes.RETURNF(Integer.parseInt(primeiroParametro), Integer.parseInt(segundoParametro));
                        break;
                    default:
                        System.err.println("Erro: Nenhuma funcao com 2 parametros foi chamada");
                }

            } else {
                //1 parametro

                nomeFuncao = linha.split(" ")[0];
                primeiroParametro = linha.split(" ")[1];
                System.out.println(nomeFuncao);
                System.out.println(primeiroParametro);

                if ("NULL".equals(primeiroParametro)) {//Se ler alguma linha de NULL, Ex. L1 NULL
                    funcoes.NULL();
                } else {

                    switch (nomeFuncao) {
                        case "CALL":
                            //Funcao call nao entra no if, pq nao 

                            for (ListaAuxiliar itemLista : filaJMP) {
                                if (itemLista.getLabel().equals(primeiroParametro) && itemLista.getInstrucao().contains("CALL")) {
                                    System.out.println("Call para posicao " + itemLista.getIndice());
                                    funcoes.CALL(itemLista.getIndice());
                                }
                            }
                            break;
                        case "JMP":

                            for (ListaAuxiliar itemLista : filaJMP) {
                                if (itemLista.getLabel().equals(primeiroParametro) && itemLista.getInstrucao().contains("JMP")) {
                                    funcoes.JMP(itemLista.getIndice());
                                }
                            }
                            break;
                        case "JMPF":
                            for (ListaAuxiliar itemLista : filaJMP) {
                                if (itemLista.getLabel().equals(primeiroParametro) && itemLista.getInstrucao().contains("JMPF")) {
                                    funcoes.JMPF(itemLista.getIndice());
                                }
                            }
                            break;
                        case "LDC":
                            funcoes.LDC(Integer.parseInt(primeiroParametro));
                            break;
                        case "LDV":
                            funcoes.LDV(Integer.parseInt(primeiroParametro));
                            break;
                        case "STR":
                            funcoes.STR(Integer.parseInt(primeiroParametro));
                            break;
                        default:
                            System.out.println("Erro: Nenhuma funcao com 1 parametro foi chamada " + nomeFuncao + " " + primeiroParametro);
                    }
                }

            }
        } else {
            //sem parametros
            //chamar a funcao
            System.out.println(linha);
            nomeFuncao = linha;

            switch (linha) {
                case "ADD":
                    funcoes.ADD();
                    break;
                case "AND":
                    funcoes.AND();
                    break;
                case "CDIF":
                    funcoes.CDIF();
                    break;
                case "CEQ":
                    funcoes.CEQ();
                    break;
                case "CMA":
                    funcoes.CMA();
                    break;
                case "CMAQ":
                    funcoes.CMAQ();
                    break;
                case "CME":
                    funcoes.CME();
                    break;
                case "CMEQ":
                    funcoes.CMEQ();
                    break;
                case "DIVI":
                    funcoes.DIVI();
                    break;
                case "HLT":
                    funcoes.HLT();
                    break;
                case "INV":
                    funcoes.INV();
                    break;
                case "MULT":
                    funcoes.MULT();
                    break;
                case "NEG":
                    funcoes.NEG();
                    break;
                case "NULL":
                    funcoes.NULL();
                    break;
                case "OR":
                    funcoes.OR();
                    break;
                case "PRN":
                    funcoes.PRN();
                    break;
                case "RD":
                    funcoes.RD();
                    break;
                case "RETURN":
                    funcoes.RETURN();
                    break;
                case "RETURNF":
                    funcoes.RETURNF();
                    break;
                case "START":
                    funcoes.START();
                    break;
                case "SUB":
                    funcoes.SUB();
                    break;
                case "PRINTAPILHA":
                    funcoes.PRINTAPILHA();
                    break;

                default:
                    System.err.println("Erro: Nenhuma funcao sem parametros foi chamada");
            }
        }

        funcoes.fila.add(linha);
        funcoes.setI();
        funcoes.PRINTAPILHA();
        System.out.println("" + "******************************************************************************************" + "");
        //} while (!linha.contains("HLT"));
        //sempre que executar uma linha, atualizar o i com a funcao setI 

        return nomeFuncao;
    }

}

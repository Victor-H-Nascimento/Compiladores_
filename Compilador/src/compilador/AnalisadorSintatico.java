/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compilador;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

/**
 *
 * @author victor
 */
public class AnalisadorSintatico {

    private final AnalisadorLexico analisadorLexico;
    private Token token = null;
    private final Stack<TabelaDeSimbolos> pilhaTabelaDeSimbolos = new Stack<>();
    private Token tokenAuxiliar = null;
    private final SimbolosToken simbolos = new SimbolosToken();
    private String fraseContendoErro = "";
    private boolean errosSintaticosSemantico = false;
    private String analiseAtribuicao = "erro";
    private final Stack<Token> pilhaOperadores = new Stack();
    private final ArrayList<ElementosPosFixa> filaPosFixa = new ArrayList();
    private int rotuloLabel = 1;
    private int posicaoMemoria = 0;
    private final GeradorCodigo gerador = new GeradorCodigo();
    private int qtdVariaveisAlloc = 0;
    private int qtdVariaveisDalloc = 0;
    private boolean chamadaFuncao = false;
    private boolean isFuncao = false;
    private final Stack<String> filaFuncProc = new Stack();
    private int qtdInicio = 0;

    AnalisadorSintatico(AnalisadorLexico analisadorLexico) throws IOException {
        this.analisadorLexico = analisadorLexico;
        analisaPrograma();
    }

    public Token getToken() {
        return token;
    }

    public void incrementaRotuloLabel() {
        this.rotuloLabel = this.rotuloLabel + 1;
    }

    public int getPosicaoMemoria() {
        return posicaoMemoria;
    }

    public void incrementaPosicaoMemoria() {
        this.posicaoMemoria = this.posicaoMemoria + 1;
    }

    public void decrementaPosicaoMemoria() {
        this.posicaoMemoria = this.posicaoMemoria - 1;
    }

    public String getFraseContendoErro() {
        return fraseContendoErro;
    }

    private void analisaPrograma() throws IOException {

        token = analisadorLexico.lexico();

        if (token.getSimbolo().contentEquals(simbolos.getPrograma())) {
            gerador.geraSTART();
            TabelaDeSimbolosProgramaProcedimentos programaTabelaSimbolos = new TabelaDeSimbolosProgramaProcedimentos(token.getLexema());
            pilhaTabelaDeSimbolos.push(programaTabelaSimbolos);
            token = analisadorLexico.lexico();

            if (token.getSimbolo().contentEquals(simbolos.getIdentificador()) && !analisadorLexico.contemErrosLexicos() && !errosSintaticosSemantico) {
                token = analisadorLexico.lexico();

                if (token.getSimbolo().contentEquals(simbolos.getPontoVirgula()) && !analisadorLexico.contemErrosLexicos() && !errosSintaticosSemantico) {
                     filaFuncProc.add("procedimento");
                    analisaBloco();
                    filaFuncProc.pop();
                    if (token.getSimbolo().contentEquals(simbolos.getPonto())) {
                        gerador.geraHLT();
                        //se acabou arquivo ou é comentário   então sucesso
                        //senao ERRO

                        for (int i = 0; i < filaPosFixa.size(); i++) {

                            System.out.print(filaPosFixa.get(i).getLexema() + " ");

                        }
                        System.out.println("");

                    } else {
                        mostraErros(".");
                    }
                } else {
                    mostraErros(";");
                }

            } else {
                mostraErros("identificador");
            }
        } else {
            mostraErros("programa");
        }

    }

    private void analisaBloco() throws IOException {

        token = analisadorLexico.lexico();
        int posicaoIncialAlloc = getPosicaoMemoria();
        String tipoSubRotina = "";

        analisaEtapaVariaveis();

        if (qtdVariaveisAlloc > 0) {
            gerador.geraALLOC(posicaoIncialAlloc, qtdVariaveisAlloc);
            qtdVariaveisAlloc = 0;
        }

        analisaSubRotinas();
        analisaComandos();

        //remover da pilha
        //fazer condicao para executar este while, somente se for um fim de um procedimento/funcao.
        int posicaoIncialDalloc = getPosicaoMemoria();
        while (!pilhaTabelaDeSimbolos.lastElement().isEscopo() && !pilhaTabelaDeSimbolos.isEmpty()) {//fazer logica para que se funcao nao tiver variaveis, nao dar pop nas variaveis de outras funcoes.

            if (pilhaTabelaDeSimbolos.lastElement() instanceof TabelaDeSimbolosFuncoes || pilhaTabelaDeSimbolos.lastElement() instanceof TabelaDeSimbolosProgramaProcedimentos) {//tira funcoes e procedimentos
                pilhaTabelaDeSimbolos.pop();
            } else {//tira variaveis
                pilhaTabelaDeSimbolos.pop();
                decrementaPosicaoMemoria();
                qtdVariaveisDalloc++;
            }

        }

        if ((pilhaTabelaDeSimbolos.lastElement() instanceof TabelaDeSimbolosFuncoes || pilhaTabelaDeSimbolos.lastElement() instanceof TabelaDeSimbolosProgramaProcedimentos) && pilhaTabelaDeSimbolos.lastElement().isEscopo() && !pilhaTabelaDeSimbolos.lastElement().getLexema().contentEquals("programa")) {// muda escopo das funcoes pra falso, exceto se for o programa
            pilhaTabelaDeSimbolos.lastElement().setEscopo(false);
        }

        
        if ( !filaFuncProc.isEmpty()) {
            tipoSubRotina = filaFuncProc.peek();
        }
        
        if (tipoSubRotina.contentEquals("funcao")) {
           // isFuncao = false;

            if (qtdVariaveisDalloc > 0) {
                gerador.geraRETURNF(posicaoIncialDalloc - qtdVariaveisDalloc, qtdVariaveisDalloc);
                qtdVariaveisDalloc = 0;
            } else {
                gerador.geraRETURNF();
            }
            
        } else if(tipoSubRotina.contentEquals("procedimento")) {
            if (qtdVariaveisDalloc > 0) {
                gerador.geraDALLOC(posicaoIncialDalloc - qtdVariaveisDalloc, qtdVariaveisDalloc);
                qtdVariaveisDalloc = 0;
            }
        }

    }

    private void analisaEtapaVariaveis() throws IOException {

        
        if (token.getSimbolo().contentEquals(simbolos.getVar()) && !errosSintaticosSemantico) {

            token = analisadorLexico.lexico();

            if (token.getSimbolo().contentEquals(simbolos.getIdentificador())) {
                while (token.getSimbolo().contentEquals(simbolos.getIdentificador())) {

                    analisaDeclaracaoVariaveis();

                    if (token.getSimbolo().contentEquals(simbolos.getPontoVirgula()) && !analisadorLexico.contemErrosLexicos() && !errosSintaticosSemantico) {
                        token = analisadorLexico.lexico();
                    } else {
                        mostraErros(";");
                    }
                }
            } else {
                mostraErros("identificador");
            }
        }
    }

    private void analisaDeclaracaoVariaveis() throws IOException {

        do {
            if (token.getSimbolo().contentEquals(simbolos.getIdentificador())) {
                if (pesquisaVariavelDuplicada(token.getLexema())) {
                    TabelaDeSimbolosVariaveis variaveisTabelaSimbolos = new TabelaDeSimbolosVariaveis(token.getLexema(), posicaoMemoria);
                    incrementaPosicaoMemoria();
                    qtdVariaveisAlloc++;
                    pilhaTabelaDeSimbolos.push(variaveisTabelaSimbolos);
                    token = analisadorLexico.lexico();
                    if (token.getSimbolo().contentEquals(simbolos.getVirgula()) || token.getSimbolo().contentEquals(simbolos.getDoisPontos())) {

                        if (token.getSimbolo().contentEquals(simbolos.getVirgula())) {
                            token = analisadorLexico.lexico();

                            if (token.getSimbolo().contentEquals(simbolos.getDoisPontos())) {
                                mostraErros("encontrado ':' quando um identificador era");
                            }

                        }

                    } else {
                        mostraErros(", ou :");
                    }

                } else {
                    erroSemanticoVariavelDuplicada();
                }
            } else {
                mostraErros("apos a ',' um identificador e");
            }

        } while (!token.getSimbolo().contentEquals(simbolos.getDoisPontos()) && !errosSintaticosSemantico);

        token = analisadorLexico.lexico();
        analisaTipo();

    }

    private void analisaTipo() throws IOException {
        if (!token.getSimbolo().contentEquals(simbolos.getInteiro()) && !token.getSimbolo().contentEquals(simbolos.getBooleano()) && !errosSintaticosSemantico) {
            mostraErros("inteiro ou booleano");
        } else {
            // senão coloca_tipo_tabela(token.lexema) semantico

            for (TabelaDeSimbolos item : pilhaTabelaDeSimbolos) {

                if (item instanceof TabelaDeSimbolosVariaveis) {
                    if (((TabelaDeSimbolosVariaveis) item).getTipo().contentEquals("")) {
                        ((TabelaDeSimbolosVariaveis) item).setTipo(token.getLexema());
                    }
                }

            }

        }

        token = analisadorLexico.lexico();

    }

    private void analisaComandos() throws IOException {
        if (token.getSimbolo().contentEquals(simbolos.getInicio()) && !errosSintaticosSemantico) {
          /*  if (flag == 1 && qtdInicio == 0) {
                flag = 0;
                gerador.geraNULL(1);
            }*/
            token = analisadorLexico.lexico();
            analisaComandoSimples();

            while (!token.getSimbolo().contentEquals(simbolos.getFim()) && !errosSintaticosSemantico) {
                if (token.getSimbolo().contentEquals(simbolos.getPontoVirgula())) {
                    token = analisadorLexico.lexico();

                    if (!token.getSimbolo().contentEquals(simbolos.getFim())) {
                        analisaComandoSimples();
                    }

                } else {
                    mostraErros(";");
                }
            }

            token = analisadorLexico.lexico();
        } else {
            mostraErros("inicio");
        }
    }

    private void analisaComandoSimples() throws IOException {
        if (token.getSimbolo().contentEquals(simbolos.getIdentificador())) {
            analisaAtribuicaoOuChamadaProcedimento();
        } else if (token.getSimbolo().contentEquals(simbolos.getSe())) {
            analisaSe();
        } else if (token.getSimbolo().contentEquals(simbolos.getEnquanto())) {
            analisaEnquanto();
        } else if (token.getSimbolo().contentEquals(simbolos.getLeia())) {
            analisaLeia();
        } else if (token.getSimbolo().contentEquals(simbolos.getEscreva())) {
            analisaEscreva();
        } else {
            analisaComandos();
        }
    }

    private void analisaAtribuicaoOuChamadaProcedimento() throws IOException {
        tokenAuxiliar = token;
        token = analisadorLexico.lexico();
        if (token.getSimbolo().contentEquals(simbolos.getAtribuicao())) {
            analisaAtribuicao();
        } else {
            analisaChamadaProcedimento();  //temos que implementar, nao tem no livro
        }
    }

    private void analisaLeia() throws IOException {
        token = analisadorLexico.lexico();
        if (token.getSimbolo().contentEquals(simbolos.getAbreParenteses()) && !errosSintaticosSemantico) {
            token = analisadorLexico.lexico();
            if (token.getSimbolo().contentEquals(simbolos.getIdentificador()) && !errosSintaticosSemantico) {
                if (pesquisaDeclaracaoVariavel(token.getLexema())) {

                    gerador.geraRD();
                    gerador.geraSTR(retornaPosicaoMemoria(token.getLexema()));
                    token = analisadorLexico.lexico();
                    if (token.getSimbolo().contentEquals(simbolos.getFechaParenteses()) && !errosSintaticosSemantico) {
                        token = analisadorLexico.lexico();
                    } else {
                        mostraErros(")");
                    }
                } else {
                    erroSemanticoVariavelIncompativel(token.getLexema());
                }

            } else {
                mostraErros("identificador");
            }
        } else {
            mostraErros("(");
        }
    }

    private void analisaEscreva() throws IOException {
        token = analisadorLexico.lexico();
        if (token.getSimbolo().contentEquals(simbolos.getAbreParenteses()) && !errosSintaticosSemantico) {
            token = analisadorLexico.lexico();
            if (token.getSimbolo().contentEquals(simbolos.getIdentificador()) && !errosSintaticosSemantico) {
                if (pesquisaDeclaracaoFuncaoVariavel(token.getLexema())) {

                    if (pesquisaDeclaracaoVariavel(token.getLexema())) {// se variavel
                        gerador.geraLDV(retornaPosicaoMemoria(token.getLexema()));
                        gerador.geraPRN();
                    } else {//se funcao
                        gerador.geraCALL(pesquisaLabelProcedimentoFuncao(token.getLexema()));
                        gerador.geraPRN();
                    }

                    token = analisadorLexico.lexico();
                    if (token.getSimbolo().contentEquals(simbolos.getFechaParenteses()) && !errosSintaticosSemantico) {
                        token = analisadorLexico.lexico();
                    } else {
                        mostraErros(")");
                    }
                } else {
                    erroSemanticoVariavelFuncaoIncompativel(token.getLexema());
                }

            } else {
                mostraErros("identificador");
            }
        } else {
            mostraErros("(");
        }
    }

    private void analisaEnquanto() throws IOException {
        //semantico
        gerador.geraNULL(rotuloLabel);
        int labelEnquanto = rotuloLabel;
        incrementaRotuloLabel();
        int labelSeEnquanto = rotuloLabel;

        token = analisadorLexico.lexico();

        analisaExpressao();// ver condicao do retorno
        fimInFixa();

        String retorno = verificaPosFixa();
        gerador.geraJMPF(rotuloLabel);
        incrementaRotuloLabel();
        if (retorno.contentEquals(simbolos.getBooleano()) && !errosSintaticosSemantico) {
            if (token.getSimbolo().contentEquals(simbolos.getFaca()) && !errosSintaticosSemantico) {
                //semantico
                token = analisadorLexico.lexico();
                analisaComandoSimples();
                gerador.geraJMP(labelEnquanto);
                gerador.geraNULL(labelSeEnquanto);
                //semantico
            } else {
                mostraErros("faca");
            }
        } else {
            erroTipoExpressao();
        }

    }

    private void analisaSe() throws IOException {
        int labelSe = rotuloLabel;
        int labelSenao = rotuloLabel;
        incrementaRotuloLabel();

        token = analisadorLexico.lexico();
        analisaExpressao();// ver condicao do retorno
        fimInFixa();
        String retorno = verificaPosFixa();

        if (retorno.contentEquals(simbolos.getBooleano()) && !errosSintaticosSemantico) {
            if (token.getSimbolo().contentEquals(simbolos.getEntao()) && !errosSintaticosSemantico) {
                gerador.geraJMPF(labelSe);// gera jmpf
                token = analisadorLexico.lexico();
                analisaComandoSimples();
                if (token.getSimbolo().contentEquals(simbolos.getSenao())) {
                    labelSenao = rotuloLabel;
                    incrementaRotuloLabel();
                    gerador.geraJMP(labelSenao);
                    gerador.geraNULL(labelSe);

                    token = analisadorLexico.lexico();
                    analisaComandoSimples();
                }
                gerador.geraNULL(labelSenao);
            } else {
                mostraErros("entao");
            }

        } else {
            erroTipoExpressao();
        }

    }

    private void analisaSubRotinas() throws IOException {
        //semantico
        int flag = 0;
        int labelFlag = 0;
        if (token.getSimbolo().contentEquals(simbolos.getProcedimento()) || token.getSimbolo().contentEquals(simbolos.getFuncao())) {
            gerador.geraJMP(rotuloLabel);
           labelFlag = rotuloLabel;
            incrementaRotuloLabel();
            flag = 1;
        }
        while (token.getSimbolo().contentEquals(simbolos.getProcedimento()) || token.getSimbolo().contentEquals(simbolos.getFuncao())) {
            if (token.getSimbolo().contentEquals(simbolos.getProcedimento())) {
                analisaDeclaracaoProcedimento();
            } else {
                analisaDeclaracaoFuncao();
            }
            if (token.getSimbolo().contentEquals(simbolos.getPontoVirgula()) && !errosSintaticosSemantico) {
                token = analisadorLexico.lexico();
            } else {
                mostraErros(";");
            }
        }
        
        if (flag == 1) {
            gerador.geraNULL(labelFlag);
        }

    }

    private void analisaDeclaracaoProcedimento() throws IOException {
        token = analisadorLexico.lexico();
        //semantico
        if (token.getSimbolo().contentEquals(simbolos.getIdentificador()) && !errosSintaticosSemantico) {
            //semantico

            if (!pesquisaDeclaracaoProcedimento(token.getLexema())) {
                TabelaDeSimbolosProgramaProcedimentos procedimentoTabelaSimbolos = new TabelaDeSimbolosProgramaProcedimentos(token.getLexema(), rotuloLabel);
                pilhaTabelaDeSimbolos.push(procedimentoTabelaSimbolos);

                token = analisadorLexico.lexico();
                if (token.getSimbolo().contentEquals(simbolos.getPontoVirgula()) && !errosSintaticosSemantico) {
                    gerador.geraNULL(rotuloLabel);
                    incrementaRotuloLabel();
                    qtdInicio++;
                     filaFuncProc.add("procedimento");
                    analisaBloco();
                    filaFuncProc.pop();
                    qtdInicio--;
                    gerador.geraRETURN();
                } else {
                    mostraErros(";");
                }
            } else {
                erroSemanticoProcedimentoIncompativel(token.getLexema());
            }

        } else {
            mostraErros("identificador");
        }
        //semantico
    }

    private void analisaDeclaracaoFuncao() throws IOException {
        token = analisadorLexico.lexico();
        //semantico
        if (token.getSimbolo().contentEquals(simbolos.getIdentificador()) && !errosSintaticosSemantico) {
            //semantico

            if (!pesquisaDeclaracaoFuncao(token.getLexema())) {
                TabelaDeSimbolosFuncoes funcaoTabelaSimbolos = new TabelaDeSimbolosFuncoes(token.getLexema(), rotuloLabel);
                pilhaTabelaDeSimbolos.push(funcaoTabelaSimbolos);

                token = analisadorLexico.lexico();
                if (token.getSimbolo().contentEquals(simbolos.getDoisPontos()) && !errosSintaticosSemantico) {
                    token = analisadorLexico.lexico();
                    if (token.getSimbolo().contentEquals(simbolos.getInteiro()) || token.getSimbolo().contentEquals(simbolos.getBooleano()) && !errosSintaticosSemantico) {
                        //semantico

                        preencheTipoFuncao(funcaoTabelaSimbolos, token);

                        token = analisadorLexico.lexico();
                        if (token.getSimbolo().contentEquals(simbolos.getPontoVirgula())) {
                            gerador.geraNULL(rotuloLabel);
                            incrementaRotuloLabel();
                            isFuncao = true;
                            filaFuncProc.add("funcao");
                            qtdInicio++;
                            analisaBloco();
                            qtdInicio--;
                            filaFuncProc.pop();
                            isFuncao = false;
                        }
                    } else {
                        mostraErros("inteiro ou booleano");
                    }
                } else {
                    mostraErros(":");
                }
            } else {
                erroSemanticoFuncaoIncompativel(token.getLexema());
            }
        } else {
            mostraErros("identificador");
        }
        //semantico
    }

    private void analisaExpressao() throws IOException {
        analisaExpressaoSimples();
        if (token.getSimbolo().contentEquals(simbolos.getMaior()) || token.getSimbolo().contentEquals(simbolos.getMaiorIgual()) || token.getSimbolo().contentEquals(simbolos.getIgual()) || token.getSimbolo().contentEquals(simbolos.getMenor()) || token.getSimbolo().contentEquals(simbolos.getMenorIgual()) || token.getSimbolo().contentEquals(simbolos.getDiferente())) {
            verificaEAdicionaPilhaOperadores(token);
            token = analisadorLexico.lexico();
            analisaExpressaoSimples();
        }

    }

    private void analisaExpressaoSimples() throws IOException {
        if (token.getSimbolo().contentEquals(simbolos.getMais()) || token.getSimbolo().contentEquals(simbolos.getMenos())) {

            if (token.getSimbolo().contentEquals(simbolos.getMais())) {
                token.setLexema("+u");
                token.setSimbolo(simbolos.getMaisUnitario());
            } else {
                token.setLexema("-u");
                token.setSimbolo(simbolos.getMenosUnitario());
            }

            verificaEAdicionaPilhaOperadores(token);//add +u/-u
            token = analisadorLexico.lexico();

        }

        analisaTermo();

        while (token.getSimbolo().contentEquals(simbolos.getMais()) || token.getSimbolo().contentEquals(simbolos.getMenos()) || token.getSimbolo().contentEquals(simbolos.getOu())) {
            verificaEAdicionaPilhaOperadores(token);//add +/-/ou
            token = analisadorLexico.lexico();
            analisaTermo();
        }

    }

    private void analisaTermo() throws IOException {
        analisaFator();
        while (token.getSimbolo().contentEquals(simbolos.getMultiplicacao()) || token.getSimbolo().contentEquals(simbolos.getDivisao()) || token.getSimbolo().contentEquals(simbolos.getE())) {
            verificaEAdicionaPilhaOperadores(token);//add */div/e
            token = analisadorLexico.lexico();
            analisaFator();
        }
    }

    private void analisaFator() throws IOException {
        if (token.getSimbolo().contentEquals(simbolos.getIdentificador())) {// se variavel ou funcao
            //semantico
            if (pesquisaDeclaracaoFuncaoVariavel(token.getLexema())) {//lexema
                if (pesquisaTipoFuncao(token)) {
                    analisaChamadaFuncao();
                    if (!filaFuncProc.peek().contentEquals("funcao")) {//!isFuncao
                        chamadaFuncao = true;
                    } else {
                        Operando elemento = new Operando();// entra aqui se for uma funcao dentro da posfixa
                        elemento.setLexema("null");
                        elemento.setTipo(preencheTipoFuncao(tokenAuxiliar));
                        filaPosFixa.add(elemento);
                    }

                } else {

                    Operando elemento = new Operando();
                    elemento.setLexema(token.getLexema());
                    pesquisaTipoVariavel(token.getLexema(), elemento);// coloca o tipo da variavel
                    filaPosFixa.add(elemento);//add identificador na saida pos fixa
                    token = analisadorLexico.lexico();
                }
            } else {
                erroSemanticoVariavelFuncaoIncompativel(token.getLexema());
            }

        } else {
            if (token.getSimbolo().contentEquals(simbolos.getNumero())) {
                Operando elemento = new Operando();
                elemento.setLexema(token.getLexema());
                elemento.setTipo(simbolos.getInteiro());

                filaPosFixa.add(elemento);//add numero na saida pos fixa
                token = analisadorLexico.lexico();

            } else if (token.getSimbolo().contentEquals(simbolos.getNao())) {
                verificaEAdicionaPilhaOperadores(token);//nao
                token = analisadorLexico.lexico();
                analisaFator();
            } else if (token.getSimbolo().contentEquals(simbolos.getAbreParenteses())) {
                verificaEAdicionaPilhaOperadores(token);//add abre parenteses
                token = analisadorLexico.lexico();
                analisaExpressao();// ver condicao do retorno
                if (token.getSimbolo().contentEquals(simbolos.getFechaParenteses()) && !errosSintaticosSemantico) {
                    verificaEAdicionaPilhaOperadores(token);//add fecha parenteses
                    token = analisadorLexico.lexico();
                } else {
                    mostraErros(")");
                }

            } else if ((token.getLexema().contentEquals("verdadeiro") || token.getLexema().contentEquals("falso")) && !errosSintaticosSemantico) {
                Operando elemento = new Operando();
                elemento.setLexema(token.getLexema());
                elemento.setTipo(simbolos.getBooleano());
                filaPosFixa.add(elemento);//add numero na saida pos fixa
                token = analisadorLexico.lexico();
            } else {
                mostraErros("verdadeiro ou falso");
            }
        }
    }

    private void analisaAtribuicao() throws IOException {

        //verificar se tokenAuxiliar é uma variavel e se já está na tabela de simbolos
        for (int i = pilhaTabelaDeSimbolos.size(); i > 0; i--) {
            if (pilhaTabelaDeSimbolos.elementAt(i - 1).getLexema().contentEquals(tokenAuxiliar.getLexema())) {

                if (pilhaTabelaDeSimbolos.elementAt(i - 1) instanceof TabelaDeSimbolosVariaveis) {
                    analiseAtribuicao = "variavel";
                } else if (pilhaTabelaDeSimbolos.elementAt(i - 1) instanceof TabelaDeSimbolosFuncoes) {
                    analiseAtribuicao = "funcao";
                }
                break;
            }
        }

        if (analiseAtribuicao.contentEquals("variavel")) {
            token = analisadorLexico.lexico();
            analisaExpressao();// ver condicao do retorno
            fimInFixa();
            if (!chamadaFuncao) {// se nao for uma chamada de funcao
                String retorno = verificaPosFixa();
                if (!retorno.contentEquals(pesquisaTipoVariavel(tokenAuxiliar.getLexema())) && !errosSintaticosSemantico) {// erro se o tipo da variavel/funcao do lado esquerdo for diferente do tipo da expressao
                    erroTipoExpressao();
                } else {
                    gerador.geraSTR(retornaPosicaoMemoria(tokenAuxiliar.getLexema()));
                }
            } else {
                chamadaFuncao = false;
            }
            analiseAtribuicao = "erro";

        } else if (analiseAtribuicao.contentEquals("funcao")) {
            if (filaFuncProc.peek().contentEquals("funcao")) { //isfuncao
                token = analisadorLexico.lexico();
                analisaExpressao();// ver condicao do retorno
                fimInFixa();
                if (!chamadaFuncao) {
                    String retorno = verificaPosFixa();
                    if (!retorno.contentEquals(pesquisaTipoVariavel(tokenAuxiliar.getLexema())) && !errosSintaticosSemantico ) {// erro se o tipo da variavel/funcao do lado esquerdo for diferente do tipo da expressao
                        erroTipoExpressao();
                    }
                } else {
                    chamadaFuncao = false;
                }
                analiseAtribuicao = "erro";
            } else {
                erroSemanticoRetornoDeFuncaoLugarIndevido();
            }

        } else {
            erroSemanticoLadoEsquerdoAtribuicao();
        }

    }

    private void analisaChamadaProcedimento() {

        if (pesquisaDeclaracaoProcedimento(tokenAuxiliar.getLexema())) {// entra aqui se nao houve nenhum errado na declaracao do procedimento

            gerador.geraCALL(pesquisaLabelProcedimentoFuncao(tokenAuxiliar.getLexema()));
        } else {
            erroSemanticoLadoEsquerdoChamadaProcedimento();
        }

    }

    private void analisaChamadaFuncao() throws IOException {

        if (pesquisaDeclaracaoFuncao(token.getLexema())) {// entra aqui se nao houve nenhum errado na declaracao do procedimento

            if (!filaFuncProc.peek().contentEquals("funcao")) {//!isfuncao
                gerador.geraCALL(pesquisaLabelProcedimentoFuncao(token.getLexema()));
                gerador.geraSTR(retornaPosicaoMemoria(tokenAuxiliar.getLexema()));// antes, verificar se tipo de retorno eh igual ao tipo da variavel    
            }

            token = analisadorLexico.lexico();
        } else {
            erroSemanticoLadoDireitoChamadaFuncao();
        }

    }

    private void mostraErros(String erroEncontrado) {

        if (analisadorLexico.contemErrosLexicos() && !errosSintaticosSemantico) {
            System.out.println("Linha " + token.getLinhaCodigo() + " - Erro Léxico: Caracter " + token.getLexema() + " não tem função definida.");
            fraseContendoErro = fraseContendoErro.concat("Linha " + Integer.toString(token.getLinhaCodigo()) + " - Erro Léxico: " + token.getLexema() + " não tem função definida.");
        } else if (!analisadorLexico.contemErrosLexicos() && !errosSintaticosSemantico) {
            System.out.println("Linha " + token.getLinhaCodigo() + " - Erro Sintatico: " + erroEncontrado + " esperado");
            fraseContendoErro = ("Linha " + token.getLinhaCodigo() + " - Erro Sintatico: " + erroEncontrado + " esperado");
        }
        errosSintaticosSemantico = true;
    }

    private boolean pesquisaVariavelDuplicada(String lexema) {

        int tamanhoPilha = pilhaTabelaDeSimbolos.size();

        while (pilhaTabelaDeSimbolos.elementAt(tamanhoPilha - 1) instanceof TabelaDeSimbolosVariaveis && !pilhaTabelaDeSimbolos.isEmpty() && tamanhoPilha > 1) {

            TabelaDeSimbolosVariaveis item = (TabelaDeSimbolosVariaveis) pilhaTabelaDeSimbolos.elementAt(tamanhoPilha - 1);

            if (item.getLexema().contentEquals(lexema)) {
                return false;
            }

            tamanhoPilha--;
        }

        return true;
    }

    private boolean pesquisaDeclaracaoVariavel(String lexema) {

        for (TabelaDeSimbolos item : pilhaTabelaDeSimbolos) {

            if (item instanceof TabelaDeSimbolosVariaveis) {
                if (item.getLexema().contentEquals(lexema)) {
                    return true;
                }
            }

        }
        return false;
    }

    private void pesquisaTipoVariavel(String lexema, Operando elemento) {

        for (int i = pilhaTabelaDeSimbolos.size(); i > 0; i--) {

            //TabelaDeSimbolosVariaveis item = (TabelaDeSimbolosVariaveis) pilhaTabelaDeSimbolos.get(i - 1);
            if (pilhaTabelaDeSimbolos.get(i - 1) instanceof TabelaDeSimbolosVariaveis && pilhaTabelaDeSimbolos.get(i - 1).getLexema().contentEquals(lexema)) {
                TabelaDeSimbolosVariaveis item = (TabelaDeSimbolosVariaveis) pilhaTabelaDeSimbolos.get(i - 1);
                String tipo = item.getTipo();
                elemento.setMemoria(item.getMemoria());//coloca valor da memoria
                if (tipo.contentEquals("inteiro")) {
                    elemento.setTipo(simbolos.getInteiro());
                } else {
                    elemento.setTipo(simbolos.getBooleano());
                }

                break;
            } else {
                //Erro semantico: nao existe tipo na variavel ou variavel nao foi encontrada
            }

        }

    }

    private String pesquisaTipoVariavel(String lexema) {

        for (int i = pilhaTabelaDeSimbolos.size(); i > 0; i--) {

            //TabelaDeSimbolosVariaveis item = (TabelaDeSimbolosVariaveis) pilhaTabelaDeSimbolos.get(i - 1);
            if (pilhaTabelaDeSimbolos.get(i - 1) instanceof TabelaDeSimbolosVariaveis && pilhaTabelaDeSimbolos.get(i - 1).getLexema().contentEquals(lexema)) {
                TabelaDeSimbolosVariaveis item = (TabelaDeSimbolosVariaveis) pilhaTabelaDeSimbolos.get(i - 1);
                String tipo = item.getTipo();

                if (tipo.contentEquals("inteiro")) {
                    return simbolos.getInteiro();
                } else {
                    return simbolos.getBooleano();
                }
            } else {
                if (pilhaTabelaDeSimbolos.get(i - 1) instanceof TabelaDeSimbolosFuncoes && pilhaTabelaDeSimbolos.get(i - 1).getLexema().contentEquals(lexema)) {
                    TabelaDeSimbolosFuncoes item = (TabelaDeSimbolosFuncoes) pilhaTabelaDeSimbolos.get(i - 1);
                    return item.getTipo();
                }
            }

        }
        return "erro";
    }

    private boolean pesquisaDeclaracaoFuncaoVariavel(String lexema) {
        for (TabelaDeSimbolos item : pilhaTabelaDeSimbolos) {

            if (item instanceof TabelaDeSimbolosVariaveis || item instanceof TabelaDeSimbolosFuncoes) {
                if (item.getLexema().contentEquals(lexema)) {
                    return true;
                }
            }

        }
        return false;
    }

    private boolean pesquisaDeclaracaoProcedimento(String lexema) {
        for (TabelaDeSimbolos item : pilhaTabelaDeSimbolos) {

            if (item instanceof TabelaDeSimbolosProgramaProcedimentos) {
                if (item.getLexema().contentEquals(lexema)) {
                    return true;
                }
            }

        }
        return false;
    }

    private boolean pesquisaDeclaracaoFuncao(String lexema) {
        for (TabelaDeSimbolos item : pilhaTabelaDeSimbolos) {

            if (item instanceof TabelaDeSimbolosFuncoes) {
                if (item.getLexema().contentEquals(lexema)) {
                    return true;
                }
            }

        }
        return false;
    }

    private void preencheTipoFuncao(TabelaDeSimbolosFuncoes elemento, Token tokenAux) {

        for (TabelaDeSimbolos item : pilhaTabelaDeSimbolos) {

            if (item instanceof TabelaDeSimbolosFuncoes) {
                if (item.getLexema().contentEquals(elemento.getLexema())) {

                    if (tokenAux.getSimbolo().contentEquals(simbolos.getInteiro())) {
                        ((TabelaDeSimbolosFuncoes) elemento).setTipo(simbolos.getInteiro());

                    } else {
                        ((TabelaDeSimbolosFuncoes) elemento).setTipo(simbolos.getBooleano());
                    }
                }
            }

        }
    }

    private String preencheTipoFuncao(Token tokenAux) {

        for (TabelaDeSimbolos item : pilhaTabelaDeSimbolos) {

            if (item instanceof TabelaDeSimbolosFuncoes) {
                if (item.getLexema().contentEquals(tokenAux.getLexema())) {

                    return ((TabelaDeSimbolosFuncoes) item).getTipo();
                }
            }

        }
        return "erro";
    }

    private boolean pesquisaTipoFuncao(Token tokenAux) {
        for (TabelaDeSimbolos item : pilhaTabelaDeSimbolos) {

            if (item instanceof TabelaDeSimbolosFuncoes) {
                if (item.getLexema().contentEquals(tokenAux.getLexema())) {

                    if (((TabelaDeSimbolosFuncoes) item).getTipo().contentEquals("sInteiro") || ((TabelaDeSimbolosFuncoes) item).getTipo().contentEquals("sBooleano")) {
                        return true;
                    }
                }
            }

        }

        return false;
    }

    private void erroSemanticoVariavelDuplicada() {
        fraseContendoErro = fraseContendoErro.concat("Linha " + Integer.toString(token.getLinhaCodigo()) + " - Erro Semantico: " + "Variavel duplicada '" + token.getLexema() + "'");
        errosSintaticosSemantico = true;
    }

    private void erroSemanticoLadoEsquerdoAtribuicao() {
        fraseContendoErro = fraseContendoErro.concat("Linha " + Integer.toString(token.getLinhaCodigo()) + " - Erro Semantico: " + "Tipo incompatível no lado esquerdo da atribuição");
        errosSintaticosSemantico = true;
    }

    private void erroSemanticoLadoEsquerdoChamadaProcedimento() {
        fraseContendoErro = fraseContendoErro.concat("Linha " + Integer.toString(token.getLinhaCodigo()) + " - Erro Semantico: " + "Tipo incompatível. Espera-se procedimento.");
        errosSintaticosSemantico = true;
    }

    private void erroSemanticoLadoDireitoChamadaFuncao() {
        fraseContendoErro = fraseContendoErro.concat("Linha " + Integer.toString(token.getLinhaCodigo()) + " - Erro Semantico: " + "Espera-se funcao do lado direito da atribuicao.");
        errosSintaticosSemantico = true;
    }

    private void erroSemanticoVariavelIncompativel(String lexema) {
        fraseContendoErro = fraseContendoErro.concat("Linha " + Integer.toString(token.getLinhaCodigo()) + " - Erro Semantico: " + lexema + " é incompatível, espera-se uma variavel.");
        errosSintaticosSemantico = true;
    }

    private void erroSemanticoVariavelFuncaoIncompativel(String lexema) {
        fraseContendoErro = fraseContendoErro.concat("Linha " + Integer.toString(token.getLinhaCodigo()) + " - Erro Semantico: " + lexema + " é incompatível, espera-se uma variavel ou uma funcao.");
        errosSintaticosSemantico = true;
    }

    private void erroSemanticoProcedimentoIncompativel(String lexema) {
        fraseContendoErro = fraseContendoErro.concat("Linha " + Integer.toString(token.getLinhaCodigo()) + " - Erro Semantico: " + lexema + " é incompatível, espera-se um procedimento.");
        errosSintaticosSemantico = true;
    }

    private void erroSemanticoFuncaoIncompativel(String lexema) {
        fraseContendoErro = fraseContendoErro.concat("Linha " + Integer.toString(token.getLinhaCodigo()) + " - Erro Semantico: " + lexema + " é incompatível, espera-se uma funcao.");
        errosSintaticosSemantico = true;
    }

    private int getPrioridade(String valor) {
        if (valor.contentEquals("+u") || valor.contentEquals("-u") || valor.contentEquals("nao")) {
            return 5;
        } else if (valor.contentEquals("*") || valor.contentEquals("div")) {
            return 4;
        } else if (valor.contentEquals("+") || valor.contentEquals("-")) {
            return 3;
        } else if (valor.contentEquals(">") || valor.contentEquals("<") || valor.contentEquals(">=") || valor.contentEquals("<=") || valor.contentEquals("=") || valor.contentEquals("!=")) {
            return 2;
        } else if (valor.contentEquals("e")) {
            return 1;
        } else if (valor.contentEquals("ou")) {
            return 0;
        }
        return -1;// se der erro
    }

    @SuppressWarnings("empty-statement")
    private void verificaEAdicionaPilhaOperadores(Token tokenAux) {

        if (pilhaOperadores.isEmpty() || tokenAux.getLexema().contentEquals("(")) {
            pilhaOperadores.add(tokenAux);
        } else if (tokenAux.getLexema().contentEquals(")")) {
            while (!pilhaOperadores.lastElement().getLexema().contentEquals("(")) {
                Token aux = pilhaOperadores.pop();
                Operador elemento = new Operador();
                elemento.setLexema(aux.getLexema());
                filaPosFixa.add(elemento);

            }
            pilhaOperadores.pop();
        } else {

            int prioridadeTokenAux = getPrioridade(tokenAux.getLexema());

            while (!pilhaOperadores.isEmpty() && !pilhaOperadores.lastElement().getLexema().contentEquals("(")) {

                if (prioridadeTokenAux <= getPrioridade(pilhaOperadores.lastElement().getLexema())) {
                    Token aux = pilhaOperadores.pop();
                    Operador elemento = new Operador();
                    elemento.setLexema(aux.getLexema());
                    filaPosFixa.add(elemento);

                } else {
                    break;
                }

            }

            pilhaOperadores.add(tokenAux);

        }

    }

    @SuppressWarnings("empty-statement")
    private void fimInFixa() {

        for (int i = pilhaOperadores.size(); i > 0; i--) {
            Token aux = pilhaOperadores.pop();
            Operador elemento = new Operador();
            elemento.setLexema(aux.getLexema());
            filaPosFixa.add(elemento);
        }

    }

    private String verificaPosFixa() {

        geraCodigoPosFixa(filaPosFixa);

        int retorno = 0;
        while (filaPosFixa.size() > 1 && errosSintaticosSemantico == false) {
            int indice = 0;
            while (filaPosFixa.get(indice) instanceof Operando) {
                indice++;
            }

            FuncoesPosFixa posFixa = new FuncoesPosFixa();

            int prioridade = getPrioridade(filaPosFixa.get(indice).getLexema());

            switch (prioridade) {

                case 0:
                    retorno = posFixa.trataEOu(filaPosFixa, indice);
                    break;

                case 1:
                    retorno = posFixa.trataEOu(filaPosFixa, indice);
                    break;
                case 2:
                    if (filaPosFixa.get(indice).getLexema().contentEquals("!=") || filaPosFixa.get(indice).getLexema().contentEquals("=")) {
                        retorno = posFixa.trataIgualDiferente(filaPosFixa, indice);
                    } else {

                        retorno = posFixa.trataRelacionais(filaPosFixa, indice);
                    }
                    break;
                case 3:
                    retorno = posFixa.trataMultDivSomaSub(filaPosFixa, indice);
                    break;
                case 4:
                    retorno = posFixa.trataMultDivSomaSub(filaPosFixa, indice);
                    break;
                case 5:
                    if (filaPosFixa.get(indice).getLexema().contentEquals("nao")) {
                        retorno = posFixa.trataNaoUnitario(filaPosFixa, indice);
                    } else {
                        retorno = posFixa.trataUnitario(filaPosFixa, indice);
                    }
                    break;

            }
            if (retorno != 1) {  //erro
                errosSintaticosSemantico = true;
                switch (retorno) {
                    case -1:
                        fraseContendoErro = fraseContendoErro.concat("Linha " + Integer.toString(token.getLinhaCodigo()) + " - Erro Semantico: " + "Para um operador unitario espera-se um inteiro");
                        break;
                    case -2:
                        fraseContendoErro = fraseContendoErro.concat("Linha " + Integer.toString(token.getLinhaCodigo()) + " - Erro Semantico: " + "Para o comando 'nao' espera-se um booleano");
                        break;
                    case -3:
                        fraseContendoErro = fraseContendoErro.concat("Linha " + Integer.toString(token.getLinhaCodigo()) + " - Erro Semantico: " + "Para um operador (+,-,div e mult) espera-se dois inteiros");
                        break;
                    case -4:
                        fraseContendoErro = fraseContendoErro.concat("Linha " + Integer.toString(token.getLinhaCodigo()) + " - Erro Semantico: " + "Para operadores relacionais espera-se dois inteiros");
                        break;
                    case -5:
                        fraseContendoErro = fraseContendoErro.concat("Linha " + Integer.toString(token.getLinhaCodigo()) + " - Erro Semantico: " + "Para 'e' e 'ou' espera-se dois booleanos");
                        break;
                    case -6:
                        fraseContendoErro = fraseContendoErro.concat("Linha " + Integer.toString(token.getLinhaCodigo()) + " - Erro Semantico: " + "Para '=' ou '!=' espera-se elementos do mesmo tipo (dois inteiros ou dois booleanos)");
                        break;
                }
            }

        }

        Operando resposta = (Operando) filaPosFixa.get(0);

        filaPosFixa.clear();;// reseta fila da pos fixa apos fim da expressao

        return resposta.getTipo();

    }

    private void erroTipoExpressao() {
        if(fraseContendoErro.contentEquals("")){
        fraseContendoErro = fraseContendoErro.concat("Linha " + Integer.toString(token.getLinhaCodigo()) + " - Erro Semantico: " + "Tipo da expressao é incompatível com o tipo do comando.");
        errosSintaticosSemantico = true;
        }
    }

    private void geraCodigoPosFixa(ArrayList<ElementosPosFixa> filaPosFixa) {
        for (ElementosPosFixa item : filaPosFixa) {
            if (item instanceof Operando) {

                if (item.getLexema().contentEquals("null")) {
                    gerador.geraCALL(pesquisaLabelProcedimentoFuncao(tokenAuxiliar.getLexema()));
                } else {
                    if (pesquisaDeclaracaoVariavel(item.getLexema())) {// se entrar aqui eh um identificador, entao gera LDV
                        gerador.geraLDV(((Operando) item).getMemoria());
                    } else {// se entrar aqui eh um numero

                        if (item.getLexema().contentEquals("verdadeiro")) {
                            gerador.geraLDC(1);// se verdadeiro, colocar 1
                        } else if (item.getLexema().contentEquals("falso")) {
                            gerador.geraLDC(0);// se falso, colocar 0
                        } else {
                            gerador.geraLDC(Integer.parseInt(item.getLexema()));//se entrar aqui eh um numero, entao gera LDC
                        }

                    }
                }

            } else {// se for operador entra aqui, entao identifica qual operador eh e chama o gerador pra ele

                switch (item.getLexema()) {

                    case "+":
                        gerador.geraADD();
                        break;
                    case "-":
                        gerador.geraSUB();
                        break;
                    case "*":
                        gerador.geraMULT();
                        break;
                    case "div":
                        gerador.geraDIVI();
                        break;
                    case "e":
                        gerador.geraAND();
                        break;
                    case "ou":
                        gerador.geraOR();
                        break;
                    case ">":
                        gerador.geraCMA();
                        break;
                    case "<":
                        gerador.geraCME();
                        break;
                    case ">=":
                        gerador.geraCMAQ();
                        break;
                    case "<=":
                        gerador.geraCMEQ();
                        break;
                    case "=":
                        gerador.geraCEQ();
                        break;
                    case "!=":
                        gerador.geraCDIF();
                        break;
                    case "nao":
                        gerador.geraNEG();
                        break;
                    case "+u":
                        gerador.geraINV();
                        break;
                    case "-u":
                        gerador.geraINV();
                        break;
                }
            }
        }

    }

    private int retornaPosicaoMemoria(String lexema) {

        for (int i = pilhaTabelaDeSimbolos.size(); i > 0; i--) {

            if (pilhaTabelaDeSimbolos.get(i - 1) instanceof TabelaDeSimbolosVariaveis) {
                TabelaDeSimbolosVariaveis aux = (TabelaDeSimbolosVariaveis) pilhaTabelaDeSimbolos.get(i - 1);

                if (aux.getLexema().contains(lexema)) {
                    return aux.getMemoria();
                }

            }
        }

        return -1;
    }

    private int pesquisaLabelProcedimentoFuncao(String lexema) {

        for (int i = pilhaTabelaDeSimbolos.size(); i > 0; i--) {

            if (pilhaTabelaDeSimbolos.get(i - 1) instanceof TabelaDeSimbolosProgramaProcedimentos) {
                TabelaDeSimbolosProgramaProcedimentos aux = (TabelaDeSimbolosProgramaProcedimentos) pilhaTabelaDeSimbolos.get(i - 1);

                if (aux.getLexema().contains(lexema)) {
                    return aux.getLabel();
                }

            } else {
                if (pilhaTabelaDeSimbolos.get(i - 1) instanceof TabelaDeSimbolosFuncoes) {
                    TabelaDeSimbolosFuncoes aux = (TabelaDeSimbolosFuncoes) pilhaTabelaDeSimbolos.get(i - 1);

                    if (aux.getLexema().contains(lexema)) {
                        return aux.getLabel();
                    }

                }

            }
        }
        return -1;
    }

    private void erroSemanticoRetornoDeFuncaoLugarIndevido() {
        fraseContendoErro = fraseContendoErro.concat("Linha " + Integer.toString(token.getLinhaCodigo()) + " - Erro Semantico: " + "Retorno de função deve estar dentro de uma função.");
        errosSintaticosSemantico = true;
    }

}

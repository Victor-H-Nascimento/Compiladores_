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
    private Stack<TabelaDeSimbolos> pilhaTabelaDeSimbolos = new Stack<TabelaDeSimbolos>();
    private Token tokenAuxiliar = null;
    private SimbolosToken simbolos = new SimbolosToken();
    private String fraseContendoErro = "";
    private boolean errosSintaticos = false;
    private boolean erroNaAtribuicao = true;
    private Stack<Token> pilhaOperadores = new Stack();
    private ArrayList<ElementosPosFixa> filaPosFixa = new ArrayList();
    private int rotuloLabel = 1;
    private int posicaoMemoria = 0;
    private final GeradorCodigo gerador = new GeradorCodigo();
    private int qtdVariaveisAlloc = 0;
    private int qtdVariaveisDalloc = 0;
    private int flag = 0;
    private int labelFlag;
    private boolean primeiroRetorno = false;
    private boolean chamadaFuncao = false;
    private boolean isFuncao = false;

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

        if (token.getSimbolo().equalsIgnoreCase("sPrograma")) {
            gerador.geraSTART();
            TabelaDeSimbolosProgramaProcedimentos programaTabelaSimbolos = new TabelaDeSimbolosProgramaProcedimentos(token.getLexema());
            pilhaTabelaDeSimbolos.push(programaTabelaSimbolos);
            token = analisadorLexico.lexico();

            if (token.getSimbolo().equalsIgnoreCase("sIdentificador") && !analisadorLexico.contemErrosLexicos() && !errosSintaticos) {
                token = analisadorLexico.lexico();
                //  insere_tabela(token.lexema,”nomedeprograma”,””,””) semantico

                if (token.getSimbolo().equalsIgnoreCase("sPontoVirgula") && !analisadorLexico.contemErrosLexicos() && !errosSintaticos) {
                    analisaBloco();
                    if (token.getSimbolo().equalsIgnoreCase("sPonto")) {
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
              //  decrementaPosicaoMemoria();
                qtdVariaveisDalloc++;
            }

        }

        if ((pilhaTabelaDeSimbolos.lastElement() instanceof TabelaDeSimbolosFuncoes || pilhaTabelaDeSimbolos.lastElement() instanceof TabelaDeSimbolosProgramaProcedimentos) && pilhaTabelaDeSimbolos.lastElement().isEscopo() && !pilhaTabelaDeSimbolos.lastElement().getLexema().contentEquals("programa")) {// muda escopo das funcoes pra falso, exceto se for o programa
            pilhaTabelaDeSimbolos.lastElement().setEscopo(false);
        }

        if (isFuncao) {
            isFuncao = false;
            if (qtdVariaveisDalloc > 0) {
                gerador.geraRETURNF(posicaoIncialDalloc - qtdVariaveisDalloc, qtdVariaveisDalloc);
                 qtdVariaveisDalloc = 0;
            }
            
            else{
                gerador.geraRETURNF();
            }
            
        } else {
            if (qtdVariaveisDalloc > 0) {
                gerador.geraDALLOC(posicaoIncialDalloc - qtdVariaveisDalloc, qtdVariaveisDalloc);
                qtdVariaveisDalloc = 0;
            }
        }

    }

    private void analisaEtapaVariaveis() throws IOException {

        if (token.getSimbolo().equalsIgnoreCase("sVar") && !errosSintaticos) {

            token = analisadorLexico.lexico();

            if (token.getSimbolo().equalsIgnoreCase("sIdentificador")) {
                while (token.getSimbolo().equalsIgnoreCase("sIdentificador")) {

                    analisaDeclaracaoVariaveis();

                    if (token.getSimbolo().equalsIgnoreCase("sPontoVirgula") && !analisadorLexico.contemErrosLexicos() && !errosSintaticos) {
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
            if (token.getSimbolo().equalsIgnoreCase("sIdentificador")) {
                if (pesquisaVariavelDuplicada(token.getLexema())) {
                    TabelaDeSimbolosVariaveis variaveisTabelaSimbolos = new TabelaDeSimbolosVariaveis(token.getLexema(), posicaoMemoria);
                    incrementaPosicaoMemoria();
                    qtdVariaveisAlloc++;
                    pilhaTabelaDeSimbolos.push(variaveisTabelaSimbolos);
                    token = analisadorLexico.lexico();
                    if (token.getSimbolo().equalsIgnoreCase("sVirgula") || token.getSimbolo().equalsIgnoreCase("sDoisPontos")) {

                        if (token.getSimbolo().equalsIgnoreCase("sVirgula")) {
                            token = analisadorLexico.lexico();

                            if (token.getSimbolo().equalsIgnoreCase("sDoisPontos")) {
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

        } while (!token.getSimbolo().equalsIgnoreCase("sDoisPontos") && !errosSintaticos);

        token = analisadorLexico.lexico();
        analisaTipo();

    }

    private void analisaTipo() throws IOException {
        if (!token.getSimbolo().equalsIgnoreCase("sInteiro") && !token.getSimbolo().equalsIgnoreCase("sBooleano") && !errosSintaticos) {
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
        if (token.getSimbolo().equalsIgnoreCase("sInicio") && !errosSintaticos) {
            token = analisadorLexico.lexico();
            analisaComandoSimples();

            while (!token.getSimbolo().equalsIgnoreCase("sFim") && !errosSintaticos) {
                if (token.getSimbolo().equalsIgnoreCase("sPontoVirgula")) {
                    token = analisadorLexico.lexico();

                    if (!token.getSimbolo().equalsIgnoreCase("sFim")) {
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
        if (token.getSimbolo().equalsIgnoreCase("sIdentificador")) {
            analisaAtribuicaoOuChamadaProcedimento();
        } else if (token.getSimbolo().equalsIgnoreCase("sSe")) {
            analisaSe();
        } else if (token.getSimbolo().equalsIgnoreCase("sEnquanto")) {
            analisaEnquanto();
        } else if (token.getSimbolo().equalsIgnoreCase("sLeia")) {
            analisaLeia();
        } else if (token.getSimbolo().equalsIgnoreCase("sEscreva")) {
            analisaEscreva();
        } else {
            analisaComandos();
        }
    }

    private void analisaAtribuicaoOuChamadaProcedimento() throws IOException {
        tokenAuxiliar = token;
        token = analisadorLexico.lexico();
        if (token.getSimbolo().equalsIgnoreCase("sAtribuicao")) {
            analisaAtribuicao();
        } else {
            analisaChamadaProcedimento();  //temos que implementar, nao tem no livro
        }
    }

    private void analisaLeia() throws IOException {
        token = analisadorLexico.lexico();
        if (token.getSimbolo().equalsIgnoreCase("sAbreParenteses") && !errosSintaticos) {
            token = analisadorLexico.lexico();
            if (token.getSimbolo().equalsIgnoreCase("sIdentificador") && !errosSintaticos) {
                if (pesquisaDeclaracaoVariavel(token.getLexema())) {
                    gerador.geraRD();
                    gerador.geraSTR(retornaPosicaoMemoria(token.getLexema()));
                    token = analisadorLexico.lexico();
                    if (token.getSimbolo().equalsIgnoreCase("sFechaParenteses") && !errosSintaticos) {
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
        if (token.getSimbolo().equalsIgnoreCase("sAbreParenteses") && !errosSintaticos) {
            token = analisadorLexico.lexico();
            if (token.getSimbolo().equalsIgnoreCase("sIdentificador") && !errosSintaticos) {
                if (pesquisaDeclaracaoFuncaoVariavel(token.getLexema())) {

                    if (pesquisaDeclaracaoVariavel(token.getLexema())) {// se variavel
                        gerador.geraLDV(retornaPosicaoMemoria(token.getLexema()));
                        gerador.geraPRN();
                    } else {//se funcao

                    }

                    token = analisadorLexico.lexico();
                    if (token.getSimbolo().equalsIgnoreCase("sFechaParenteses") && !errosSintaticos) {
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
        if (retorno.contentEquals(simbolos.getBooleano())) {
            if (token.getSimbolo().equalsIgnoreCase("sFaca") && !errosSintaticos) {
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

        if (retorno.contentEquals(simbolos.getBooleano())) {
            if (token.getSimbolo().equalsIgnoreCase("sEntao") && !errosSintaticos) {
                gerador.geraJMPF(labelSe);// gera jmpf
                token = analisadorLexico.lexico();
                analisaComandoSimples();
                if (token.getSimbolo().equalsIgnoreCase("sSenao")) {
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

        if (token.getSimbolo().equalsIgnoreCase("sProcedimento") || token.getSimbolo().equalsIgnoreCase("sFuncao")) {
            gerador.geraJMP(rotuloLabel);
            labelFlag = rotuloLabel;
            incrementaRotuloLabel();
            flag = 1;
            primeiroRetorno = true;
        }
        while (token.getSimbolo().equalsIgnoreCase("sProcedimento") || token.getSimbolo().equalsIgnoreCase("sFuncao")) {
            if (token.getSimbolo().equalsIgnoreCase("sProcedimento")) {
                analisaDeclaracaoProcedimento();
            } else {
                analisaDeclaracaoFuncao();
            }
            if (token.getSimbolo().equalsIgnoreCase("sPontoVirgula") && !errosSintaticos) {
                token = analisadorLexico.lexico();
            } else {
                mostraErros(";");
            }
        }
        if (flag == 1) {

            if (primeiroRetorno) {
                primeiroRetorno = false;
            } else {
                //gerador.geraRETURN();
            }
        }
    }

    private void analisaDeclaracaoProcedimento() throws IOException {
        token = analisadorLexico.lexico();
        //semantico
        if (token.getSimbolo().equalsIgnoreCase("sIdentificador") && !errosSintaticos) {
            //semantico

            if (pesquisaDeclaracaoProcedimento(token.getLexema())) {
                TabelaDeSimbolosProgramaProcedimentos procedimentoTabelaSimbolos = new TabelaDeSimbolosProgramaProcedimentos(token.getLexema(), rotuloLabel);
                pilhaTabelaDeSimbolos.push(procedimentoTabelaSimbolos);

                token = analisadorLexico.lexico();
                if (token.getSimbolo().equalsIgnoreCase("sPontoVirgula") && !errosSintaticos) {
                    gerador.geraNULL(rotuloLabel);
                    incrementaRotuloLabel();
                    analisaBloco();
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
        if (token.getSimbolo().equalsIgnoreCase("sIdentificador") && !errosSintaticos) {
            //semantico

            if (pesquisaDeclaracaoFuncao(token.getLexema())) {
                TabelaDeSimbolosFuncoes funcaoTabelaSimbolos = new TabelaDeSimbolosFuncoes(token.getLexema(), rotuloLabel);
                pilhaTabelaDeSimbolos.push(funcaoTabelaSimbolos);

                token = analisadorLexico.lexico();
                if (token.getSimbolo().equalsIgnoreCase("sDoisPontos") && !errosSintaticos) {
                    token = analisadorLexico.lexico();
                    if (token.getSimbolo().equalsIgnoreCase("sInteiro") || token.getSimbolo().equalsIgnoreCase("sBooleano") && !errosSintaticos) {
                        //semantico

                        preencheTipoFuncao(funcaoTabelaSimbolos, token);

                        token = analisadorLexico.lexico();
                        if (token.getSimbolo().equalsIgnoreCase("sPontoVirgula")) {
                            gerador.geraNULL(rotuloLabel);
                            incrementaRotuloLabel();
                            isFuncao = true;
                            analisaBloco();
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
        if (token.getSimbolo().equalsIgnoreCase("sMaior") || token.getSimbolo().equalsIgnoreCase("sMaiorIgual") || token.getSimbolo().equalsIgnoreCase("sIgual") || token.getSimbolo().equalsIgnoreCase("sMenor") || token.getSimbolo().equalsIgnoreCase("sMenorIgual") || token.getSimbolo().equalsIgnoreCase("sDiferente")) {
            verificaEAdicionaPilhaOperadores(token);
            token = analisadorLexico.lexico();
            analisaExpressaoSimples();
        }

    }

    private void analisaExpressaoSimples() throws IOException {
        if (token.getSimbolo().equalsIgnoreCase("sMais") || token.getSimbolo().equalsIgnoreCase("sMenos")) {

            if (token.getSimbolo().equalsIgnoreCase("sMais")) {
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

        while (token.getSimbolo().equalsIgnoreCase("sMais") || token.getSimbolo().equalsIgnoreCase("sMenos") || token.getSimbolo().equalsIgnoreCase("sOu")) {
            verificaEAdicionaPilhaOperadores(token);//add +/-/ou
            token = analisadorLexico.lexico();
            analisaTermo();
        }

    }

    private void analisaTermo() throws IOException {
        analisaFator();
        while (token.getSimbolo().equalsIgnoreCase("sMultiplicacao") || token.getSimbolo().equalsIgnoreCase("sDivisao") || token.getSimbolo().equalsIgnoreCase("sE")) {
            verificaEAdicionaPilhaOperadores(token);//add */div/e
            token = analisadorLexico.lexico();
            analisaFator();
        }
    }

    private void analisaFator() throws IOException {
        if (token.getSimbolo().equalsIgnoreCase("sIdentificador")) {// se variavel ou funcao
            //semantico
            if (pesquisaDeclaracaoFuncaoVariavel(token.getLexema())) {//lexema
                if (pesquisaTipoFuncao(token)) {
                    analisaChamadaFuncao();
                    chamadaFuncao = true;
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
            if (token.getSimbolo().equalsIgnoreCase("sNumero")) {
                Operando elemento = new Operando();
                elemento.setLexema(token.getLexema());
                elemento.setTipo(simbolos.getInteiro());

                filaPosFixa.add(elemento);//add numero na saida pos fixa
                token = analisadorLexico.lexico();

            } else if (token.getSimbolo().equalsIgnoreCase("sNao")) {
                verificaEAdicionaPilhaOperadores(token);//nao
                token = analisadorLexico.lexico();
                analisaFator();
            } else if (token.getSimbolo().equalsIgnoreCase("sAbreParenteses")) {
                verificaEAdicionaPilhaOperadores(token);//add abre parenteses
                token = analisadorLexico.lexico();
                analisaExpressao();// ver condicao do retorno
                if (token.getSimbolo().equalsIgnoreCase("sFechaParenteses") && !errosSintaticos) {
                    verificaEAdicionaPilhaOperadores(token);//add fecha parenteses
                    token = analisadorLexico.lexico();
                } else {
                    mostraErros(")");
                }

            } else if ((token.getLexema().equalsIgnoreCase("verdadeiro") || token.getLexema().equalsIgnoreCase("falso")) && !errosSintaticos) {
                token = analisadorLexico.lexico();
            } else {
                mostraErros("verdadeiro ou falso");
            }
        }
    }

    private void analisaAtribuicao() throws IOException {

        //verificar se tokenAuxiliar é uma variavel e se já está na tabela de simbolos
        for (int i = pilhaTabelaDeSimbolos.size(); i > 0; i--) {
            if (pilhaTabelaDeSimbolos.elementAt(i - 1).getLexema().contentEquals(tokenAuxiliar.getLexema()) && (pilhaTabelaDeSimbolos.elementAt(i - 1) instanceof TabelaDeSimbolosVariaveis || pilhaTabelaDeSimbolos.elementAt(i - 1) instanceof TabelaDeSimbolosFuncoes)) {
                erroNaAtribuicao = false;
                break;
            }
        }

        if (!erroNaAtribuicao) {
            token = analisadorLexico.lexico();
            analisaExpressao();// ver condicao do retorno
            fimInFixa();
            if (!chamadaFuncao) {// se nao era funcao
                String retorno = verificaPosFixa();

                if (pesquisaDeclaracaoVariavel(tokenAuxiliar.getLexema())) { // se for variavel gerar STR
                    gerador.geraSTR(retornaPosicaoMemoria(tokenAuxiliar.getLexema()));
                } else {
                    //   gerador.geraRETURNF(0, 0);
                }

                if (!retorno.contentEquals(pesquisaTipoVariavel(tokenAuxiliar.getLexema()))) {// erro se o tipo da variavel/funcao do lado esquerdo for diferente do tipo da expressao
                    erroTipoExpressao();
                }
            } else {// se acabou de chamar uma funcao
                chamadaFuncao = false;
            }

            erroNaAtribuicao = true;

        } else {
            erroSemanticoLadoEsquerdoAtribuicao();
        }

    }

    private void analisaChamadaProcedimento() {
        //verificar se tokenAuxiliar é um procedimento e está guardado na tabela de simbolos

        for (int i = pilhaTabelaDeSimbolos.size(); i > 0; i--) {
            if (pilhaTabelaDeSimbolos.elementAt(i - 1).getLexema().contentEquals(tokenAuxiliar.getLexema()) && pilhaTabelaDeSimbolos.elementAt(i - 1) instanceof TabelaDeSimbolosProgramaProcedimentos) {
                erroNaAtribuicao = false;
                break;
            }
        }

        if (!erroNaAtribuicao) {// entra aqui se nao houve nenhum errado na declaracao do procedimento
            erroNaAtribuicao = true;
            if (flag == 1) {
                flag = 0;
                gerador.geraNULL(labelFlag);
            }
            gerador.geraCALL(pesquisaLabelProcedimentoFuncao(tokenAuxiliar.getLexema()));
        } else {
            erroSemanticoLadoEsquerdoChamadaProcedimento();
        }

    }

    private void analisaChamadaFuncao() throws IOException {

        if (flag == 1) {
            flag = 0;
            gerador.geraNULL(labelFlag);
        }

        gerador.geraCALL(pesquisaLabelProcedimentoFuncao(token.getLexema()));

        token = analisadorLexico.lexico();
    }

    private void mostraErros(String erroEncontrado) {

        if (analisadorLexico.contemErrosLexicos() && !errosSintaticos) {
            System.out.println("Linha " + token.getLinhaCodigo() + " - Erro Léxico: Caracter " + token.getLexema() + " não tem função definida.");
            fraseContendoErro = fraseContendoErro.concat("Linha " + Integer.toString(token.getLinhaCodigo()) + " - Erro Léxico: " + token.getLexema() + " não tem função definida.");
        } else if (!analisadorLexico.contemErrosLexicos() && !errosSintaticos) {
            System.out.println("Linha " + token.getLinhaCodigo() + " - Erro Sintatico: " + erroEncontrado + " esperado");
            fraseContendoErro = ("Linha " + token.getLinhaCodigo() + " - Erro Sintatico: " + erroEncontrado + " esperado");
        }
        errosSintaticos = true;
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
                    return false;
                }
            }

        }
        return true;
    }

    private boolean pesquisaDeclaracaoFuncao(String lexema) {
        for (TabelaDeSimbolos item : pilhaTabelaDeSimbolos) {

            if (item instanceof TabelaDeSimbolosFuncoes) {
                if (item.getLexema().contentEquals(lexema)) {
                    return false;
                }
            }

        }
        return true;
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
        errosSintaticos = true;
    }

    private void erroSemanticoLadoEsquerdoAtribuicao() {
        fraseContendoErro = fraseContendoErro.concat("Linha " + Integer.toString(token.getLinhaCodigo()) + " - Erro Semantico: " + "Tipo incompatível no lado esquerdo da atribuição");
        errosSintaticos = true;
    }

    private void erroSemanticoLadoEsquerdoChamadaProcedimento() {
        fraseContendoErro = fraseContendoErro.concat("Linha " + Integer.toString(token.getLinhaCodigo()) + " - Erro Semantico: " + "Tipo incompatível. Espera-se procedimento.");
        errosSintaticos = true;
    }

    private void erroSemanticoVariavelIncompativel(String lexema) {
        fraseContendoErro = fraseContendoErro.concat("Linha " + Integer.toString(token.getLinhaCodigo()) + " - Erro Semantico: " + lexema + " é incompatível, espera-se uma variavel.");
        errosSintaticos = true;
    }

    private void erroSemanticoVariavelFuncaoIncompativel(String lexema) {
        fraseContendoErro = fraseContendoErro.concat("Linha " + Integer.toString(token.getLinhaCodigo()) + " - Erro Semantico: " + lexema + " é incompatível, espera-se uma variavel ou uma funcao.");
        errosSintaticos = true;
    }

    private void erroSemanticoProcedimentoIncompativel(String lexema) {
        fraseContendoErro = fraseContendoErro.concat("Linha " + Integer.toString(token.getLinhaCodigo()) + " - Erro Semantico: " + lexema + " é incompatível, espera-se um procedimento.");
        errosSintaticos = true;
    }

    private void erroSemanticoFuncaoIncompativel(String lexema) {
        fraseContendoErro = fraseContendoErro.concat("Linha " + Integer.toString(token.getLinhaCodigo()) + " - Erro Semantico: " + lexema + " é incompatível, espera-se uma funcao.");
        errosSintaticos = true;
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

        while (filaPosFixa.size() > 1) {
            int indice = 0;
            while (filaPosFixa.get(indice) instanceof Operando) {
                indice++;
            }

            FuncoesPosFixa posFixa = new FuncoesPosFixa();

            int prioridade = getPrioridade(filaPosFixa.get(indice).getLexema());

            switch (prioridade) {

                case 0:
                    posFixa.trataEOu(filaPosFixa, indice);
                    break;

                case 1:
                    posFixa.trataEOu(filaPosFixa, indice);
                    break;
                case 2:
                    if (filaPosFixa.get(indice).getLexema().contentEquals("!=") || filaPosFixa.get(indice).getLexema().contentEquals("=")) {
                        posFixa.trataIgualDiferente(filaPosFixa, indice);
                    } else {
                        posFixa.trataRelacionais(filaPosFixa, indice);
                    }
                    break;
                case 3:
                    posFixa.trataMultDivSomaSub(filaPosFixa, indice);
                    break;
                case 4:
                    posFixa.trataMultDivSomaSub(filaPosFixa, indice);
                    break;
                case 5:
                    if (filaPosFixa.get(indice).getLexema().contentEquals("nao")) {
                        posFixa.trataNaoUnitario(filaPosFixa, indice);
                    } else {
                        posFixa.trataUnitario(filaPosFixa, indice);
                    }
                    break;

            }

        }

        Operando resposta = (Operando) filaPosFixa.get(0);

        filaPosFixa.clear();;// reseta fila da pos fixa apos fim da expressao

        return resposta.getTipo();

    }

    private void erroTipoExpressao() {
        fraseContendoErro = fraseContendoErro.concat("Linha " + Integer.toString(token.getLinhaCodigo()) + " - Erro Semantico: " + "Tipo da expressao é incompatível com o tipo do comando.");
        errosSintaticos = true;
    }

    private void geraCodigoPosFixa(ArrayList<ElementosPosFixa> filaPosFixa) {
        for (ElementosPosFixa item : filaPosFixa) {
            if (item instanceof Operando) {
                if (pesquisaDeclaracaoVariavel(item.getLexema())) {// se entrar aqui eh um identificador, entao gera LDV
                    gerador.geraLDV(((Operando) item).getMemoria());
                } else {// se entrar aqui eh um numero
                    gerador.geraLDC(Integer.parseInt(item.getLexema()));//se entrar aqui eh um numero, entao gera LDC
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
}

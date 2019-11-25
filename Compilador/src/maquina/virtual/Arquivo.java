package maquina.virtual;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 *
 * @author victor
 */
public class Arquivo {

    private boolean jaExiste;

    public Arquivo() {//construtor
    }

    public void Read(Funcoes c) {

        InputStreamReader arq = new InputStreamReader(getClass().getResourceAsStream("saidaAssembly.txt"));
        BufferedReader lerArq = new BufferedReader(arq);
        String linha = "";
        try {

            do {
                linha = lerArq.readLine();

                c.fila.add(linha);
            } while (!linha.contains("HLT"));

            arq.close();

         
        } catch (IOException ex) {
            System.out.println("Erro: Não foi possível ler o arquivo!");
        }
    }
    

    public void EnderecaJMP(Funcoes c, ArrayList<ListaAuxiliar> fila) {

        for (int x = 0; x < c.fila.size(); x++) {
            String linhaComInstrucao = c.fila.get(x).toString();


            /*
                Transformar os parametros L%d (Ex. L3) em um valor inteiro que aponte para o indice da fila principal
                Valor de L%d esta em label
             */
            if (linhaComInstrucao.contains("JMP") || linhaComInstrucao.contains("JMPF") || linhaComInstrucao.contains("CALL")) {
                String instrucao = linhaComInstrucao.split(" ")[0];
                String label = linhaComInstrucao.split(" ")[1];

                for (int i = 0; i < c.fila.size(); i++) {
                    String a = c.fila.get(i).toString();

                    if (a.startsWith(label.concat(" "))) {

                        for (ListaAuxiliar verificacao : fila) {
                            if (i == verificacao.getIndice()) {
                                jaExiste = true;
                            }
                        }

                        if (!jaExiste) {
                            ListaAuxiliar struct = new ListaAuxiliar();
                            struct.setInstrucao(instrucao);
                            struct.setLabel(label);
                            struct.setIndice(i);
                            fila.add(struct);
                        } else {
                            jaExiste = false;
                        }

                        break;
                    }
                }

            }

        }

    }

}

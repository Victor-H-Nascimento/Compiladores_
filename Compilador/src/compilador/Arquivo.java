package compilador;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author victor
 */
public class Arquivo {

    private FileReader arq;
    private BufferedReader lerArq;

    public Arquivo() {//construtor

    }

    public void Ler(String Caminho, Funcoes c) {

        try {
            arq = new FileReader(Caminho);
            lerArq = new BufferedReader(arq);
            boolean continua = true;

            try {

                do {

                    String aux = lerArq.readLine();

                    if (aux != null) {
                        c.leArquivo(aux);
                    } else {
                        continua = false;
                    }

                } while (continua);

                arq.close();

            } catch (IOException ex) {
                System.out.println("Erro: Não foi possível ler o arquivo!");
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Erro: Arquivo não encontrado!");
        }
    }

}

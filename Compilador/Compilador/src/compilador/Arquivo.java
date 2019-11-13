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
            FileReader arq = new FileReader(Caminho);
            BufferedReader lerArq = new BufferedReader(arq);
            boolean continua = true;
            
            try {

                do {
                    
                    //linha = lerArq.read();
                    //c.leArquivo(Character.toString((char) linha));
                    String aux = lerArq.readLine();
                    
                    if (aux != null) {
                         c.leArquivo(aux);
                    }
                    
                    else{
                        continua = false;
                    }
                    
                    //System.out.println("Oq estou lendo?" + lerArq.readLine());
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

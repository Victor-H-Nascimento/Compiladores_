//OBS:
//Na funcao Call ao inves de inserirmos o valor i+1, nos inserimos o valor i e na funcao JMPF nao atualizamos o valor de i dentro da funcao
// esses comandos continuaram funcionando pq na Maquina Virtual nos atualizamos o i a cada iteraçao
package maquina.virtual;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

/**
 *
 * @author victor
 */
public class Funcoes {

    //atributos
    private int s;//topo da plha
    private int i;//indice proxima instrucao
    private final int numeroNULL;
    public Stack pilha = new Stack();
    public ArrayList<Object> fila = new ArrayList();
    private final Scanner scanner = new Scanner(System.in);

    //construtor
    public Funcoes() {
        this.i = 0;
        this.numeroNULL = -999999;
    }

    //getters e setter
    public int getI() {
        return i;
    }

    public void setI() {
        i++;
    }

    //metodos abstratos
    public void PRINTAPILHA() {
        for (int j = pilha.size() - 1; j >= 0; j--) {
            int primeiroValor = (int) pilha.elementAt(j);
            System.out.println(" s-> " + j + "       " + "|" + primeiroValor + "|");
        }
    }

    public void LDC(int k) {//carrega constante
        //S:=s + 1 ; M [s]: = k 
        s++;
        pilha.add(k);//M[s]: = k
    }

    public void LDV(int n) {//carrega valor
        //S:=s + 1 ; M [s]: = M[n] 
        s++;
        int aux = (int) pilha.elementAt(n);
        pilha.add(aux); //M[s]: = M[n]  
    }

    public void ADD() {  //somar  
        //M[s-1]:=M[s-1] + M[s]; s:=s - 1
        pilha.add((int) pilha.elementAt(pilha.size() - 1) + (int) pilha.elementAt(pilha.size() - 2));
        pilha.pop();
        pilha.pop();
        s = pilha.size() - 1; //atualiza s
    }

    public void SUB() {
        //M[s-1]:=M[s-1] - M[s]; s:=s - 1
        pilha.add((int) pilha.elementAt(pilha.size() - 1) - (int) pilha.elementAt(pilha.size() - 2));
        pilha.pop();
        pilha.pop();
        s = pilha.size() - 1; //atualiza s
    }

    public void MULT() {
        //M[s-1]:=M[s-1] * M[s]; s:=s - 1
        pilha.add((int) pilha.elementAt(pilha.size() - 1) * (int) pilha.elementAt(pilha.size() - 2));
        pilha.pop();
        pilha.pop();
        s = pilha.size() - 1; //atualiza s
    }

    public void DIVI() {
        //M[s-1]:=M[s-1] / M[s]; s:=s - 1

        pilha.add((int) pilha.elementAt(pilha.size() - 1) / (int) pilha.elementAt(pilha.size() - 2));
        pilha.pop();
        pilha.pop();
        s = pilha.size() - 1; //atualiza s
    }

    public void INV() {
        // M[s]:= -M[s] 
        pilha.add((int) pilha.elementAt(pilha.size() - 1) * -1);
        pilha.pop();//remove o topo
    }

    public void AND() {
        //se M [s-1] = 1 e M[s] = 1  então M[s-1]:=1  senão M[s-1]:=0;  s:=s - 1 
        int primeiroValor = (int) pilha.elementAt(pilha.size() - 1);
        pilha.pop();//remove o topo
        int segundoValor = (int) pilha.elementAt(pilha.size() - 1);
        pilha.pop();//duas vezes para tirar os 2 valores 

        if (primeiroValor == segundoValor && primeiroValor == 1) {
            pilha.add(primeiroValor);
        } else {
            if (segundoValor == 0) {
                pilha.add(segundoValor);
            } else {
                pilha.add(primeiroValor);
            }
        }
        s = pilha.size() - 1; //atualiza s

    }

    public void OR() {
        //e M[s-1] = 1  ou M[s] = 1  então M[s-1]:=1  senão M[s-1]:=0; s:=s - 1 

        int primeiroValor = (int) pilha.elementAt(pilha.size() - 1);
        pilha.pop();//remove o topo
        int segundoValor = (int) pilha.elementAt(pilha.size() - 1);
        pilha.pop();//duas vezes para tirar os 2 valores 

        if (primeiroValor == 1) {
            pilha.add(primeiroValor);
        } else {
            pilha.add(segundoValor);
        }
        s = pilha.size() - 1; //atualiza s
    }

    public void NEG() {
        // M[s]:=1 - M[s] 
        pilha.add(1 - (int) pilha.elementAt(s));
        pilha.pop();//remove o topo
    }

    public void CME() {
        //se M[s-1] < M[s]  então M[s-1]:=1  senão M[s-1]:=0; s:=s - 1 

        int primeiroValor = (int) pilha.elementAt(pilha.size() - 1);
        pilha.pop();//remove o topo
        int segundoValor = (int) pilha.elementAt(pilha.size() - 1);
        pilha.pop();//duas vezes para tirar os 2 valores somados

        if (segundoValor < primeiroValor) {
            pilha.add(1);
        } else {
            pilha.add(0);
        }

        s = pilha.size() - 1; //atualiza s

    }

    public void CMA() {
        //se M[s-1] > M[s]  então M[s-1]:=1  senão M[s-1]:=0; s:=s - 1
        int primeiroValor = (int) pilha.elementAt(pilha.size() - 1);
        pilha.pop();//duas vezes para tirar os 2 valores somados
        int segundoValor = (int) pilha.elementAt(pilha.size() - 1);
        pilha.pop();//duas vezes para tirar os 2 valores somados

        if (segundoValor > primeiroValor) {
            pilha.add(1);
        } else {
            pilha.add(0);
        }

        s = pilha.size() - 1; //atualiza s
    }

    public void CEQ() {
        //se M[s-1] = M[s]  então M[s-1]:=1  senão M[s-1]:=0; s:=s - 1

        int primeiroValor = (int) pilha.elementAt(pilha.size() - 1);
        pilha.pop();//duas vezes para tirar os 2 valores somados
        int segundoValor = (int) pilha.elementAt(pilha.size() - 1);
        pilha.pop();//duas vezes para tirar os 2 valores somados

        if (primeiroValor == segundoValor) {
            pilha.add(1);
        } else {
            pilha.add(0);
        }

        s = pilha.size() - 1; //atualiza s
    }

    public void CDIF() {
        //se M[s-1] != M[s]  então M[s-1]:=1  senão M[s-1]:=0; s:=s - 1

        int primeiroValor = (int) pilha.elementAt(pilha.size() - 1);
        pilha.pop();//duas vezes para tirar os 2 valores somados
        int segundoValor = (int) pilha.elementAt(pilha.size() - 1);
        pilha.pop();//duas vezes para tirar os 2 valores somados

        if (primeiroValor != segundoValor) {
            pilha.add(1);
        } else {
            pilha.add(0);
        }

        s = pilha.size() - 1; //atualiza s
    }

    public void CMEQ() {
        //se M[s-1] <= M[s]  então M[s-1]:=1  senão M[s-1]:=0; s:=s - 1

        int primeiroValor = (int) pilha.elementAt(pilha.size() - 1);
        pilha.pop();//duas vezes para tirar os 2 valores somados
        int segundoValor = (int) pilha.elementAt(pilha.size() - 1);
        pilha.pop();//duas vezes para tirar os 2 valores somados

        if (segundoValor <= primeiroValor) {
            pilha.add(1);
        } else {
            pilha.add(0);
        }

        s = pilha.size() - 1; //atualiza s
    }

    public void CMAQ() {
        //se M[s-1] >= M[s]  então M[s-1]:=1  senão M[s-1]:=0; s:=s - 1

        int primeiroValor = (int) pilha.elementAt(pilha.size() - 1);
        pilha.pop();//duas vezes para tirar os 2 valores somados
        int segundoValor = (int) pilha.elementAt(pilha.size() - 1);
        pilha.pop();//duas vezes para tirar os 2 valores somados

        if (segundoValor >= primeiroValor) {
            pilha.add(1);
        } else {
            pilha.add(0);
        }

        s = pilha.size() - 1; //atualiza s
    }

    public void START() {
        this.s = -1;
    }

    public void HLT() {
        //  “Para a execução da MVD”
        //como parar?
    }

    public void STR(int n) {
        //M[n]:=M[s]; s:=s-1
        if (pilha.size() - 1 >= 1 && n <= pilha.size() - 1) {
            int primeiroValor = (int) pilha.elementAt(pilha.size() - 1);
            pilha.remove(n);// se this.s == 1, entao n tem q ser obrigatoriamente 0
            pilha.insertElementAt(primeiroValor, n);
            pilha.pop();
            s = pilha.size() - 1; //atualiza s   
        } else {
            System.out.println("Nao chamou funcao STR " + s);
        }

    }

    public void JMP(int t) {
        i = t;
    }

    public void JMPF(int t) {
        //se M[s] = 0 então i:=t senão i:=i + 1;s:=s-1
        if ((int) pilha.elementAt(pilha.size() - 1) == 0) {
            i = t;
        }
        pilha.pop();//remove o topo
        s = pilha.size() - 1; //atualiza s
    }

    public void NULL() {
    }

    public void RD() {
        s = pilha.size(); //atualiza s
        System.out.println("Entrada de dados: ");
        pilha.add(Interface.entradaDados());
    }

    public void PRN() {

        int primeiroValor = (int) pilha.elementAt(pilha.size() - 1);
        System.out.println(" PRN s-> " + s + "       " + "|" + primeiroValor + "|");
        pilha.pop();//remove o topo
        s = pilha.size() - 1; //atualiza s
    }

    public void ALLOC(int m, int n) {
        //ALLOC     m,n      (Alocar memória): Para k:=0 até n-1 faça {s:=s + 1; M[s]:=M[m+k]}

        if (!pilha.isEmpty() && m <= this.s) {// se nao estiver vazia, entao faca alloc

            for (int k = 0; k < n; k++) {
                s++;
                int aux = (int) pilha.elementAt(m + k);
                pilha.add(aux);
                pilha.remove(m + k);
                pilha.insertElementAt(numeroNULL, m + k);
            }
        } else {
            for (int j = 0; j < n; j++) {
                s++;
                pilha.add(numeroNULL);// -999999 eh o nosso null
            }
        }

    }

    public void DALLOC(int m, int n) {
        //DALLOC  m,n      (Desalocar memória): Para  k:=n-1  até 0  faça       {M[m+k]:=M[s]; s:=s - 1} 
        for (int k = 0; k <= n - 1; k++) {

            if (m + k <= pilha.size() - 1) {
                int aux = (int) pilha.elementAt(pilha.size() - 1);
                pilha.remove(m + k);
                pilha.insertElementAt(aux, m + k);
                pilha.pop();
                s = pilha.size() - 1;
            } else {
                pilha.pop();// excluir o topo caso o m+k seja maior q o topo, pois caso contrario o topo da pilha tera NULL
            }
        }
    }

    public void CALL(int t) {
        //CALL   t   (Chamar procedimento ou função):  S:=s + 1; M[s]:=i + 1; i:=t 
        s = pilha.size();
        pilha.add(i); // no noso caso inserimos a posicao i, ao inves de i+1.
        i = t;
    }

    public void RETURN() {
        // i:=M[s]; s:=s - 1 

        i = (int) pilha.elementAt(pilha.size() - 1);
        pilha.pop();
        s = pilha.size() - 1;
    }

    public void RETURNF(int m, int n) {
        // i:=M[s]; s:=s - 1 // Dalloc (m,n) // 
        int aux = (int) pilha.elementAt(pilha.size() - 1);
        pilha.pop();
        DALLOC(m, n);
        s++;//posicao do Topo
        pilha.add(aux);
        RETURN();
    }

    public void RETURNF() {
        RETURN();
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compilador;

import java.io.IOException;
import javax.swing.JFrame;

/**
 *
 * @author victor
 */
public class Inicial {
    
     public static void main(String[] args) throws IOException {
        InterfaceEditor primeiraInterface = new InterfaceEditor();
        primeiraInterface.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        primeiraInterface.setLocationRelativeTo(null);
        primeiraInterface.setVisible(true);
}
    
}



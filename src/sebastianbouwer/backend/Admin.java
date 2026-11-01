/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sebastianbouwer.backend;

/**
 *
 * @author user
 */
public class Admin extends Account{
    
    public Admin(String sUI, String sU, String sP, String sJD, boolean sIA, 
            /*settings*/ String sT, String sRT, String sLSP, String sOSP, boolean bN, int iMCS) {
        super(sUI, sU, sP, sJD, true, sT, sRT, sLSP, sOSP, bN, iMCS);
    }
    
}

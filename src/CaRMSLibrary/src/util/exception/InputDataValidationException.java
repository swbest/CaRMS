/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util.exception;

/**
 *
 * @author dtjldamien
 */
public class InputDataValidationException extends Exception {

    public InputDataValidationException() {
    }

    public InputDataValidationException(String msg) {
        super(msg);
    }
}

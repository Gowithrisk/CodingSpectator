/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator;

public class UninitializedFinalTestFile {

    static final String CONSTANT;

    public static void main(String[] args) {
        System.out.println(CONSTANT);
    }

}
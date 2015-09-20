package edu.tce.cse.document;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.HashMap;
import java.util.Map;
import mpi.Datatype;
import mpi.MPIException;
import mpi.User_function;

/**
 *
 * @author Sidharth Manohar
 */

class DocumentFrequencyReducer extends User_function {

    @Override
    public void Call(Object inVec, int inOffset, Object inOutVec, 
            int inOutOffset, int count, Datatype dataType) throws MPIException {

        Map<String, Integer> inDocumentFrequencyMap = 
                ((Map<String, Integer>[]) inVec)[0];

        Object[] inOut = (Object[]) inOutVec;

        Map<String, Integer> inOutDocumentFrequencyMap = 
                (Map<String, Integer>) inOut[0];

        for (String word : inDocumentFrequencyMap.keySet()) {
            int frequency = inDocumentFrequencyMap.get(word);
            if (inOutDocumentFrequencyMap.containsKey(word)) {
                frequency += inOutDocumentFrequencyMap.get(word);
            }
            inOutDocumentFrequencyMap.put(word, frequency);
        }
    }
}

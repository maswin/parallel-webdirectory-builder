package edu.tce.cse.document;

import java.util.List;
import java.util.Map;

import mpi.Datatype;
import mpi.MPIException;
import mpi.User_function;

public class TfIdfReducer extends User_function {
	@Override
    public void Call(Object inVec, int inOffset, Object inOutVec, 
            int inOutOffset, int count, Datatype dataType) throws MPIException {

		List<double[]> inTfIdfVector = 
                ((List<double[]>[]) inVec)[0];

        Object[] inOut = (Object[]) inOutVec;

        List<double[]> inOutTfIdfVector = 
                (List<double[]>) inOut[0];

        for (double[] tfIdf: inTfIdfVector) {
            inOutTfIdfVector.add(tfIdf);
        }
    }
}

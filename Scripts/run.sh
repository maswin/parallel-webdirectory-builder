#!/bin/bash

mpjrun.sh -np $1 -Xmx2g -cp bin/:lib/lucene-analyzers-common-5.2.1.jar:lib/lucene-core-5.2.1.jar:lib/mpj.jar:lib/parallelcolt-0.9.4.jar:lib/commons-math3-3.5.jar:lib/kd.jar:lib/colt.jar:lib/jaxen-1.1-beta-6.jar:lib/dom4j-1.6.1.jar edu.tce.cse.webdirectorybuilder.WebDirectoryBuilder -i $2 -o $3 nogui

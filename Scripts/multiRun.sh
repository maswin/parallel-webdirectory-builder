#! /bin/bash

io="Trail"
end=".txt"
totalOutput="Output.txt"
numOfProcessor="1"
otherCommand=""
inputFolderPath="Input/"
outputFolderPath="Output/"

inputFolder=("TestDocuments" "Economics" "Geography" "Health" "History" "Literature" "Mathematics" "Music" "Science" "Sports" "Technology")
inputFolder1=("TestDocuments")
for folder in "${inputFolder[@]}"
do
	echo "Working On "$folder
	for run in {1..15}
	do
		command="./run.sh $numOfProcessor $inputFolderPath$folder $outputFolderPath$folder$io$run$end"
		echo $command
		echo $outputFolderPath$folder$totalOutput
		$command >> $outputFolderPath$folder$totalOutput
		echo "Trail "$run" Over"
	done
done


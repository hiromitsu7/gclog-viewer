#!/bin/bash

set -u

mvn clean compile exec:java -Dexec.mainClass=hiromitsu.gclogviewer.Main -Dexec.args=./verbosegc.001.log

grep "global" output_all.csv > output_global.csv 

INFILE="output_all.csv"
INFILE_GLOBAL="output_global.csv"
OUTFILE="${INFILE%.*}.png"

gnuplot <<EOF
set datafile separator ','
set xdata time
set timefmt '%Y-%m-%dT%H:%M:%S'
set format x '%m/%d %H:%M'
set grid xtics ytics
set key outside
set y2tics
set xlabel 'time'
set ylabel 'heap[MB]'
set y2label 'duration[ms]'
set term png size 1600,960
set output '$OUTFILE'
plot '$INFILE' u 1:(\$3/1024.0/1024) with line title 'used', '$INFILE' u 1:(\$4/1024.0/1024) with line title 'total', '$INFILE' u 1:5 with line axes x1y2 title 'duration', '$INFILE_GLOBAL' u 1:5 axes x1y2 title 'global duration'
set output
EOF

rm -f output_all.csv
rm -f output_global.csv
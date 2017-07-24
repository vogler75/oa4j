for f in `ls *.cpp *.cxx *.hxx *.h`
do
  cat $f | sed "s/at_rocworks_oc4j/at_rocworks_oa4j/g" |  
           sed "s/at\.rocworks\.oc4j/at.rocworks.oa4j/g" |
           sed "s/at\/rocworks\/oc4j/at\/rocworks\/oa4j/g" > tmp
  mv tmp $f
done

for f in `ls at_rocworks_oc4j_*.cpp at_rocworks_oc4j_*.h`
do
  n=`echo $f | sed "s/at_rocworks_oc4j/at_rocworks_oa4j/"`
  echo $n
  mv $f $n
done

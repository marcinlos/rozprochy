echo "Copying $1..."
echo "student:"
scp -r $1 los@student.agh.edu.pl:~/rozprochy/
echo "Jagular:"
scp -r $1 los@jagular.iisg.agh.edu.pl:~/rozprochy/

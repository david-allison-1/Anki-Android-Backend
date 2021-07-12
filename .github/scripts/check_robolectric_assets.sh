ls -l

SO=$(find . -name "*.so" | wc -l)
DYLIB=$(find . -name "*.dylib" | wc -l)
DLL=$(find . -name "*.dll" | wc -l)
if [ $SO -ne 1 ] 
then 
    echo "Error: Expected 1 .so file"; 
	exit 1; 
fi

if [ $DYLIB -ne 1 ]
then 
    echo "Error: Expected 1 .dylib file" 
	exit 1
fi
if [ $DLL -ne 1 ]
then 
	echo "Error: Expected 1 .dll file" 
	exit 1
fi

# list the libraries
objdump --private-headers rsdroid.dll  | grep "DLL Name:"

objdump --private-headers rsdroid.dll  | grep "DLL Name:" | grep "libgcc"
hasGcc=$?
if [ "$hasGcc" -eq 0  ]
then
	echo "[WARN]: libgcc module found. Users will need to install GCC on Windows (#46)"
fi

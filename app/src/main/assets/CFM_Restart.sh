
touch "$2"/../run/cmdRunning
"$2"/clash.service -k
"$2"/clash.service -s
rm -rf "$2"/../run/cmdRunning
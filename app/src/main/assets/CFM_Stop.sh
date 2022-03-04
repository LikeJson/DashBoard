if [[ "$(magisk -v | grep -i lite)" != "" ]]
then
  MAGISK_PATH="/data/adb/lite_modules"
else
  MAGISK_PATH="/data/adb/modules"
fi

touch "$1"/../run/cmdRunning
"$1"/clash.service -k && "$1"/clash.tproxy -k
rm -rf "$1"/../run/cmdRunning
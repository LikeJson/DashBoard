if [[ "$(magisk -v | grep -i lite)" != "" ]]
then
  MAGISK_PATH="/data/adb/lite_modules"
else
  MAGISK_PATH="/data/adb/modules"
fi

touch "$1"/../run/cmdRunning
"$1"/clash.service -s && "$1"/clash.tproxy -s
rm -rf "$1"/../run/cmdRunning
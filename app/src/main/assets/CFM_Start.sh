if [[ "$(magisk -v | grep -i lite)" != "" ]]
then
  MAGISK_PATH="/data/adb/lite_modules"
else
  MAGISK_PATH="/data/adb/modules"
fi

touch "$2"/../run/cmdRunning
"$2"/clash.service -s && "$2"/clash.tproxy -s
rm -rf "$2"/../run/cmdRunning
if [[ "$(magisk -v | grep -i lite)" != "" ]]
then
  MAGISK_PATH="/data/adb/lite_modules"
else
  MAGISK_PATH="/data/adb/modules"
fi

if [ "$1" == "false" ] && [ -f ${MAGISK_PATH}/Clash_For_Magisk/disable ]; then
  rm -f ${MAGISK_PATH}/Clash_For_Magisk/disable
else
  touch "$2"/../run/cmdRunning
  "$2"/clash.service -s && "$2"/clash.tproxy -s
  rm -rf "$2"/../run/cmdRunning
fi
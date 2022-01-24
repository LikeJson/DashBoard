if [[ "$(magisk -v | grep -i lite)" != "" ]]
then
  MAGISK_PATH="/data/adb/lite_modules"
else
  MAGISK_PATH="/data/adb/modules"
fi

if [ "$1" == "false" ] ; then
  touch ${MAGISK_PATH}/Clash_For_Magisk/disable
else
  "$2"/clash.service -k && "$2"/clash.tproxy -k
fi
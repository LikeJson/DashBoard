if [[ "$(magisk -v | grep -i lite)" != "" ]]
then
  MAGISK_PATH="/data/adb/lite_modules"
else
  MAGISK_PATH="/data/adb/modules"
fi

if [ "$1" == "false" ] ; then
  rm -f ${MAGISK_PATH}/Clash_For_Magisk/disable
else
  "$2"/clash.service -s && "$2"/clash.tproxy -s
fi
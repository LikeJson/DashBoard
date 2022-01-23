if [[ "$(magisk -v | grep -i lite)" != "" ]]
then
  MAGISK_PATH="/data/adb/lite_modules"
else
  MAGISK_PATH="/data/adb/modules"
fi

if [ "$1" == "" ] ; then
  rm -f ${MAGISK_PATH}/Clash_For_Magisk/disable
else
  "$1"/clash.service -s && "$1"/clash.tproxy -s
fi
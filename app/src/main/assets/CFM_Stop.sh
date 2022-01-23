if [[ "$(magisk -v | grep -i lite)" != "" ]]
then
  MAGISK_PATH="/data/adb/lite_modules"
else
  MAGISK_PATH="/data/adb/modules"
fi

if [ "$1" == "" ] ; then
  touch ${MAGISK_PATH}/Clash_For_Magisk/disable
else
  "$1"/clash.service -k && "$1"/clash.tproxy -k
fi
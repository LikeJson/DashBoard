if [[ "$(magisk -v | grep -i lite)" != "" ]]
then
  MAGISK_PATH="/data/adb/lite_modules"
else
  MAGISK_PATH="/data/adb/modules"
fi

/data/adb/modules/Clash_For_Magisk/scripts/clash.service -s && /data/adb/modules/Clash_For_Magisk/scripts/clash.tproxy -

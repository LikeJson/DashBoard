if [[ "$(magisk -v | grep -i lite)" != "" ]]
then
  MAGISK_PATH="/data/adb/lite_modules"
else
  MAGISK_PATH="/data/adb/modules"
fi

disABpath=${MAGISK_PATH}/Clash_For_Magisk/disable
touch ${disABpath}
scripts_dir="/data/adb/modules/Clash_For_Magisk/scripts"

service_path="${scripts_dir}/clash.service"
tproxy_path="${scripts_dir}/clash.tproxy"

${service_path} -s && ${tproxy_path} -s
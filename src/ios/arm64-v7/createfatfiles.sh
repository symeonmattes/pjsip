#!/usr/bin/env bash

files=("libg7221codec" "libpjmedia-codec" "libpjsua" "libgsmcodec" "libpjmedia-videodev" "libpjsua2" "libilbccodec" "libpjnath" "libresample" "libpj" "libpjsdp" "libspeex" "libpjlib-util" "libpjsip" "libsrtp" "libpjmedia" "libpjsip-simple" "libyuv" "libpjmedia-audiodev" "libpjsip-ua")

tLen=${#files[@]}

for (( i=0; i<${tLen}; i++ ));
do
  
  output=${files[$i]}".a"
  arm64=${files[$i]}"-arm64.a"
  armv7=${files[$i]}"-armv7.a"

  lipo -arch armv7 ../armv7/$armv7 -arch arm64 ../arm64/$arm64 -create -output $output
done

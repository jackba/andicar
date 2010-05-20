##! /bin/sh
. ./myEnv.sh
rm *.apk
ant release
cp ./dist/AndiCar.apk ./AndiCarUnsigned.apk
$JAVA_HOME/bin/jarsigner -verbose -keystore ../<keystorefile> AndiCarUnsigned.apk <keystore alias>
$ANDROID_SDK_HOME/tools/zipalign -v 4 AndiCarUnsigned.apk AndiCar.apk


 

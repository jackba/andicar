##! /bin/sh
rm *.apk
ant release
cp ./dist/AndiCar.apk ./AndiCarUnsigned.apk
jarsigner -verbose -keystore ../<keystorefile> AndiCarUnsigned.apk <keystore password>
<android-sdk>/tools/zipalign -v 4 AndiCarUnsigned.apk AndiCar.apk


 

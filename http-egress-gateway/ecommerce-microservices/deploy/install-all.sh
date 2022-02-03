#!/bin/bash

CWD=`pwd`

cd $CWD/../catalog-service
mvn clean install -DskipTests=true

cd $CWD/../payment-service
mvn clean install -DskipTests=true

cd $CWD/../order-service
mvn clean install -DskipTests=true

cd $CWD/../payment-history-service
mvn clean install -DskipTests=true

cd $CWD/../catalog-ui
mvn clean install -DskipTests=true

cd $CWD


exit 0
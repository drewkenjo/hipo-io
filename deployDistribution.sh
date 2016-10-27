#-------------------------------------------------------------------------------------------------
# Script is exporting existing Jar files to repository
#-------------------------------------------------------------------------------------------------
#  JEVIO
REPO="/Users/gavalian/Work/MavenRepo"

mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file  -Dfile=target/hipo-2.0.jar \
    -DgroupId=org.hep.hipo \
    -DartifactId=hipo \
    -Dversion=2.0-SNAPSHOT \
    -Dpackaging=jar \
    -DlocalRepositoryPath=$REPO

scp -r $REPO/org/hep/hipo/hipo clas12@jlabl1:/group/clas/www/clasweb/html/clas12maven/org/hep/hipo/.

#!/usr/bin/env groovy
@Library('customFagg') _
def notif = new fagg.notif()

properties([
    pipelineTriggers([pollSCM('''H/2 * * *  1-5''')])
])

node {

    env.JAVA_HOME="${tool 'jdk11'}"
    env.PATH="${env.JAVA_HOME}/bin:${env.PATH}"
    sh 'java -version'

	try {

		stage('checkout') {
			checkout scm
		}

		def pomProperties = readMavenPom file: 'pom.xml'
        echo "Will build for ${pomProperties.version}"
        echo "is it a SNAPSHOT buid : ${("${pomProperties.version}" ==~ /(.*)SNAPSHOT(.*)/)}"

		if("${pomProperties.version}" ==~ /(.*)SNAPSHOT(.*)/){

			stage('check java') {
				sh "java -version"
			}

			stage('clean') {
				sh "chmod +x mvnw"
				sh "./mvnw -Dhttps.proxyHost=proxy -Dhttps.proxyPort=8080 clean"
			}

			stage('backend tests') {
				try {
					sh "./mvnw -Dhttps.proxyHost=proxy -Dhttps.proxyPort=8080 verify"
				} catch (err) {
					throw err
				} finally {
					junit '**/target/test-results/**/TEST-*.xml'
				}
			}

			stage('packaging') {
				sh "./mvnw -Dhttps.proxyHost=proxy -Dhttps.proxyPort=8080 package -Pprod -DskipTests"
				archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
			}

			stage('Deploy to nexus Snapshots'){
				sh "./mvnw deploy:deploy-file -DgroupId=${pomProperties.groupId} -DartifactId=${pomProperties.artifactId} -Dversion=${pomProperties.version} -Dpackaging=${pomProperties.packaging} -Dfile=target/${pomProperties.artifactId}-${pomProperties.version}.${pomProperties.packaging} -DrepositoryId=NexusSnapshot -Durl=http://nexus.net/content/repositories/snapshots/"
			}
		}
	} catch (e) {
		notif.notifyFailedLib("#plato-team")
		throw e
   }
}

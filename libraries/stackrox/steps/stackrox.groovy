void call(){
    stage("Image Scan"){
        sh """
        roxctl -e ${centralStackrox_url} image scan --image ${ImageName}:${tag} --insecure-skip-tls-verify > sha.json
        roxctl -e ${centralStackrox_url} image scan --image ${ImageName}:${tag} --insecure-skip-tls-verify --output json > scan.json
        roxctl -e ${centralStackrox_url} image scan --image ${ImageName}:${tag} --insecure-skip-tls-verify --output table > scan.html
        """
        archiveArtifacts allowEmptyArchive: true, artifacts: "scan.html"
        def jsonContent = readFile('scan.json')
        def criticalCount = sh(script: "jq -r '.result.summary.CRITICAL' scan.json", returnStdout: true).trim().toInteger()
        def link = sh(script: "jq -r '.id' sha.json", returnStdout: true).trim()
        if (criticalCount > 3) {
            currentBuild.result = 'FAILURE'
            echo "Total Critical Severity: ${criticalCount} And scan completed with result: ${currentBuild.result}"
            echo "Image Link: https://${centralStackrox_url}/main/vulnerability-management/image/${link}"
            // error "Aborting the pipeline due to critical severity found"
        } else {
            currentBuild.result = 'SUCCESS'
            echo "Total Critical Severity: ${criticalCount} And scan completed with result: ${currentBuild.result}"
            echo "Image Link: https://${centralStackrox_url}/main/vulnerability-management/image/${link}"
        }
    }
}     

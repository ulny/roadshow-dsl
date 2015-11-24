import jenkins.*
import jenkins.model.*

/*
 * Use this function to enable must have-defaults for your jobs
 * LogRotator - make sure that you are cleaning up old logs
 * Timeout - kill job in case if it takes to long
 *           This will indicate a coming problem for you and unclock
 *           pipeline in case of hanging jobs
 * Timestamps - just nice to have have feature. Adds time stamps
 *              to console output
 *
 */
def addDefaultParameters(def context, buildsToKeep=50, artifactsToKeep=10, timeoutVal=30) {
    // Add timestamps and timeouts
    context.wrappers {
        timestamps()
        timeout {
            absolute(timeoutVal)
        }
    }
    // Set log rotator
    context.logRotator {
        numToKeep(buildsToKeep)
        artifactNumToKeep(artifactsToKeep)
    }
}

/*
 * Use this function to setup git clone to your repo
 * repoURL - URL for repository to clone
 * branch - branch name to checkout
 *
 */
def addGitSCM(def context, repoURL, branchName='master', credentialsId='jenkins') {
    context.scm {
        git{
            remote {
                name('origin')
                url(repoURL)
                credentials(credentialsId)
            }
          branch(branchName)
          // Make sure that repository is clean and we have
          // no leftovers from the previous builds or unsuccessful checkouts
          wipeOutWorkspace()
        }
    }
}

/*
 * Create a view
 */
listView("${GITHUB_USER}") {
    description('All jobs for GitHub user ${GITHUB_USER}')
    jobs {
        regex(/${GITHUB_USER}.+/)
    }
    columns {
        name()
        status()
        weather()
        lastDuration()
        buildButton()
    }
}

/*
 * Verify that we can build war, run unit tests and measure code coverage
 */
job("${GITHUB_USER}.roadshow.generated.build") {
    // Set default parameters
    addDefaultParameters(delegate)
    // Add Git SCM
    addGitSCM(delegate, "git@github.com:${GITHUB_USER}/roadshow.git")
    // Set trigger to poll SCM every minute
    triggers {
        scm('* * * * *')
    }
    // Actual build steps
    steps {
        // Build war file, run tests and measure coverage
        shell('./gradlew clean war jenkinstest jacoco')
        // Just for fun
        shell("echo 'Hello, world!!'")
    }
    // Post build steps
    publishers {
        // Collect code coverage report
        jacocoCodeCoverage()
        // Collect unit test results
        archiveJunit('build/test-results/*.xml')
        // Collect compilation warnings
        warnings(['Java Compiler (javac)'])
        // Trigger downstream job
        downstream("${GITHUB_USER}.roadshow.generated.staticanalysis",
                   'SUCCESS')
    }
}

/*
 * Run static analysis and post results
 */
job("${GITHUB_USER}.roadshow.generated.staticanalysis") {
    // Set default parameters
    addDefaultParameters(delegate)
    // Add Git SCM
    addGitSCM(delegate, "git@github.com:${GITHUB_USER}/roadshow.git")
    // Actual build steps
    steps {
        // Run static code analysis
        shell('./gradlew staticanalysis')
    }
    // Post-build steps
    publishers {
        // Collect check style report
        checkstyle('build/reports/checkstyle/*.xml')
        // Collect PMD report
        pmd('build/reports/pmd/*.xml')
        // Collect tasks statistics
        tasks('**/*', '', 'FIXME', 'TODO', 'LOW', true)
    }
}


/*
 * Run static analysis and post results
 */
job("${GITHUB_USER}.roadshow.generated.test") {
    // Set default parameters
    addDefaultParameters(delegate)
    // Add Git SCM
    addGitSCM(delegate, "git@github.com:${GITHUB_USER}/roadshow.git")
    // Actual build steps
    steps {
        // Run static code analysis
        shell('export GITHUB_USER=whatever \
                ./build.sh
')
    }
    // Post-build steps
    publishers {
        // Collect check style report
        checkstyle('build/reports/checkstyle/*.xml')
        // Collect PMD report
        pmd('build/reports/pmd/*.xml')
        // Collect tasks statistics
        tasks('**/*', '', 'FIXME', 'TODO', 'LOW', true)
    }
}

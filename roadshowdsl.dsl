job("${GITHUB_USER}.roadshow.generated.build") {
    scm {
      git{
        remote {
          name('origin')
          url("git@github.com:${GITHUB_USER}/roadshow.git")
        }
        branch("master")
      }
    }
    triggers {
        scm('* * * * *')
    }
    steps {
        gradle('clean war jenkinstest jacoco')
      	shell("echo 'Hello, world!!'")
    }
  	publishers {
      	jacocoCodeCoverage()
      	archiveJunit('build/test-results/*.xml')
      	warnings(['Java Compiler (javac)'])
    	downstream("${GITHUB_USER}.roadshow.generated.staticanalysis", 'SUCCESS')
    }
}

job("${GITHUB_USER}.roadshow.generated.staticanalysis") {
    scm {
        git{
          remote {
            name('origin')
            url("git@github.com:${GITHUB_USER}/roadshow.git")
          }
          branch("master")
        }
    }
    triggers {
        scm('* * * * *')
    }
    steps {
        gradle('clean staticanalysis')
    }
  	publishers {
      checkstyle('build/reports/checkstyle/*.xml')	
      pmd('build/reports/pmd/*.xml')
      tasks('**/*', '', 'FIXME', 'TODO', 'LOW', true)
  	}
}

job("meekrosoft.roadshow.generated.build") {
    scm {
      git{
        remote {
          name('origin')
          url("git@github.com:meekrosoft/roadshow.git")
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

job("meekrosoft.roadshow.generated.staticanalysis") {
    scm {
        git{
          remote {
            name('origin')
            url("git@github.com:meekrosoft/roadshow.git")
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

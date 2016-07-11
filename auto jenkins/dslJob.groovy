job('csharp_test') {
    steps{
        shell([
            'cd /home/jenkins',
            'sudo xbuild docpacker.sln'
        ].join('\n')

        )
    }
}

job('e2e_test'){
    steps{
        shell([
            '#!/bin/bash',
            'export DISPLAY=:0',
            'sudo Xvfb :0 -ac -screen 0 1920x1080x24 &',
            'webdriver-manager start &',
            'gulp e2e'
        ].join('\n')

        )
    }
}
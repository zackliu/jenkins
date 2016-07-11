import java.net.URL
import hudson.cli.*
  
def url = new URL('http://localhost:8080');
def acli = new CLI(url);
acli.execute('install-plugin', 'xvfb');

FROM selenium/standalone-chrome

USER root

RUN apt-get update && apt-get install -y sudo openjdk-8-jre openssh-server git

RUN apt-get update && apt-get install -y npm supervisor

RUN npm install -g protractor mocha jasmine gulp

RUN ln -s /usr/bin/nodejs /usr/bin/node

RUN useradd jenkins
RUN mkdir -p /home/jenkins
RUN chown -R jenkins /home/jenkins
RUN chgrp -R jenkins /home/jenkins
RUN echo "jenkins:jenkins" | chpasswd
RUN echo "root:root" | chpasswd
RUN mkdir -p /var/run/sshd
RUN echo "seluser ALL=(ALL) NOPASSWD: ALL" >> etc/sudoers

RUN wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb && \
    dpkg --unpack google-chrome-stable_current_amd64.deb && \
    apt-get install -f -y && \
    mkdir /jenkins

#COPY entry_point.sh /opt/bin/entry_point.sh
COPY supervisord.conf /etc/supervisord.conf

EXPOSE 22
#CMD ["/usr/bin/supervisord"] 

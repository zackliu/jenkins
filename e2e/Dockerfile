FROM node:argon

#install protractor mocha jasmine xvfb wget openjdk
RUN apt-get update && \
    npm install -g protractor mocha jasmine && \ 
    webdriver-manager update && \
    apt-get update && apt-get install -y xvfb && \
    apt-get update && apt-get install -y wget openjdk-7-jre

#install chrome
RUN wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb && \
    dpkg --unpack google-chrome-stable_current_amd64.deb && \
    apt-get install -f -y && \
 #   agt-get clean && \
    mkdir /jenkins

RUN apt-get update && apt-get install -y supervisor
RUN npm install -g gulp
RUN apt-get install -y sudo && \
    apt-get install -y openssh-server

RUN /usr/bin/ssh-keygen -A
RUN sed -ri 's/^session\s+required\s+pam_loginuid.so$/session optional pam_loginuid.so/' /etc/pam.d/sshd




#COPY spec.js /protractor/



RUN useradd jenkins
RUN mkdir -p /home/jenkins
RUN chown -R jenkins /home/jenkins
RUN chgrp -R jenkins /home/jenkins
RUN echo "jenkins:jenkins" | chpasswd
RUN echo "root:root" | chpasswd
RUN mkdir -p /var/run/sshd

#RUN echo "jenkins  ALL=(ALL)  ALL" >> etc/sudoers
RUN echo "jenkins ALL=(ALL) NOPASSWD: ALL" >> etc/sudoers
#RUN chmod 740 etc/sudoers

COPY Docs.E2E /home/jenkins/
RUN cd /home/jenkins && npm install

COPY /files/resolv.conf /etc/resolv.conf
COPY supervisord.conf /etc/supervisord.conf
COPY exec.sh /home/jenkins/

RUN apt-get -y upgrade

EXPOSE 4444 
EXPOSE 80
EXPOSE 22
ENV DISPLAY=:0
ENV DBUS_SESSION_BUS_ADDRESS=/dev/null

#VOLUME /dev/shm
#RUN export DISPLAY=:0
#CMD ["/usr/bin/supervisord"] 
#USER protractor
#CMD ["/bin/bash"]
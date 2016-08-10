FROM node:argon

RUN apt-get update && apt-get install -y openssh-server && \
    apt-get update && apt-get install -y openjdk-7-jre && \
    apt-get update && apt-get install -y sudo && \
    apt-get update && npm install request

RUN /usr/bin/ssh-keygen -A

RUN sed -ri 's/^session\s+required\s+pam_loginuid.so$/session optional pam_loginuid.so/' /etc/pam.d/sshd

RUN echo "root:password" | chpasswd
RUN useradd jenkins
RUN mkdir -p /home/jenkins
RUN chown -R jenkins /home/jenkins
RUN chgrp -R jenkins /home/jenkins
RUN echo "jenkins:jenkins" | chpasswd
RUN mkdir -p /var/run/sshd

RUN echo "jenkins ALL=(ALL) NOPASSWD: ALL" >> etc/sudoers

#COPY /files/resolv.conf /etc/resolv.conf

USER jenkins

COPY . /home/jenkins/

WORKDIR /home/jenkins/

USER root

EXPOSE 22

#CMD ["node", "/home/jenkins/createjobs"]
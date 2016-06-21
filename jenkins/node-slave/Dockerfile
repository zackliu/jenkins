FROM node:argon

RUN apt-get update && apt-get install -y openssh-server && \
    apt-get update && apt-get install -y openjdk-7-jre && \
    apt-get update && apt-get install -y sudo

RUN /usr/bin/ssh-keygen -A

RUN sed -ri 's/^session\s+required\s+pam_loginuid.so$/session optional pam_loginuid.so/' /etc/pam.d/sshd

#RUN useradd jenkins -m -s /bin/bash

#RUN mkdir /home/jenkins/.ssh
#COPY /files/authorized_keys /home/jenkins/.ssh/authorized_keys
#RUN chown -R jenkins /home/jenkins
#RUN chgrp -R jenkins /home/jenkins
#RUN chmod 600 /home/jenkins/.ssh/authorized_keys
#RUN chmod 700 /home/jenkins/.ssh
#RUN mkdir /var/run/sshd
#RUN chmod 755 /var/run/sshd

RUN echo "root:password" | chpasswd
RUN useradd jenkins
RUN mkdir -p /home/jenkins
RUN chown -R jenkins /home/jenkins
RUN chgrp -R jenkins /home/jenkins
RUN echo "jenkins:jenkins" | chpasswd
RUN mkdir -p /var/run/sshd



RUN echo "jenkins  ALL=(ALL)  ALL" >> etc/sudoers

# Set Name Servers
COPY /files/resolv.conf /etc/resolv.conf

#VOLUME /var/log/

# Expose SSH port and run SSHD
EXPOSE 22
CMD ["/usr/sbin/sshd","-D"]
#CMD ["/bin/bash"]
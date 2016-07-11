FROM mono:latest

RUN apt-get update && apt-get install -y openssh-server && \
    apt-get update && apt-get install -y openjdk-7-jre && \
    apt-get update && apt-get install -y sudo

RUN /usr/bin/ssh-keygen -A

RUN sed -ri 's/^session\s+required\s+pam_loginuid.so$/session optional pam_loginuid.so/' /etc/pam.d/sshd


RUN useradd jenkins
RUN mkdir -p /home/jenkins
RUN chown -R jenkins /home/jenkins
RUN chgrp -R jenkins /home/jenkins
RUN echo "jenkins:jenkins" | chpasswd
RUN echo "root:root" | chpasswd
RUN mkdir -p /var/run/sshd

RUN echo "jenkins ALL=(ALL) NOPASSWD: ALL" >> etc/sudoers

COPY /files/resolv.conf /etc/resolv.conf

EXPOSE 22


COPY . /home/jenkins

#CMD ["xbuild", "/var/csharp/docpacker.sln"]
CMD ["/bin/bash"]
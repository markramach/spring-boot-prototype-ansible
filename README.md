##spring-boot-prototype-ansible

Turning your Continuous Integration process into a Continuous Deployment Pipeline is easier than you think. Using a tool like [Ansible](http://www.ansible.com/), we can break the deployment process into simple repeatable roles that can handle extremely complex deployment scenarios. We can also integrate this deployment process with Maven to make the entire process of committing, building, testing and deploying into one streamline delivery pipeline.

Ansible is a powerful collection of open source modules that allow you to remotely manage the state of your servers. Ansible is agentless. There is no additional software that needs to be installed on a target host(s).

This prototype project, much like previous ones, will be constructing a simple [Spring Boot](http://projects.spring.io/spring-boot/) REST service packaged in a [Docker](https://www.docker.com/) container. However, in this case we will leverage a [Maven Ansible Plugin](https://github.com/tmullender/ansible-maven-plugin) to automate the installation of our Docker container on a host.

## Pre-Requisites

### Docker Installation

This project requires the installation of Docker. See the Docker [installation](https://docs.docker.com/installation/#installation) guide to get up and running.

Once you have Docker installed, you will need to authenticate with the Docker Registry. To complete this step you will need to create an account [here](https://hub.docker.com/account/signup/)

Once you have created an account you should run the following command:

	docker login
	
You will be prompted for all authentication details.

### Ansible Installation

This project requires the installation of Ansible. See the Ansible [installation](http://docs.ansible.com/intro_installation.html) guide to get up and running.

## Quick Start

The easiest way to get started with the prototype is to fork, clone or download this repository.

	git clone https://github.com/markramach/spring-boot-prototype-ansible.git
	
This prototype project consists of a simple spring boot REST service. To build the project simply run the following command:

	mvn clean install
	
At this point you will have a complete runnable Spring Boot jar file in the project's target directory. 

### Build the Docker Image
	
This project has the Maven Docker Plugin pre-configured in the pom.xml file. To build the docker image simply run the following command:

	mvn docker:build
	
### Tag the Docker Image

Now that the image has been created, we should tag the image to indicate the version of the Spring Boot artifact that it contains. 

	mvn docker:tag
	
Running the `docker images` command should now produce the following output.

	Marks-MacBook-Pro:spring-boot-prototype-ansible mramach$ docker images
	REPOSITORY                                                  TAG                 IMAGE ID            CREATED              VIRTUAL SIZE
	markramach/spring-boot-prototype-ansible                    latest              350c05d529c3        6 minutes ago       830.4 MB
	markramach/spring-boot-prototype-ansible                    1.0.0-SNAPSHOT      350c05d529c3        6 minutes ago       830.4 MB

### Push the Docker Image

To make the image available outside of your local system, you need to push the image to a valid Docker Registry.

To push the new images to the repository run the following command:

	mvn docker:push

At this point you should be able to browse [Docker Registry](https://registry.hub.docker.com/u/markramach/spring-boot-prototype-ansible/tags/manage/) and see 2 tag files.

### Define The Deployment Playbook

This prototype project contains a sample [playbook](https://github.com/markramach/spring-boot-prototype-ansible/blob/master/src/main/ansible/deploy.yml) to deploy the Docker image we just created. This playbook can deploy the image to any number of hosts that we define in the [hosts](https://github.com/markramach/spring-boot-prototype-ansible/blob/master/src/main/ansible/hosts) file. For the purposes of this project we are simply going to execute the playbook against the localhost. However, you can define any number of remote hosts and connection details for those hosts using the inventory file. For more information see the Ansible [Invetory](http://docs.ansible.com/intro_inventory.html) documentation.

	---
	- hosts: all 
	  tasks:
	   - name: Install docker-py
	     pip:
	       name: 'https://pypi.python.org/packages/source/d/docker-py/docker-py-1.1.0.tar.gz'
    	   state: present
	   - name: Start Docker Container
	     docker:
	       name: spring-boot-prototype-ansible
	       image: "{{ dockerImagePrefix }}/{{ projectArtifactId }}:{{ projectVersion }}"
	       pull: always
	       insecure_registry: true
	       state: started
	       ports:
	         - "8080:8080"
	       expose:
	         - 8080

The deploy.yml file included in the prototype project simply instructs Ansible to ensure that there our container is up and running on the target host. It indicates that the a pull attempt will always be made. Additionally, port 8080 on the container will be bound to port 8080 on the target host.

### Define The Target Host

The prototype contains a hosts file at src/main/ansible/hosts. It contains a single line

	localhost ansible_connection=local
	
The hosts file indicates that the only target host for our operation is the localhost. To define a remote host you could modify the file like the following:

	10.20.30.40 ansible_connection=ssh ansible_ssh_user=root

### Plugin Configuration

	<plugin>
		<groupId>co.escapeideas.maven</groupId>
		<artifactId>ansible-maven-plugin</artifactId>
		<version>1.2.0</version>
		<configuration>
			<inventory>${basedir}/src/main/ansible/hosts</inventory>
			<playbook>${basedir}/src/main/ansible/deploy.yml</playbook>
			<extraVars>
				<docker.registry>dockerRegistry=${docker.registry}</docker.registry>
				<docker.image.prefix>dockerImagePrefix=${docker.image.prefix}</docker.image.prefix>
				<project.artifactId>projectArtifactId=${project.artifactId}</project.artifactId>
				<project.version>projectVersion=${project.version}</project.version>
			</extraVars>
		</configuration>
	</plugin>
            
As you can see, we have defined the Ansible inventory and playbook that will be used during the plugin execution. Also, we have defined several variables that will be made available to the playbook. These allow the docker container configuration to be dynamic during the build process.

### Run The Playbook

To run the playbook execute the following command:

	mvn -X ansible:playbook

You should see the following output:

	[INFO] Working directory: /Users/mramach/projects_001/spring-boot-prototype-ansible/target
	[DEBUG] 
	[DEBUG] PLAY [all] ******************************************************************** 
	[DEBUG] 
	[DEBUG] GATHERING FACTS *************************************************************** 
	[DEBUG] ok: [localhost]
	[DEBUG] 
	[DEBUG] TASK: [Install docker-py] ***************************************************** 
	[DEBUG] ok: [localhost]
	[DEBUG] 
	[DEBUG] TASK: [Start Docker Container] ************************************************ 
	[DEBUG] changed: [localhost]
	[DEBUG] 
	[DEBUG] PLAY RECAP ******************************************************************** 
	[DEBUG] localhost                  : ok=3    changed=1    unreachable=0    failed=0   
	[DEBUG] 
	[INFO] ------------------------------------------------------------------------
	[INFO] BUILD SUCCESS
	[INFO] ------------------------------------------------------------------------
	[INFO] Total time: 4.876 s
	[INFO] Finished at: 2015-07-01T15:03:16-05:00
	[INFO] Final Memory: 10M/309M
	[INFO] ------------------------------------------------------------------------

You can verify that the docker container is up and running on the localhost:

	Marks-MacBook-Pro:spring-boot-prototype-ansible mramach$ docker ps -a
	CONTAINER ID        IMAGE                                                                      COMMAND                CREATED             STATUS                     PORTS                    NAMES
	d721bb041191        markramach/spring-boot-prototype-ansible:1.0.0-SNAPSHOT                    "java -jar /spring-b   4 seconds ago       Up 3 seconds               0.0.0.0:8080->8080/tcp   spring-boot-prototype-ansible  

Once the Spring Boot application is up and running you can execute a HTTP `GET` request on the sample resource. Depending on your docker setup localhost may not work. If you are running on OS X, you may need to use the IP address of the boot2docker VM.

	http://localhost:8080/say-hello?name=world
	
You should see the following response:

	{"hello":"world"}
	
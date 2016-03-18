# Wildfly-Swarm Hystrix and Turbine Example
Wildfly-Swarm Example using Hystrix as the circuit-breaker. This example is composed of:
* __Employee REST Service:__ a simple JAX-RS service that returns the list of employees. This service randomly generates error and timed-out responses.
* __Payroll REST Service:__ a simple JAX-RS service that invokes Employee service using Hystrix
* __Turbine:__ one of Netflix components for aggregating streams of json data
* __Hystrix Dashboard:__ a dashboard for visualizing aggregated data streams 

# Build Docker Images

	$ mvn clean install docker:build
	
# Run Local

Start the Employee service:

	$ cd employee-service
	$ mvn wildfly-swarm:run


Start the Payroll service:

	$ cd payroll-service
	$ mvn -Dswarm.port.offset=1 -Dapi.employee.endpoint=http://localhost:8080 wildfly-swarm:run
	
Generate some load on the Payroll endpoint

	$ ab -n 10 http://localhost:8081/payroll

  The results will be similar to the following:

	Percentage of the requests served within a certain time (ms)
	  50%    397
	  66%    463
	  75%   1009
	  80%   1009
	  90%   1010
	  95%   1010
	  98%   1010
	  99%   1010
	 100%   1010 (longest request)

Notice the response time for various fractions of requests. Now run a larger number of requests and observe that the circuit-breaker closes the circuit after a certain number of failure and timeouts:

	$ ab -n 100 http://localhost:8081/payroll

	Percentage of the requests served within a certain time (ms)
	  50%      2
	  66%      2
	  75%      3
	  80%      4
	  90%     11
	  95%    370
	  98%   1006
	  99%   1007
	 100%   1007 (longest request)
	 
# Run on OpenShift

To run this example on OpenShift, first you should build the Docker images for all components and have them available locally. To build the images, you can take advantage of [docker-maven-plugin](https://github.com/fabric8io/docker-maven-plugin) to build a Docker image for each project. Run the following in the root project:

	$ mvn install docker:build  

Now that the images are ready, import the OpenShift template wildfly-swarm-microservices-example.json and create an application based on the imported template:
	
	$ oc new-project wildfly-swarm --display-name="Wildfly Swarm Microservices"
	$ oc create -f wildfly-swarm-microservices-example.json
	$ oc new-app wildfly-swarm-microservices-example
	
A number of objects for the services and components are created within the project:

	--> Deploying template wildfly-swarm-microservices-example for "wildfly-swarm-microservices-example"
	--> Creating resources ...
	    Service "employee-app" created
	    Service "payroll-app" created
	    Service "turbine" created
	    Service "hystrix-dashboard" created
	    Route "employee-app" created
	    Route "payroll-app" created
	    Route "turbine" created
	    Route "hystrix-dashboard" created
	    DeploymentConfig "employee-app" created
	    DeploymentConfig "payroll-app" created
	    DeploymentConfig "turbine" created
	    DeploymentConfig "hystrix-dashboard" created
	--> Success
	    Run 'oc status' to view your app.
	
![Hystrix Dashboard](https://raw.githubusercontent.com/siamaksade/wildfly-swarm-hystrix-example/master/images/containers.png)


Netflix Turbine makes API calls to OpenShift in order to discover data endpoints for aggregation. Therefore the service account in your project needs to have read access to cluster. Run the following command to give sufficient privileges to the service account:
	
	$ oc login -u system:admin
	$ oadm policy add-cluster-role-to-user cluster-reader system:serviceaccount:wildfly-swarm:default
	
You can test the REST services using _curl_ command:

	$ curl http://employee-app-wildfly-swarm.10.1.2.2.xip.io/employees
	[{"id":1,"name":"John"},{"id":2,"name":"Sarah"},{"id":3,"name":"Matt"},{"id":4,"name":"Linda"}]
	
	$ curl http://payroll-app-wildfly-swarm.10.1.2.2.xip.io/payroll
	{"employee":{"id":1,"name":"John"},"salary":1500,"employeeName":"John"},{"employee":{"id":2,"name":"Sarah"},"salary":3000,"employeeName":"Sarah"},{"employee":{"id":3,"name":"Matt"},"salary":4500,"employeeName":"Matt"},{"employee":{"id":4,"name":"Linda"},"salary":6000,"employeeName":"Linda"}]
	
If you get an "Internal Server Error" for some of the Employee requests, it's by design! The service simulates a certain ration of errors and timeouts. 

# Hystrix Dashboard

Hystrix Dashboard shows the status of Hystrix commands being run and the circuit state (close or open) on each endpoint. Enter Turbine's stream url in Hystrix Dashboard and then click _Add Stream_ and _Monitor Streams_:

![Hystrix Dashboard](https://raw.githubusercontent.com/siamaksade/wildfly-swarm-hystrix-example/master/images/hystrix-dashboard.png)

![Hystrix Dashboard](https://raw.githubusercontent.com/siamaksade/wildfly-swarm-hystrix-example/master/images/hystrix-open.png)

Generate some load on the Payroll service and monitor the endpoints through Hystrix Dashboard:
	
	ab -n 100 http://payroll-app-wildfly-swarm.10.1.2.2.xip.io/payroll
	
![Hystrix Dashboard](https://raw.githubusercontent.com/siamaksade/wildfly-swarm-hystrix-example/master/images/hystrix-close.png)

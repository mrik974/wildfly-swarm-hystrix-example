# Wildfly-Swarm Hystrix Example
Wildfly-Swarm Example using Hystrix as the circuit-breaker. This example is composed of two REST services: Employee and Payroll. The Payroll service upon invocation makes a REST call to the Employee service using Hystrix. 

The Employee service responds randomly with latency and error for a fraction of requests.

# Build
mvn clean install

# Run
1. Run the Employee service
	
	cd employee-service
	mvn wildfly-swarm:run

2. Run the Payroll service
	
	cd payroll-service
	mvn -Dswarm.port.offset=1 -Dapi.employee.endpoint=http://localhost:8080 wildfly-swarm:run
	

# Use

1. Run a number of requests against the Employee endpoint

	ab -n 10 http://localhost:8080/employees

  The results will be similar to the following:

	Percentage of the requests served within a certain time (ms)
	  50%      3
	  66%      7
	  75%    203
	  80%   1268
	  90%   1481
	  95%   1481
	  98%   1481
	  99%   1481
	 100%   1481 (longest request)

  Note the percentage requests with latencies.

2. Run a number of requests against the Payroll endpoint

	ab -n 10 http://localhost:8081/payroll

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

  Run a larger number of requests and observe that the circuit-breaker closes the circuit after 
  a certain number of failure and timeouts:

	ab -n 100 http://localhost:8081/payroll

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

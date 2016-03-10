package org.wildfly.swarm.payroll.api;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.wildfly.swarm.payroll.model.Employee;
import org.wildfly.swarm.payroll.model.Payroll;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;

@ApplicationScoped
@Path("/payroll")
public class PayrollController {
	private static final String EMPLOYEES_ENDPOINT = System.getProperty("api.employee.endpoint", "http://localhost:8080");
	
	public PayrollController() {
		HystrixCommandProperties.Setter()
			.withCircuitBreakerRequestVolumeThreshold(10);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Payroll> findAll() {
		List<Employee> employees = new CommandFindEmployees().execute();
		List<Payroll> payroll = new ArrayList<>();
		for (Employee employee : employees) {
			payroll.add(new Payroll(employee, employee.getId() * 1500));
		}

		return payroll;
	}

	class CommandFindEmployees extends HystrixCommand<List<Employee>> {
		public CommandFindEmployees() {
			super(HystrixCommandGroupKey.Factory.asKey("EmployeesGroup"));
		}

		@Override
		protected List<Employee> run() {
			Builder request = ClientBuilder.newClient().target(EMPLOYEES_ENDPOINT + "/employees").request();
			return request.get(new GenericType<List<Employee>>(){});
		}
	}
}

package org.wildfly.swarm.payroll.api;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

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
	private static Logger LOG = Logger.getLogger(PayrollController.class.getName());
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
			String url = Utils.getEmployeeEndpoint("/employees");
			Builder request = ClientBuilder.newClient().target(url).request();
			try {
			 return request.get(new GenericType<List<Employee>>(){});
			} catch (Exception e) {
				LOG.severe("Failed to call Employee service at " + url + ": " + e.getMessage());
				throw e;
			}
		}
	}
}

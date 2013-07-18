package br.com.caelum.vraptor.interceptor;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import br.com.caelum.vraptor4.interceptor.DefaultInterceptorRegistry;
import br.com.caelum.vraptor4.interceptor.ExceptionHandlerInterceptor;
import br.com.caelum.vraptor4.interceptor.ExecuteMethodInterceptor;
import br.com.caelum.vraptor4.interceptor.Interceptor;

public class DefaultInterceptorRegistryTest {
	
	@Test
	public void shouldRegisterAllComponents() {
		DefaultInterceptorRegistry registry = new DefaultInterceptorRegistry();
		registry.register(ExecuteMethodInterceptor.class, ExceptionHandlerInterceptor.class);
		
		List<Class<? extends Interceptor>> expected = newArrayList();
		expected.add(ExecuteMethodInterceptor.class);
		expected.add(ExceptionHandlerInterceptor.class);
		
		Assert.assertEquals(expected, registry.all());
	}
}

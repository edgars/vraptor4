/***
 * Copyright (c) 2009 Caelum - www.caelum.com.br/opensource All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package br.com.caelum.vraptor.environment;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.util.NoSuchElementException;

import javax.servlet.ServletContext;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

public class DefaultEnvironmentTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	private DefaultEnvironment env;
	private ServletContext context;

	@Before
	public void setup() {
		context = Mockito.mock(ServletContext.class);
		env = new DefaultEnvironment(context);
	}

	@Test
	public void shouldUseTheCurrentEnvironmentFileIfFound() throws IOException {
		URL resource = env.getResource("/hibernate.cfg.xml");
		assertThat(resource, is(equalTo(DefaultEnvironment.class.getResource("/development/hibernate.cfg.xml"))));
	}

	@Test
	public void shouldLoadConfigurationInDefaultFileEnvironment() throws IOException {
		when(context.getInitParameter(DefaultEnvironment.ENVIRONMENT_PROPERTY)).thenReturn("production");
		
		assertThat(env.get("env_name"), is(equalTo("production")));
		assertEquals("only_in_default_file", env.get("only_in_default_file"));
	}

	@Test
	public void shouldUseTheDefaultFileIfEnvironmentIsNotFound() throws IOException {
		when(context.getInitParameter("br.com.caelum.vraptor.environment")).thenReturn("production");
		URL resource = env.getResource("/hibernate.cfg.xml");
		
		assertThat(resource, is(equalTo(DefaultEnvironment.class.getResource("/hibernate.cfg.xml"))));
		assertThat(env.get("env_name"), is(equalTo("production")));
	}

	@Test
	public void shouldUseFalseIfFeatureIsNotPresent() throws IOException {
		assertThat(env.supports("feature_that_doesnt_exists"), equalTo(false));
	}

	@Test
	public void shouldThrowExceptionIfKeyDoesNotExist() throws Exception {
		exception.expect(NoSuchElementException.class);

		env.get("key_that_doesnt_exist");
	}

	@Test
	public void shouldGetDefaultValueIfTheValueIsntSet() throws Exception {
		String value = env.get("inexistent", "fallback");
		assertEquals("fallback", value);
	}

	@Test
	public void shouldGetValueIfIsSetInProperties() throws Exception {
		String value = env.get("env_name", "fallback");
		assertEquals("development", value);
	}

	@Test
	public void shouldTrimValueWhenEvaluatingSupport() throws Exception {
		assertThat(env.supports("untrimmed_boolean"), is(true));
	}

	@Test
	public void shouldNotUseAnyPropertiesIfItDoesntExist() throws IOException {
		when(context.getInitParameter("br.com.caelum.vraptor.environment")).thenReturn("test");
		URL resource = env.getResource("/hibernate.cfg.xml");

		assertThat(resource, is(equalTo(DefaultEnvironment.class.getResource("/hibernate.cfg.xml"))));
		assertFalse(env.has("unexistant_key"));
	}
}
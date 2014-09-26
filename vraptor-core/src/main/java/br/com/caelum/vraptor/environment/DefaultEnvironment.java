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

import static br.com.caelum.vraptor.environment.EnvironmentType.DEVELOPMENT;
import static br.com.caelum.vraptor.environment.EnvironmentType.PRODUCTION;
import static br.com.caelum.vraptor.environment.EnvironmentType.TEST;
import static com.google.common.base.Objects.firstNonNull;
import static java.security.AccessController.doPrivileged;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.PrivilegedAction;
import java.util.NoSuchElementException;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A default {@link Environment} implementation which loads the environment file based on {@code VRAPTOR_ENV} system 
 * property or {@code br.com.caelum.vraptor.environment} property in the context init parameter.
 *
 * @author Alexandre Atoji
 * @author Andrew Kurauchi
 * @author Guilherme Silveira
 * @author Rodrigo Turini
 * @author OtÃ¡vio Garcia
 */
@ApplicationScoped
@Named("environment")
public class DefaultEnvironment implements Environment {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultEnvironment.class);
	public static final String ENVIRONMENT_PROPERTY = "br.com.caelum.vraptor.environment";
	public static final String BASE_ENVIRONMENT_FILE = "environment";

	private final ServletContext context;
	private Properties properties;
	private EnvironmentType environmentType;

	/**
	 * @deprecated CDI eyes only
	 */
	protected DefaultEnvironment() {
		this(null);
	}

	@Inject
	public DefaultEnvironment(ServletContext context) {
		this.context = context;
	}

	@PostConstruct
	protected void init() {
		properties = new Properties();
		environmentType = new EnvironmentType(findEnvironmentName(context));
		loadAndPut(BASE_ENVIRONMENT_FILE);
		loadAndPut(environmentType.getName());
	}

	private void loadAndPut(String environment) {
		try (InputStream resource = getClass().getResourceAsStream("/" + environment + ".properties")) {
			properties.load(resource);
		} catch (NullPointerException | IOException whenNotFound) {
			LOG.warn("Could not find the file '{}.properties' to load.", environment);
		}
	}

	private String findEnvironmentName(final ServletContext context) {
		return doPrivileged(new PrivilegedAction<String>() {
			@Override
			public String run() {
				String name = fromSystemEnv();
				if (name != null) {
					return name;
				}
		
				name = fromSystemProperty();
				if (name != null) {
					return name;
				}
		
				name = fromApplicationContext();
				return firstNonNull(name, "development");
			}
		});
	}

	/**
	 * Find environment name using {@code VRAPTOR_ENV} from system environment.
	 */
	private String fromSystemEnv() {
		return System.getenv("VRAPTOR_ENV");
	}

	/**
	 * Find environment name using {@code br.com.caelum.vraptor.environment} from system property. To define this
	 * you can start the application server using {@code -Dbr.com.caelum.vraptor.environment=DEVELOPMENT}.
	 */
	private String fromSystemProperty() {
		return System.getProperty(ENVIRONMENT_PROPERTY);
	}

	/**
	 * Find environment name using {@code br.com.caelum.vraptor.environment} from application context. You
	 * can define using web.xml, adding an init parameter {@code br.com.caelum.vraptor.environment=DEVELOPMENT}.
	 */
	private String fromApplicationContext() {
		return context.getInitParameter(ENVIRONMENT_PROPERTY);
	}

	@Override
	public boolean supports(String feature) {
		if (has(feature)) {
			return Boolean.parseBoolean(get(feature).trim());
		}
		return false;
	}

	@Override
	public boolean has(String key) {
		return properties.containsKey(key);
	}

	@Override
	public String get(String key) {
		if (!has(key)) {
			throw new NoSuchElementException(String.format("Key %s not found in environment %s", key, getName()));
		}
		return properties.getProperty(key);
	}

	@Override
	public String get(String key, String defaultValue) {
		if (has(key)) {
			return get(key);
		}
		return defaultValue;
	}

	@Override
	public void set(String key, String value) {
		properties.setProperty(key, value);
	}

	@Override
	public Iterable<String> getKeys() {
		return properties.stringPropertyNames();
	}

	@Override
	public boolean isProduction() {
		return PRODUCTION.equals(environmentType);
	}

	@Override
	public boolean isDevelopment() {
		return DEVELOPMENT.equals(environmentType);
	}

	@Override
	public boolean isTest() {
		return TEST.equals(environmentType);
	}

	@Override
	public URL getResource(String name) {
		URL resource = getClass().getResource("/" + getName() + name);
		if (resource != null) {
			return resource;
		}
		return getClass().getResource(name);
	}

	@Override
	public String getName() {
		return environmentType.getName();
	}
}
/***
 * Copyright (c) 2009 Caelum - www.caelum.com.br/opensource
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */

package br.com.caelum.vraptor.http.ognl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import br.com.caelum.vraptor4.http.ognl.EmptyElementsRemoval;

public class EmptyElementsRemovalTest {
	
	private EmptyElementsRemoval removal;
	
	@Before
	public void setup() {
		this.removal = new EmptyElementsRemoval();
	}
	
	@Test
	public void shouldRemoveNullElementsOutOfAnArrayList() {
		List<String> items = new ArrayList<String>();
		removal.add(items);
		items.add("value");
		items.add(null);
		items.add("value");
		removal.removeExtraElements();
		assertThat(items.size()	, is(equalTo(2)));
	}
	
	class Dog {
		private String names[];
		public void setNames(String names[]) {
			this.names = names;
		}
		public String[] getNames() {
			return names;
		}
	}
	
	@Test
	public void shouldRemoveNullElementsOutOfAnArray() throws SecurityException, NoSuchMethodException {
		Dog dog = new Dog();
		dog.names = new String[] {"first", null, "second", null};
		removal.add(dog.getNames(), Dog.class.getMethod("setNames", dog.names.getClass()), dog);
		removal.removeExtraElements();
		assertThat(dog.names.length, is(equalTo(2)));
	}
	
	@Test
	public void shouldIgnoreTheFirstArrayIfOverriden() throws SecurityException, NoSuchMethodException {
		Dog dog = new Dog();
		dog.names = new String[] {"first", null, "second", null, "third"};
		removal.add(dog.getNames(), Dog.class.getMethod("setNames", dog.names.getClass()), dog);
		dog.names = new String[] {"first", null, "second", null};
		removal.add(dog.getNames(), Dog.class.getMethod("setNames", dog.names.getClass()), dog);
		removal.removeExtraElements();
		assertThat(dog.names.length, is(equalTo(2)));
	}
	

	@Test
	public void shouldPruneTheFirstArrayIfTheSecondIsInADifferentInstance() throws SecurityException, NoSuchMethodException {
		Dog dog = new Dog();
		dog.names = new String[] {"first", null, "second", null, "third"};
		removal.add(dog.getNames(), Dog.class.getMethod("setNames", dog.names.getClass()), dog);
		Dog dog2 = new Dog();
		dog2.names = new String[] {"first", null, "second", null};
		removal.add(dog2.getNames(), Dog.class.getMethod("setNames", dog.names.getClass()), dog2);
		removal.removeExtraElements();
		assertThat(dog.names.length, is(equalTo(3)));
		assertThat(dog2.names.length, is(equalTo(2)));
	}
}

/*
 * Copyright 2005-2016 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.redhat.hotrodclient;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.swagger.Swagger2Feature;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller;
import org.infinispan.protostream.DescriptorParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.redhat.hotrodclient.serviceImpl.UserServiceImpl;

@SpringBootApplication
public class Application {

	
	private Logger logger = LoggerFactory.getLogger(getClass());


    @Autowired
	private Bus bus;

	@Value("${infinispan.server}")
	private String cacheServer;


	@Value("${infinispan.port}")
	private int cachePort;

	@Value("${infinispan.realm.user}")
	private String realmUser;
	
	@Value("${infinispan.realm.password}")
	private String realmPassword;
	
	@Value("${infinispan.server.name}")
	private String serverName;
	
	@Value("${infinispan.realm.name}")
	private String realm;
	public static void main(String[] args) {

		ApplicationContext applicationContext = SpringApplication.run(Application.class, args);

		for (String name : applicationContext.getBeanDefinitionNames()) {
			System.out.println(name);
		}
	}

	@Bean
	public JacksonJaxbJsonProvider jacksonJaxbJsonProvider() {
		return new JacksonJaxbJsonProvider();
	}

	@Bean
	public Server rsServer() throws DescriptorParserException, IOException {
		// setup CXF-RS
		JAXRSServerFactoryBean endpoint = new JAXRSServerFactoryBean();
		endpoint.setBus(bus);
		endpoint.setServiceBeans(Arrays.<Object>asList(new UserServiceImpl(remoteCacheContainer())));
		endpoint.setProvider(jacksonJaxbJsonProvider());
		endpoint.setAddress("/");
		endpoint.setFeatures(Arrays.asList(new Swagger2Feature()));
		return endpoint.create();
	}

	@Bean
	public RemoteCacheManager remoteCacheContainer() throws DescriptorParserException, IOException {

		Objects.requireNonNull(cacheServer, "Infinispan service host not found in the environment");
		Objects.requireNonNull(cachePort, "Infinispan service port not found in the environment");

		String hostPort = cacheServer + ":" + cachePort;
		logger.info("Connecting to the Infinispan service at {}", hostPort);

		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.addServer().host(cacheServer).port(cachePort);
		builder.security().authentication()
        .serverName(serverName) //define server name, should be specified in XML configuration
        .saslMechanism("DIGEST-MD5") // define SASL mechanism, in this example we use DIGEST with MD5 hash
        .callbackHandler(new LoginHandler(realmUser, realmPassword.toCharArray(), realm)) // define login handler, implementation defined
        .enable();
		
		RemoteCacheManager remoteCacheManager = new RemoteCacheManager(builder.create(), true);

		return remoteCacheManager;
	}

}

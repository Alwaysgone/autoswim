package io.autoswim.quarkus.config;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MicroprofileAutoswimConfig {
	@ConfigProperty(name = "autoswim.port")
	int swimPort;
	
	@ConfigProperty(name = "autoswim.workingDir")
	Path workingDir;
	
	@ConfigProperty(name = "autoswim.registerDefaultMessageHandler", defaultValue = "true")
	boolean registerDefaultMessageHandler;
	
	@ConfigProperty(name = "autoswim.seedNodes")
	List<String> seedNodes;
	
	@ConfigProperty(name = "autoswim.memberAlias")
	Optional<String> memberAlias;

	public int getSwimPort() {
		return swimPort;
	}

	public Path getWorkingDir() {
		return workingDir;
	}

	public boolean isRegisterDefaultMessageHandler() {
		return registerDefaultMessageHandler;
	}

	public List<String> getSeedNodes() {
		return seedNodes;
	}

	public Optional<String> getMemberAlias() {
		return memberAlias;
	}
}

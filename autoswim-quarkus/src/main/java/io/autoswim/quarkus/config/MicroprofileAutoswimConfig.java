package io.autoswim.quarkus.config;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MicroprofileAutoswimConfig {
	@ConfigProperty(name = "autoswim.port")
	int swimPort;
	
	@ConfigProperty(name = "autoswim.working-dir")
	Path workingDir;
	
	@ConfigProperty(name = "autoswim.register-default-message-handler", defaultValue = "true")
	boolean registerDefaultMessageHandler;
	
	@ConfigProperty(name = "autoswim.seed-nodes")
	Optional<List<String>> seedNodes;
	
	@ConfigProperty(name = "autoswim.member-alias")
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
		return seedNodes.orElse(Collections.emptyList());
	}

	public Optional<String> getMemberAlias() {
		return memberAlias;
	}
}

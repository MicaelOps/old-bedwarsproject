package br.com.logicmc.bedwars.extra;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.function.Consumer;

public class YamlFile {

	private final String name;
	private FileConfiguration config;
	private File file;
	
	public YamlFile(String name) {
		this.name = name;
	}

	public boolean load(JavaPlugin plugin) {
		file = new File(plugin.getDataFolder(), name);
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), name));
		return true;
	}

	public boolean loadResource(JavaPlugin plugin) {
		file = new File(plugin.getDataFolder(), name);
		if(!file.exists()) {
			try (InputStream stream = plugin.getResource(name)) {
				try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
					int i = stream.read();
					while (i != -1) {
						writer.write(i);
						i = stream.read();
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}

		}
		config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), name));
		return true;
	}
	public void save() {
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public FileConfiguration getConfig() {
		return config;
	}

	public void loopThroughSectionKeys(String section, Consumer<? super String> method) {
		ConfigurationSection configsection = config.getConfigurationSection(section);
		if(configsection != null)
			configsection.getKeys(false).forEach(method);
	}
	public Location getLocation(String path) {
		if (config !=null) {
			if(config.get(path) != null)
				return new Location(Bukkit.getWorld(path+".world"), config.getDouble(path+".x"),config.getDouble(path+".y"),config.getDouble(path+".z") );
		}
		return null;
	}
	public void setLocation(String path, Location location) {
		config.set(path+".x",location.getX());
		config.set(path+".y",location.getY());
		config.set(path+".z",location.getZ());
		save();
	}
}

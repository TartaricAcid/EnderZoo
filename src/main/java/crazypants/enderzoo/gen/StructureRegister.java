package crazypants.enderzoo.gen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import crazypants.enderzoo.IoUtil;
import crazypants.enderzoo.Log;
import crazypants.enderzoo.config.Config;
import crazypants.enderzoo.gen.io.StructureResourceManager;
import crazypants.enderzoo.gen.structure.StructureTemplate;
import crazypants.enderzoo.gen.structure.StructureGenerator;

public class StructureRegister {

  public static final StructureRegister instance = createInstance();

  private static StructureRegister createInstance() {
    StructureRegister reg = new StructureRegister();
    reg.init();
    return reg;
  }

  private final Map<String, StructureGenerator> generators = new HashMap<String, StructureGenerator>();
  private final Map<String, StructureTemplate> templates = new HashMap<String, StructureTemplate>();
  //Keep these separately so they can be retried ever reload attempt
  private final Set<String> genUids = new HashSet<String>();
  

  private StructureResourceManager resourceManager;

  private StructureRegister() {
  }

  private void init() {
    resourceManager = new StructureResourceManager(this);
  }

  public StructureResourceManager getResourceManager() {
    return resourceManager;
  }

  public void registerJsonGenerator(String json) throws Exception {
    StructureGenerator tp = resourceManager.parseJsonGenerator(json);
    registerConfig(tp);
  }

  public void registerConfig(StructureGenerator gen) {
    generators.put(gen.getUid(), gen);
    genUids.add(gen.getUid());
  }

  public StructureGenerator getConfig(String uid) {
    return generators.get(uid);
  }

  public Collection<StructureGenerator> getConfigs() {
    return generators.values();
  }

  public void registerStructureData(String uid, NBTTagCompound nbt) throws IOException {
    templates.put(uid, new StructureTemplate(nbt));
  }

  public void registerStructureData(String uid, StructureTemplate st) {
    templates.put(uid, st);
  }

  public StructureTemplate getStructureTemplate(String uid) {
    if(templates.containsKey(uid)) {
      return templates.get(uid);
    }
    StructureTemplate sd = null;
    try {
      sd = resourceManager.loadStructureData(uid);
    } catch (IOException e) {
      Log.error("StructureRegister: Could not load structure data for " + uid + " Ex: " + e);
      e.printStackTrace();
    } finally {
      templates.put(uid, sd);
    }
    return sd;
  }

  public void reload() {
    templates.clear();
    generators.clear();
    for (String uid : genUids) { 
      StructureGenerator tmp;
      try {
        tmp = resourceManager.loadTemplate(uid);
        if(tmp != null) {
          registerConfig(tmp);
        }
      } catch (Exception e) {
        Log.error("StructureRegister: Could not load structure data for " + uid + " Ex: " + e);
        e.printStackTrace();
      }
    }

  }

}
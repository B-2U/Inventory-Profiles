package io.github.jsnimda.common.input;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.lwjgl.glfw.GLFW;

import io.github.jsnimda.common.gui.DebugScreen.DebugInfos;
import io.github.jsnimda.common.input.KeybindSettings.Context;
import io.github.jsnimda.common.input.KeybindSettings.KeyAction;
import net.minecraft.client.Minecraft;

public class GlobalInputHandler {

  private static final GlobalInputHandler INSTANCE = new GlobalInputHandler();
  public static GlobalInputHandler getInstance() {
    return INSTANCE;
  }

  public final List<Integer> pressingKeys = new ArrayList<>();
  public List<Integer> beforePressingKeys = new ArrayList<>();
  public int lastKey = -1;
  public int lastAction = -1;

  private Optional<Keybind> currentSettingKeybind = Optional.empty(); // TODO beautify code
  private boolean firstKey = false;

  private GlobalInputHandler() {

  }

  public boolean isActivated(List<Integer> keyCodes, KeybindSettings settings) {
    if (keyCodes.isEmpty())
      return false;
    if (settings.activateOn == KeyAction.PRESS && lastAction == GLFW.GLFW_RELEASE)
      return false;
    if (settings.activateOn == KeyAction.RELEASE && lastAction == GLFW.GLFW_PRESS)
      return false;
    if (settings.context == Context.INGAME && Minecraft.getInstance().currentScreen != null)
      return false;
    if (settings.context == Context.GUI && Minecraft.getInstance().currentScreen == null)
      return false;
    // checked: context, activateOn
    // ref: malilib KeybindMulti.updateIsPressed()
    List<Integer> pressedKeys = lastAction == GLFW.GLFW_PRESS ? pressingKeys : beforePressingKeys;
    if (pressedKeys.size() >= keyCodes.size() && (settings.allowExtraKeys || pressedKeys.size() == keyCodes.size())) {
      if (settings.orderSensitive) {
        for (int i = 0; i < keyCodes.size(); i++) {
          if (keyCodes.get(keyCodes.size() - 1 - i).intValue() != pressedKeys.get(pressedKeys.size() - 1 - i).intValue()) {
            return false;
          }
        }
        return true;
      } else { // order insensitive
        return keyCodes.contains(lastKey) && pressedKeys.containsAll(keyCodes);
      }
    } else {
      return false;
    }
  }

  public void setCurrentSettingKeybind(Keybind keybind) {
    this.currentSettingKeybind = Optional.ofNullable(keybind);
    this.firstKey = false;
    this.ingoreNextKey = true;
  }

  public Keybind getCurrentSettingKeybind() {
    return this.currentSettingKeybind.orElse(null);
  }

  private boolean ingoreNextKey = true; // FIXME temporary fix

  private void handleCurrentSettingKeybind() {
    if (lastAction == GLFW.GLFW_PRESS) {
      if (this.ingoreNextKey) {
        ingoreNextKey = false;
        return;
      }
      this.firstKey = true;
      if (lastKey == GLFW.GLFW_KEY_ESCAPE) {
        this.currentSettingKeybind.get().setKeyCodes(new ArrayList<>());
        this.currentSettingKeybind = Optional.empty();
      } else {
        this.currentSettingKeybind.get().setKeyCodes(new ArrayList<>(pressingKeys));
      }
    } else if (lastAction == GLFW.GLFW_RELEASE) {
      if (pressingKeys.isEmpty() && this.firstKey) {
        this.currentSettingKeybind = Optional.empty();
      }
    }
  }

  public boolean onKeyPress(int key) {
    if (pressingKeys.contains(key)) return false; // should err / cancelled by other mod
    beforePressingKeys = new ArrayList<>(pressingKeys);
    pressingKeys.add(key);
    lastKey = key;
    lastAction = GLFW.GLFW_PRESS;
    return onInput();
  }

  public boolean onKeyRelease(int key) {
    if (!pressingKeys.contains(key)) return false; // should err / cancelled by other mod
    beforePressingKeys = new ArrayList<>(pressingKeys);
    pressingKeys.remove((Object)key);
    lastKey = key;
    lastAction = GLFW.GLFW_RELEASE;
    return onInput();
  }

  private boolean onInput() {
    if (currentSettingKeybind.isPresent()) {
      handleCurrentSettingKeybind();
      return true;
    }
    registeredInputHandlers.forEach(x -> x.onInput(lastKey, lastAction));
    return false;
  }


  public boolean onKey(int key, int scanCode, int action, int modifiers) {
    DebugInfos.onKey(key, scanCode, action, modifiers);
    if (action == GLFW.GLFW_PRESS) {
      return onKeyPress(key);
    }
    if (action == GLFW.GLFW_RELEASE) {
      return onKeyRelease(key);
    }
    return false;
  }
  public boolean onMouseButton(int button, int action, int mods) {
    DebugInfos.onMouseButton(button, action, mods);
    if (action == GLFW.GLFW_PRESS) {
      return onKeyPress(button - 100);
    }
    if (action == GLFW.GLFW_RELEASE) {
      return onKeyRelease(button - 100);
    }
    return false;
  }

  // ============
  // Api
  // ============

  private List<IInputHandler> registeredInputHandlers = new ArrayList<>();
  public void registerInputHandler(IInputHandler inputHandler) {
    if (!registeredInputHandlers.contains(inputHandler)) {
      registeredInputHandlers.add(inputHandler);
    }
  }

}
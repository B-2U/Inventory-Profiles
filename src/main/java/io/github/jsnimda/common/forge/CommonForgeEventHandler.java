package io.github.jsnimda.common.forge;

import io.github.jsnimda.common.event.GlobalInitHandler;
import io.github.jsnimda.common.input.GlobalInputHandler;
import io.github.jsnimda.common.vanilla.Vanilla;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class CommonForgeEventHandler {

  // Keyboard <-> KeyboardListener , onKey <-> onKeyEvent // ref: malilib forge 1.14.4 ForgeInputEventHandler
  @SubscribeEvent
  public void onKeyboardInput(InputEvent.KeyInputEvent event) {
    if (Vanilla.INSTANCE.screen() == null) { // non null is handled below
      GlobalInputHandler.INSTANCE.onKey(event.getKey(), event.getScanCode(), event.getAction(), event.getModifiers());
    }
  }

  // Keyboard.onKey()
  // fix vanilla keybind swallow my listener
  // by line 308 aboolean[0] early returned
  // (e.g. pressing z + 1 while hovering slots)
  @SubscribeEvent
  public void onKey1(GuiScreenEvent.KeyboardKeyPressedEvent.Pre event) {
    // tmp solution fixing crafting recipe crash when opening other screen
    // (as post will also be swallowed if vanilla screen handle it)
    // fixme better approach
    Screen lastScreen = Vanilla.INSTANCE.screen();
    GlobalInputHandler.INSTANCE.onKey(event.getKeyCode(), event.getScanCode(), GLFW.GLFW_PRESS, event.getModifiers());
    if (lastScreen != Vanilla.INSTANCE.screen() && event.isCancelable()) { // detect gui change, cancel vanilla
      event.setCanceled(true);
    }
  }

  @SubscribeEvent
  public void onKey0(GuiScreenEvent.KeyboardKeyReleasedEvent.Pre event) {
    Screen lastScreen = Vanilla.INSTANCE.screen();
    GlobalInputHandler.INSTANCE.onKey(event.getKeyCode(), event.getScanCode(), GLFW.GLFW_RELEASE, event.getModifiers());
    if (lastScreen != Vanilla.INSTANCE.screen() && event.isCancelable()) { // detect gui change, cancel vanilla
      event.setCanceled(true);
    }
  }


  @SubscribeEvent
  public void onMouseInputEvent(InputEvent.MouseInputEvent event) {
    GlobalInputHandler.INSTANCE.onMouseButton(event.getButton(), event.getAction(), event.getMods());
  } // fixme occasionally throw npe on Vanilla.mc() (idk why, build/class loading related?)

  @SubscribeEvent
  public void onWorldLoad(WorldEvent.Load event) {
    // clear keybind (pressing keys)
    GlobalInputHandler.INSTANCE.getPressedKeys().clear();
  }

  // todo mouse move event
  /*
  @Inject(method = "onCursorPos", at = @At("RETURN"))
  private void onCursorPos(long handle, double xpos, double ypos, CallbackInfo ci) {
    VanillaUtil.INSTANCE.updateMouse();
  }
   */

  boolean initialized = false;

  // do onInit() on first tick (minecraft client ready)
  @SubscribeEvent
  public void clientClick(TickEvent.ClientTickEvent e) {
    if (e.phase == TickEvent.Phase.START) {
      if (!initialized) {
        initialized = true;
        GlobalInitHandler.INSTANCE.onInit();
      }
    }
  }

}
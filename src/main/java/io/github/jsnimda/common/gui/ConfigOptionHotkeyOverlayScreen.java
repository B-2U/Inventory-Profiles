package io.github.jsnimda.common.gui;

import java.util.List;
import java.util.stream.Collectors;

import com.mojang.blaze3d.platform.GlStateManager;

import io.github.jsnimda.common.config.IConfigOption;
import io.github.jsnimda.common.config.options.ConfigHotkey;
import io.github.jsnimda.common.input.ConfigElementKeybindSetting;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TranslationTextComponent;

public class ConfigOptionHotkeyOverlayScreen extends OverlayScreen {

  private ConfigHotkey configHotkey;
  private ConfigElementKeybindSetting keybindSettingElement;
  List<? extends IConfigOption> configs;
  List<ConfigOptionWidgetBase<?>> configWidgets;

  private int dialogX;
  private int dialogY;
  private int dialogWidth;
  private int dialogHeight;
  protected ConfigOptionHotkeyOverlayScreen(ConfigHotkey configHotkey) {
    super(new TranslationTextComponent("inventoryprofiles.common.gui.config.advanced_keybind_settings"));
    this.configHotkey = configHotkey;
    this.keybindSettingElement = new ConfigElementKeybindSetting(configHotkey.getMainKeybind().getDefaultSettings(), configHotkey.getMainKeybind().getSettings());
    configs = keybindSettingElement.getConfigOptions();
    configWidgets = configs.stream().map(x -> ConfigOptionWidgetBase.of(x)).collect(Collectors.toList());
  }

  private static final int COLOR_WHITE  = 0xFFFFFFFF;
  private static final int COLOR_BORDER = 0xFF999999;
  private static final int COLOR_BG     = 0xFF000000;
  @Override
  public void render(int mouseX, int mouseY, float partialTicks) {
    super.render(mouseX, mouseY, partialTicks);
    RenderHelper.disableStandardItemLighting();
    GlStateManager.disableDepthTest();
    GlStateManager.pushMatrix();
    GlStateManager.translatef(0, 0, 400);
    this.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
    configHotkey.getMainKeybind().setSettings(keybindSettingElement.getSettings());

    dialogHeight = 5 * 20 + 2 + 10;
    int maxTextW = configs.stream().mapToInt(x -> this.font.getStringWidth(I18n.format("inventoryprofiles.common.gui.config." + x.getKey()))).max().orElse(0);
    dialogWidth = maxTextW + 150 + 2 + 20;
    dialogWidth = Math.max(this.font.getStringWidth("\u00a7l" + this.title.getFormattedText()) + 20, dialogWidth);
    dialogX = (this.width - dialogWidth) / 2;
    dialogY = (this.height - dialogHeight) / 2;

    fill(dialogX, dialogY, dialogX + dialogWidth, dialogY + dialogHeight, COLOR_BG);
    VHLine.outline(dialogX, dialogY, dialogX + dialogWidth, dialogY + dialogHeight, COLOR_BORDER);

    int y0 = dialogY + 2;
    drawCenteredString(this.font, "\u00a7l" + this.title.getFormattedText(), dialogX + dialogWidth / 2, y0 + 6, COLOR_WHITE);

    for (ConfigOptionWidgetBase<?> widget : configWidgets) {
      y0 += 20;
      String displayName = I18n.format("inventoryprofiles.common.gui.config." + widget.configOption.getKey());
      int tx = dialogX + 10;
      int ty = y0 + 6;
      int tw = this.font.getStringWidth(displayName);
      drawString(this.font, displayName, tx, ty, COLOR_WHITE);
      if (VHLine.contains(tx, ty - 1, tx + tw, ty + 9 + 1, mouseX, mouseY)) {
        Tooltips.getInstance().addTooltip(I18n.format("inventoryprofiles.common.gui.config.description." + widget.configOption.getKey()), mouseX, mouseY, k -> k * 2 / 3);
      }
      widget.x = dialogX + maxTextW + 2 + 10;
      widget.y = y0;
      widget.width = 150;
      widget.render(mouseX, mouseY, partialTicks);
    }

    Tooltips.getInstance().renderAll();

    GlStateManager.popMatrix();
  }

  @Override
  public boolean mouseClicked(double d, double e, int i) {
    if (super.mouseClicked(d, e, i)) {
      return true;
    }
    if (!VHLine.contains(dialogX, dialogY, dialogX + dialogWidth, dialogY + dialogHeight, (int)d, (int)e)) { // click outside dialog
      this.onClose();
      return true;
    }
    return false;
  }

  @Override
  protected void init() {
    configWidgets.forEach(x -> children.add(x));
  }

}
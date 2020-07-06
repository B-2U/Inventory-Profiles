package io.github.jsnimda.inventoryprofiles.event

import io.github.jsnimda.common.input.GlobalInputHandler
import io.github.jsnimda.common.input.KeyCodes
import io.github.jsnimda.common.math2d.Line
import io.github.jsnimda.common.math2d.Rectangle
import io.github.jsnimda.common.math2d.intersects
import io.github.jsnimda.common.util.containsAny
import io.github.jsnimda.common.vanilla.Vanilla
import io.github.jsnimda.common.vanilla.VanillaUtil
import io.github.jsnimda.common.vanilla.alias.ContainerScreen
import io.github.jsnimda.common.vanilla.alias.CraftingInventory
import io.github.jsnimda.common.vanilla.alias.CraftingResultInventory
import io.github.jsnimda.common.vanilla.alias.PlayerInventory
import io.github.jsnimda.inventoryprofiles.config.ModSettings
import io.github.jsnimda.inventoryprofiles.config.Tweaks
import io.github.jsnimda.inventoryprofiles.gui.inject.ContainerScreenHandler
import io.github.jsnimda.inventoryprofiles.ingame.*
import io.github.jsnimda.inventoryprofiles.inventory.ContainerClicker
import io.github.jsnimda.inventoryprofiles.inventory.ContainerTypes
import io.github.jsnimda.inventoryprofiles.inventory.VanillaContainerType.*
import io.github.jsnimda.inventoryprofiles.item.isEmpty

object GameEventHandler {
  var x = -1
  var y = -1
  var lastX = -1
  var lastY = -1
  fun onTick() {
    lastX = x
    lastY = y
    x = VanillaUtil.mouseX()
    y = VanillaUtil.mouseY()
    ContinuousCraftingHandler.onTick()
    if (ModSettings.ENABLE_AUTO_REFILL.booleanValue) {
      AutoRefillHandler.onTick()
    }
    if (Tweaks.CONTAINER_SWIPE_MOVING_ITEMS.booleanValue) {
      MiscHandler.swipeMoving()
    }
  }

  fun onJoinWorld() {
    GlobalInputHandler.pressedKeys.clear() // sometimes left up not captured
    if (ModSettings.ENABLE_AUTO_REFILL.booleanValue) {
      AutoRefillHandler.onJoinWorld()
    }
  }

  fun postScreenRender() {
    // partial tick = this.client.getLastFrameDuration()
    ContainerScreenHandler.postRender()
  }

  fun preRenderTooltip() {
    ContainerScreenHandler.preRenderTooltip()
  }

  fun preScreenRender() {
    ContainerScreenHandler.preScreenRender()
  }

  // only client should call this
  fun onCrafted() {
    ContinuousCraftingHandler.onCrafted()
  }
}

object MiscHandler {
  fun swipeMoving() {
    if (!VanillaUtil.shiftDown()) return
    if (!GlobalInputHandler.pressedKeys.contains(KeyCodes.MOUSE_BUTTON_1)) return
    // fixed mouse too fast skip slots
    // use ContainerScreen.isPointOverSlot()/.getSlotAt() / Slot.x/yPosition
    val screen = Vanilla.screen()
    val containerBounds = (screen as? ContainerScreen<*>)?.`(containerBounds)` ?: return
    val line = with(GameEventHandler) { Line(lastX, lastY, x, y) }

    val types = ContainerTypes.getTypes(Vanilla.container())
    val matchSet = setOf(NO_SORTING_STORAGE, SORTABLE_STORAGE, PURE_BACKPACK)
    for (slot in Vanilla.container().`(slots)`) {
      // disable for non storage (tmp solution for crafting table result slot)
      if (!Tweaks.SWIPE_MOVE_CRAFTING_RESULT_SLOT.booleanValue) {
        if (!types.containsAny(matchSet) && slot.`(inventory)` !is PlayerInventory) continue
        if (slot.`(inventory)` is CraftingInventory || slot.`(inventory)` is CraftingResultInventory) continue
      }

      val rect = Rectangle(containerBounds.x + slot.`(left)`, containerBounds.y + slot.`(top)`, 16, 16)
      if (!line.intersects(rect)) continue
      if (slot.`(itemStack)`.isEmpty()) continue
      ContainerClicker.shiftClick(vPlayerSlotOf(slot, screen).`(id)`)
    }
  }
}
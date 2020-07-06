package io.github.jsnimda.common.vanilla

import io.github.jsnimda.common.vanilla.alias.IntegratedServer
import io.github.jsnimda.common.vanilla.alias.MinecraftClient
import io.github.jsnimda.common.vanilla.alias.Screen
import io.github.jsnimda.common.vanilla.alias.Window

// ============
// vanillamapping code depends on mappings (package io.github.jsnimda.common.vanilla)
// ============

object Vanilla {

  // ============
  // minecraft objects
  // ============

  fun mc() = MinecraftClient.getInstance() ?: error("MinecraftClient is not initialized!")
  fun window(): Window = mc().mainWindow ?: error("mc.window is not initialized!")
  fun screen(): Screen? = mc().currentScreen

  fun textRenderer() = mc().fontRenderer ?: error("mc.textRenderer is not initialized!")
  fun textureManager() = mc().textureManager ?: error("mc.textureManager is not initialized!")
  fun soundManager() = mc().soundHandler ?: error("mc.soundManager is not initialized!")
  fun languageManager() = mc().languageManager ?: error("mc.languageManager is not initialized!")
  fun resourceManager() = mc().resourceManager ?: error("mc.resourceManager is not initialized!")

  fun inGameHud() = mc().ingameGUI ?: error("mc.inGameHud is not initialized!")
  fun chatHud() = inGameHud().chatGUI ?: throw AssertionError("unreachable")

  fun mouse() = mc().mouseHelper ?: error("mc.mouse is not initialized!")

  fun server(): IntegratedServer? = mc().integratedServer

  // ============
  // java objects
  // ============

  fun runDirectoryFile() = mc().gameDir ?: error("mc.runDirectory is not initialized!")

  // ============
  // in-game objects
  // ============

  fun worldNullable() = mc().world ?: null
  fun playerNullable() = mc().player ?: null

  fun world() = worldNullable() ?: error("mc.world is not initialized! Probably not in game")
  fun player() = playerNullable() ?: error("mc.player is not initialized! Probably not in game")
  fun playerInventory() = player().inventory ?: throw AssertionError("unreachable")
  fun playerContainer() = player().container ?: throw AssertionError("unreachable") // container / openContainer
  fun container() = player().openContainer ?: playerContainer()

  fun interactionManager() =
    mc().playerController ?: error("mc.interactionManager is not initialized! Probably not in game")

  fun recipeBook() = player().recipeBook ?: throw AssertionError("unreachable")

}

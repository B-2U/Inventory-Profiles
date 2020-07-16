package io.github.jsnimda.inventoryprofiles.event

import io.github.jsnimda.common.vanilla.Vanilla
import io.github.jsnimda.common.vanilla.alias.Container
import io.github.jsnimda.common.vanilla.alias.ContainerScreen
import io.github.jsnimda.common.vanilla.alias.CraftingInventory
import io.github.jsnimda.common.vanilla.alias.Slot
import io.github.jsnimda.inventoryprofiles.config.GuiSettings
import io.github.jsnimda.inventoryprofiles.ingame.`(id)`
import io.github.jsnimda.inventoryprofiles.ingame.`(inventory)`
import io.github.jsnimda.inventoryprofiles.ingame.`(itemStack)`
import io.github.jsnimda.inventoryprofiles.ingame.`(slots)`
import io.github.jsnimda.inventoryprofiles.inventory.AdvancedContainer
import io.github.jsnimda.inventoryprofiles.inventory.AreaTypes
import io.github.jsnimda.inventoryprofiles.inventory.ContainerType.CRAFTING
import io.github.jsnimda.inventoryprofiles.inventory.ContainerTypes
import io.github.jsnimda.inventoryprofiles.inventory.data.collect
import io.github.jsnimda.inventoryprofiles.item.*

object ContinuousCraftingHandler {
  private val checked
    get() = GuiSettings.CONTINUOUS_CRAFTING_SAVED_VALUE.booleanValue
  private var trackingScreen: ContainerScreen<*>? = null
  fun onTickInGame() {
    val screen = Vanilla.screen()
    if (screen !is ContainerScreen<*> || !checked) {
      trackingScreen = null
      return
    }
    if (screen != trackingScreen) {
      trackingScreen = screen
      init()
    }
    handle()
  }

  lateinit var monitor: Monitor
  var isCrafting = false
  fun init() {
    val container = Vanilla.container()
    val types = ContainerTypes.getTypes(container)
    isCrafting = types.contains(CRAFTING)
    if (!isCrafting) return
    monitor = Monitor(container)
    onCraftCount = 0
  }

  var onCraftCount = 0 // this tick crafted item
  fun handle() {
    if (!isCrafting) return
    // todo quick craft from recipe book
    if (onCraftCount > 0) {
      onCraftCount--
      monitor.autoRefill()
    }
    monitor.save()
  }

  fun onCrafted() {
    if (!isCrafting) return
    onCraftCount = (onCraftCount + 2).coerceAtMost(2)
    // ^^ i think this is the right approach can really fix the stability
  }

  private fun shouldHandle(storedItem: ItemStack, currentItem: ItemStack): Boolean {
    if (storedItem.isEmpty()) return false
    return currentItem.isEmpty() // storedItem not empty -> became empty
  }

  class Monitor(container: Container) {
    val containerSlots = container.`(slots)`
    val ingredientSlots = containerSlots.filter { it.`(inventory)` is CraftingInventory }

    //    val resultSlot = containerSlots.filterIsInstance<CraftingResultSlot>() // should be 1
    val slotMonitors = ingredientSlots.map { ItemSlotMonitor(it) }

    val playerSlotIndices = with(AreaTypes) { playerStorage + playerHotbar + playerOffhand - lockedSlots }
      .getItemArea(container, containerSlots)
      .slotIndices // supplies

    fun autoRefill() {
      // do statistic
      val typeToSlotListMap = mutableMapOf<ItemType, MutableList<Int>>() // slotIndex
      for (slotMonitor in slotMonitors) {
        with(slotMonitor) {
          if (shouldHandle(storedItem, slot.`(itemStack)`)) {
            // record this
            typeToSlotListMap.getOrPut(storedItem.itemType, { mutableListOf() }).add(slot.`(id)`)
          }
        }
      }
      if (typeToSlotListMap.isEmpty()) {
        return
      }
      AdvancedContainer.tracker(instant = true) {
        val playerSubTracker = tracker.subTracker(playerSlotIndices)
        val counter = playerSubTracker.slots.collect()
        val map: Map<ItemType, Pair<Int, List<MutableItemStack>>> = typeToSlotListMap.mapValues { (type, list) ->
          (counter.count(type) / list.size).coerceAtMost(type.maxCount) to list.map { tracker.slots[it] }
        }
        playerSubTracker.slots.forEach { source ->
          map[source.itemType]?.let { (eachCount, fedList) ->
            for (destination in fedList) {
              if (destination.count >= eachCount) continue
              if (source.isEmpty()) break
              val remaining = eachCount - destination.count
              source.transferNTo(destination, remaining)
            }
          }
        }
      }
    }

    fun save() {
      slotMonitors.forEach { it.save() }
    }
  }

  class ItemSlotMonitor(val slot: Slot) {
    var storedItem = ItemStack.EMPTY

    fun save() {
      storedItem = slot.`(itemStack)`
    }
  }
}
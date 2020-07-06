package io.github.jsnimda.inventoryprofiles.inventory

import io.github.jsnimda.common.vanilla.Vanilla
import io.github.jsnimda.common.vanilla.alias.*
import io.github.jsnimda.inventoryprofiles.ingame.`(invSlot)`
import io.github.jsnimda.inventoryprofiles.ingame.`(inventory)`
import io.github.jsnimda.inventoryprofiles.ingame.`(selectedSlot)`
import io.github.jsnimda.inventoryprofiles.ingame.vFocusedSlot
import io.github.jsnimda.inventoryprofiles.inventory.VanillaContainerType.*

private val hotbarInvSlots = 0..8
private val storageInvSlots = 9..35
private const val offhandInvSlot = 40
private val mainhandInvSlot
  get() = Vanilla.playerInventory().`(selectedSlot)`

object AreaTypes {
  val focusedSlot = AreaType.match { it == vFocusedSlot() }

  val playerStorage = AreaType.player(storageInvSlots.toList())
  val playerHotbar = AreaType.player(hotbarInvSlots.toList())
  val playerHandsAndHotbar =
    AreaType.player(
      (listOf(mainhandInvSlot, offhandInvSlot) + hotbarInvSlots).distinct()
    ) // priority: mainhand -> offhand -> hotbar 1-9

  val playerOffhand = AreaType.player(offhandInvSlot)

  val itemStorage: AreaType = // slots that purpose is storing any item (e.g. crafting table / furnace is not the case)
    AreaType { vanillaContainer, vanillaSlots -> // only non empty for SORTABLE_STORAGE
      val types = ContainerTypes.getTypes(vanillaContainer)
      if (types.contains(SORTABLE_STORAGE)) {
        val isHorse = types.contains(HORSE_STORAGE)
        vanillaSlots.forEachIndexed { slotIndex, slot ->
          if (slot.`(inventory)` is PlayerInventory) return@forEachIndexed
          // first two slot of horse is not item storage
          if (!(isHorse && slot.`(invSlot)` in 0..1)) {
            slotIndices.add(slotIndex)
          }
        }
      }
      // check rectangular
      if (slotIndices.isNotEmpty() && types.contains(RECTANGULAR)) {
        val total = slotIndices.size
        with(types) {
          when {
            contains(WIDTH_9) -> {
              if (total % 9 == 0) {
                isRectangular = true
                width = 9
                height = total / 9
              }
            }
            contains(HEIGHT_3) -> {
              if (total % 3 == 0) {
                isRectangular = true
                width = total / 3
                height = 3
              }
            }
          }
        }
      }
    }

  val nonPlayer = AreaType.match { it.`(inventory)` !is PlayerInventory }
  val nonPlayerNonOutput = AreaType.match {
    it.`(inventory)` !is PlayerInventory
        && it.`(inventory)` !is CraftingResultInventory
        && it !is CraftingResultSlot
        && it !is TradeOutputSlot
  }
  val playerHotbarAndOffhand = AreaType.player(hotbarInvSlots + offhandInvSlot)
  val playerStorageAndHotbarAndOffhand = AreaType.player(storageInvSlots + hotbarInvSlots + offhandInvSlot)

  val craftingIngredient = AreaType.match {
    it.`(inventory)` is CraftingInventory
        && it.`(inventory)` !is CraftingResultInventory
        && it !is CraftingResultSlot
        && it !is TradeOutputSlot
  }
}

class AreaType() {
  companion object {


    //    fun matchSlots(vararg slots: Slot?) = match { it in slots }
    fun match(predicate: (Slot) -> Boolean) = AreaType { _, vanillaSlots ->
      vanillaSlots.forEachIndexed { slotIndex, slot ->
        if (predicate(slot)) slotIndices.add(slotIndex)
      }
    }

    fun player(vararg invSlots: Int) = player(invSlots.toList())
    fun player(invSlots: List<Int>) = AreaType { _, vanillaSlots ->
      val map = mutableMapOf<Int, Int>() // invSlot, slotIndex
      vanillaSlots.forEachIndexed { slotIndex, slot ->
        if (slot.`(inventory)` is PlayerInventory) map[slot.`(invSlot)`] = slotIndex
      }
      invSlots.mapNotNull { map[it] }.let { slotIndices.addAll(it) }
    }
  }

  private var add: ItemArea.(Container, List<Slot>) -> Unit = { _, _ -> }

  constructor(add: ItemArea.(Container, List<Slot>) -> Unit) : this() {
    this.add = add
  }

  fun getItemArea(vanillaContainer: Container, vanillaSlots: List<Slot>): ItemArea =
    ItemArea(this).apply {
      add(vanillaContainer, vanillaSlots)
      if (!isRectangular) {
        val total = slotIndices.size
        if (total % 9 == 0) {
          isRectangular = true
          width = 9
          height = total / 9
        }
      }
    }
}

class ItemArea(val type: AreaType) {
  var isRectangular = false
  var width = 0
  var height = 0
  val slotIndices = mutableListOf<Int>()
  fun isEmpty() = slotIndices.isEmpty()
}

/*
object SectionRegister {
  val list = mutableListOf<Section>()

  // invSlot
  //   head,chest,legs,feet 39 38 37 36
  //   offhand 40
  //   hotbar 0 - 8    left to right
  //   storage 9 - 35    left to right, top to bottom
  val playerMainhand =
    Section(preserveSlot = true) {
      it.slot.`(inventory)`.let { inv -> inv is PlayerInventory && inv.`(selectedSlot)` == it.slot.`(invSlot)` }
    }
  val playerHotbar = section<PlayerInventory>(0 until 9)
  val playerStorage = section<PlayerInventory>(9..35)
  val playerArmor = section<PlayerInventory>(36..39, preserveSlot = true)
    .apply { sort = { list -> list.sortedByDescending { it.slot.`(invSlot)` } } }
  val playerHand = section<PlayerInventory>(39)
  val playerChest = section<PlayerInventory>(38)
  val playerLegs = section<PlayerInventory>(37)
  val playerFeet = section<PlayerInventory>(36)
  val playerOffhand = section<PlayerInventory>(40)
  val playerRemaining = section<PlayerInventory>()

  val nonPlayer = copyOfRemaining()
  val trader = section<TraderInventory>(preserveSlot = true)
  val traderOutput = Section { it.slot is TradeOutputSlot }
  val traderInput = section<TraderInventory>()
  val crafting = section<CraftingInventory>()
  val craftingResult = section<CraftingResultInventory>()

  val horseEquipment = Section { it.owner.container is HorseContainer && it.slot.`(invSlot)` in 0..1 }
  val sortableStorage = Section { it.owner.properties.category == ContainerCategory.SORTABLE_STORAGE }
  val nonSortableStorage = Section { it.owner.properties.category == ContainerCategory.NON_SORTABLE_STORAGE }
  val nonStorage = Section { it.owner.properties.category == ContainerCategory.NON_STORAGE }

}

open class Section(val preserveSlot: Boolean = false, add: Boolean = true, val acceptSlot: (ShallowSlot) -> Boolean) {
  var sort = { list: List<ShallowSlot> -> list.sortedBy { it.slot.`(invSlot)` } }

  init {
    if (add) SectionRegister.list.add(this)
  }
}

private fun copyOfRemaining() = Section(true) { true }

private inline fun <reified T : Inventory> section(range: IntRange, preserveSlot: Boolean = false) =
  Section(preserveSlot) { it.slot.`(inventory)` is T && it.slot.`(invSlot)` in range }

private inline fun <reified T : Inventory> section(range: Int, preserveSlot: Boolean = false) =
  Section(preserveSlot) { it.slot.`(inventory)` is T && it.slot.`(invSlot)` == range }

private inline fun <reified T : Inventory> section(preserveSlot: Boolean = false) =
  Section(preserveSlot) { it.slot.`(inventory)` is T }

*/
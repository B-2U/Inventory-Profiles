package io.github.jsnimda.inventoryprofiles.inventory.sandbox

import io.github.jsnimda.inventoryprofiles.inventory.data.ItemTracker
import io.github.jsnimda.inventoryprofiles.inventory.data.MutableItemTracker
import io.github.jsnimda.inventoryprofiles.item.*

//import io.github.jsnimda.common.vanilla.alias.Slot as VanillaSlot

class ContainerSandbox(
  items: MutableItemTracker,
  clickNode: SandboxClick? = null
) {
  private val mutableItems = items
  val items: ItemTracker
    get() = mutableItems

  fun leftClick(slotIndex: Int) = with(mutableItems) {
    val target = slots[slotIndex]
    if (cursor.isEmpty() || target.isEmpty() || !cursor.stackableWith(target)) {
      cursor.swapWith(target)
    } else {
      cursor.transferTo(target)
    }
    addClick(slotIndex, 0)
  }

  fun rightClick(slotIndex: Int) = with(mutableItems) {
    val target = slots[slotIndex]
    if (cursor.isEmpty()) {
      target.splitHalfTo(cursor)
    } else if (cursor.stackableWith(target)) {
      cursor.transferOneTo(target)
    } else {
      cursor.swapWith(target)
    }
    addClick(slotIndex, 1)
  }

  fun leftClickOutside() = with(mutableItems) {
    thrownItems.add(cursor)
    cursor.setEmpty()
    addClick(-999, 0)
  }

  fun rightClickOutside() = with(mutableItems) { // in creative mode throw all
    val dummy = MutableItemStack.empty()
    cursor.transferOneTo(dummy)
    thrownItems.add(dummy)
    addClick(-999, 1)
  }

  val clickCount
    get() = clickNode?.clickIndex?.plus(1) ?: 0
  var clickNode: SandboxClick? = clickNode
    private set

  private fun addClick(slotIndex: Int, button: Int) {
    SandboxClick(clickCount, slotIndex, button, clickNode).also { clickNode = it }
  }

//  fun copy() = ContainerSandbox(items.copyAsMutable(), clicks)
}

data class SandboxClick(
  val clickIndex: Int,
  val slotIndex: Int,
  val button: Int,
  val previousClick: SandboxClick? = null
)

fun SandboxClick?.toList(): List<SandboxClick> {
  val list = mutableListOf<SandboxClick>()
  var click: SandboxClick? = this
  while (click != null) {
    list.add(click)
    click = click.previousClick
  }
  list.reverse()
  return list
}
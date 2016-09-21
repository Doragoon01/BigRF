package com.dora_goon.bigrf.multiblock.tileentity;

import com.dora_goon.bigrf.multiblock.interfaces.INeighborUpdatableEntity;
import com.dora_goon.bigrf.utility.AdjacentInventoryHelper;

import cofh.lib.util.helpers.BlockHelper;
import erogenousbeef.core.multiblock.MultiblockMasterBase;
import erogenousbeef.core.multiblock.MultiblockValidationException;
import cofh.lib.util.helpers.BlockHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityMultiblockItemPort extends TileEntityMultiblockPart implements IInventory, ISidedInventory, INeighborUpdatableEntity {

	protected ItemStack[] _inventories;
	protected boolean isInlet;
	protected AdjacentInventoryHelper adjacencyHelper;

	public static final int SLOT_INLET = 0;
	public static final int SLOT_OUTLET = 1;
	public static final int NUM_SLOTS = 2;

	private static final int[] kInletExposed = {SLOT_INLET};
	private static final int[] kOutletExposed = {SLOT_OUTLET};

	public TileEntityMultiblockItemPort() {
		super();

		_inventories = new ItemStack[getSizeInventory()];
		isInlet = true;
	}

	// Multiblock overrides
	@Override
	public void onMachineAssembled(MultiblockMasterBase controller) {
		super.onMachineAssembled(controller);

		adjacencyHelper = new AdjacentInventoryHelper(this.getOutwardsDir());
		checkForAdjacentInventories();
	}

	@Override
	public void onMachineBroken() {
		super.onMachineBroken();
		adjacencyHelper = null;
	}

	// TileEntity overrides
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		_inventories = new ItemStack[getSizeInventory()];
		if(tag.hasKey("Items")) {
			NBTTagList tagList = tag.getTagList("Items", 10);
			for(int i = 0; i < tagList.tagCount(); i++) {
				NBTTagCompound itemTag = (NBTTagCompound)tagList.getCompoundTagAt(i);
				int slot = itemTag.getByte("Slot") & 0xff;
				if(slot >= 0 && slot <= _inventories.length) {
					ItemStack itemStack = new ItemStack((Block)null,0,0);
					itemStack.readFromNBT(itemTag);
					_inventories[slot] = itemStack;
				}
			}
		}

		if(tag.hasKey("isInlet")) {
			this.isInlet = tag.getBoolean("isInlet");
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		NBTTagList tagList = new NBTTagList();

		for(int i = 0; i < _inventories.length; i++) {
			if((_inventories[i]) != null) {
				NBTTagCompound itemTag = new NBTTagCompound();
				itemTag.setByte("Slot", (byte)i);
				_inventories[i].writeToNBT(itemTag);
				tagList.appendTag(itemTag);
			}
		}

		if(tagList.tagCount() > 0) {
			tag.setTag("Items", tagList);
		}

		tag.setBoolean("isInlet", isInlet);
	}

	// MultiblockTileEntityBase
	@Override
	protected void encodeDescriptionPacket(NBTTagCompound packetData) {
		super.encodeDescriptionPacket(packetData);

		packetData.setBoolean("inlet", isInlet);
	}

	@Override
	protected void decodeDescriptionPacket(NBTTagCompound packetData) {
		super.decodeDescriptionPacket(packetData);

		if(packetData.hasKey("inlet")) {
			setInlet(packetData.getBoolean("inlet"));
		}
	}

	// IInventory
	@Override
	public int getSizeInventory() {
		return NUM_SLOTS;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return _inventories[slot];
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		if(_inventories[slot] != null)
		{
			if(_inventories[slot].stackSize <= amount)
			{
				ItemStack itemstack = _inventories[slot];
				_inventories[slot] = null;
				markDirty();
				return itemstack;
			}
			ItemStack newStack = _inventories[slot].splitStack(amount);
			if(_inventories[slot].stackSize == 0)
			{
				_inventories[slot] = null;
			}

			markDirty();
			return newStack;
		}
		else
		{
			return null;
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		return null;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack itemstack) {
		_inventories[slot] = itemstack;
		if(itemstack != null && itemstack.stackSize > getInventoryStackLimit())
		{
			itemstack.stackSize = getInventoryStackLimit();
		}

		markDirty();
	}

	@Override
	public String getInventoryName() {
		return "Item Port";
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}


	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		if(worldObj.getTileEntity(xCoord, yCoord, zCoord) != this)
		{
			return false;
		}
		return entityplayer.getDistanceSq((double)xCoord + 0.5D, (double)yCoord + 0.5D, (double)zCoord + 0.5D) <= 64D;
	}

	@Override
	public void openInventory() {
	}

	@Override
	public void closeInventory() {
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemstack) {
		if(itemstack == null) { return true; }

		if(slot == SLOT_INLET) {
			return true; //Reactants.isFuel(itemstack);
		}
		else if(slot == SLOT_OUTLET) {
			return true; //Reactants.isWaste(itemstack);
		}
		else {
			return false;
		}
	}

	// ISidedInventory
	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		if(isInlet()) {
			return kInletExposed;
		}
		else {
			return kOutletExposed;
		}
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack itemstack, int side) {
		return isItemValidForSlot(slot, itemstack);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack itemstack, int side) {
		return isItemValidForSlot(slot, itemstack);
	}

	/**
	 * Called when stuff has been placed in the access port
	 */

	public boolean isInlet() { return this.isInlet; }

	public void setInlet(boolean shouldBeInlet) {
		if(isInlet == shouldBeInlet) { return; }

		isInlet = shouldBeInlet;

		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);

		if(!worldObj.isRemote) {
			distributeItems();
			markChunkDirty();
		}

		notifyNeighborsOfTileChange();
	}

	protected void distributeItems() {
		if(worldObj.isRemote) { return; }
		if(adjacencyHelper == null) { return; }

		if(this.isInlet()) { return; }

		_inventories[SLOT_OUTLET] = adjacencyHelper.distribute(_inventories[SLOT_OUTLET]);
		markChunkDirty();
	}


	protected void checkForAdjacentInventories() {
		ForgeDirection outDir = getOutwardsDir();

		if(adjacencyHelper == null && outDir != ForgeDirection.UNKNOWN) {
			adjacencyHelper = new AdjacentInventoryHelper(outDir);
		}

		if(adjacencyHelper != null && outDir != ForgeDirection.UNKNOWN) {
			TileEntity te = worldObj.getTileEntity(xCoord + outDir.offsetX, yCoord + outDir.offsetY, zCoord + outDir.offsetZ);
			if(adjacencyHelper.set(te)) {
				distributeItems();
			}
		}
	}

	protected void markChunkDirty() {
		worldObj.markTileEntityChunkModified(xCoord, yCoord, zCoord, this);
	}


	// INeighborUpdateableEntity
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z,
			Block neighborBlock) {
		//checkForAdjacentInventories();
	}

	@Override
	public void onNeighborTileChange(IBlockAccess world, int x, int y, int z,
			int neighborX, int neighborY, int neighborZ) {
		int side = BlockHelper.determineAdjacentSide(this, neighborX, neighborY, neighborZ);
		if(side == getOutwardsDir().ordinal()) {
			checkForAdjacentInventories();
		}
	}

}

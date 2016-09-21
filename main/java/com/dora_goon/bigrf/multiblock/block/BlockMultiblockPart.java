package com.dora_goon.bigrf.multiblock.block;

import java.util.ArrayList;
import java.util.List;

import com.dora_goon.bigrf.BigRF;
import com.dora_goon.bigrf.multiblock.MultiblockBoiler;
import com.dora_goon.bigrf.multiblock.MultiblockCell;
import com.dora_goon.bigrf.multiblock.MultiblockReactor;
import com.dora_goon.bigrf.multiblock.MultiblockTurbine;
import com.dora_goon.bigrf.multiblock.interfaces.INeighborUpdatableEntity;
import com.dora_goon.bigrf.multiblock.tileentity.TileEntityMultiblockFluidPort;
import com.dora_goon.bigrf.multiblock.tileentity.TileEntityMultiblockFluxPort;
import com.dora_goon.bigrf.multiblock.tileentity.TileEntityMultiblockItemPort;
import com.dora_goon.bigrf.multiblock.tileentity.TileEntityMultiblockPart;
import com.dora_goon.bigrf.reference.CreativeTabBigRF;
import com.dora_goon.bigrf.reference.Reference;
import com.dora_goon.bigrf.utility.LogHelper;
import com.dora_goon.bigrf.utility.StaticUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.core.common.CoordTriplet;
import erogenousbeef.core.multiblock.IMultiblockPart;
import erogenousbeef.core.multiblock.MultiblockMasterBase;
import erogenousbeef.core.multiblock.rectangular.PartPosition;
import erogenousbeef.core.multiblock.rectangular.RectangularMultiblockMasterBase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockMultiblockPart extends BlockContainer {
	
	public static final int METADATA_FRAME = 0;
	public static final int METADATA_FLUXPORT = 1;
	public static final int METADATA_ITEMPORT = 2;
	public static final int METADATA_FLUIDPORT = 3;

	public static final int METADATA_ENERGYCELL_CONTROLLER = 4;
	public static final int METADATA_BOILER_CONTROLLER = 5;
	public static final int METADATA_TURBINE_CONTROLLER = 6;
	public static final int METADATA_REACTOR_CONTROLLER = 7;

    public static final  int[] METADATA_CONTROLLERS = {METADATA_ENERGYCELL_CONTROLLER,METADATA_BOILER_CONTROLLER,METADATA_TURBINE_CONTROLLER,METADATA_REACTOR_CONTROLLER};

	private static final int PORT_IN = 0;
	private static final int PORT_OUT = 1;
	private static final int PORT_OFF = 2;
	
	private static final int CONTROLLER_OFF = 0;
	private static final int CONTROLLER_ON = 1;
	private static final int CONTROLLER_IDLE = 2;
	
	private static String[] _subBlocks = new String[] { "frame",
														"fluxport",
														"itemport",
														"fluidport",
														"energy_cell",
														"boiler",
														"turbine",
														"reactor",};
	
	private static String[][] _states = new String[][] {
		{"default", "face", "corner", "eastwest", "northsouth", "vertical"}, // Frame
		{"in", "out","off"}, 			// Flux Port
		{"in", "out","off"}, 			// Item Port
		{"in", "out","off"}, 			// Fluid Port
		{"off", "idle", "on"}, 		    // EC Controller
		{"off", "idle", "on"}, 		// Boiler Controller
		{"off", "idle", "on"}, 		// Turbine Controller
		{"off", "idle", "on"}, 		// Reactor Controller
	};
	
	private IIcon[][] _icons = new IIcon[_states.length][];
	
	public static boolean isFrame(int metadata) { return metadata == METADATA_FRAME; }
	public static boolean isFluxPort(int metadata) { return metadata == METADATA_FLUXPORT; }
	public static boolean isItemPort(int metadata) { return metadata == METADATA_ITEMPORT; }
	public static boolean isFluidPort(int metadata) { return metadata == METADATA_FLUIDPORT; }


	public static boolean isController(int metadata) {
		for (int i : METADATA_CONTROLLERS) {
			if (i == metadata) {return true;}
		}
		return false;
	}

	public static boolean isCellController(int metadata) { return metadata == METADATA_ENERGYCELL_CONTROLLER; }
	public static boolean isBoilerController(int metadata) { return metadata == METADATA_BOILER_CONTROLLER; }
	public static boolean isTurbineController(int metadata) { return metadata == METADATA_TURBINE_CONTROLLER; }
	public static boolean isReactorController(int metadata) { return metadata == METADATA_REACTOR_CONTROLLER; }

	
	
	public BlockMultiblockPart(Material material) {
		super(material);
		
		setStepSound(soundTypeMetal);
		setHardness(2.0f);
		setBlockName("multiblockPart");
		this.setBlockTextureName(Reference.TEXTURE_NAME_PREFIX + "blockMultiblockPart");
		this.setCreativeTab(CreativeTabBigRF.BigRF_TAB);
	}


	@Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
		IIcon icon = null;
		int metadata = blockAccess.getBlockMetadata(x,y,z);
						
		switch(metadata) {
			case METADATA_FRAME:
				icon = getFrameIcon(blockAccess, x, y, z, side);
				break;
			case METADATA_FLUXPORT:
				icon = getFluxPortIcon(blockAccess, x, y, z, side);
				break;
			case METADATA_ITEMPORT:
				icon = getItemPortIcon(blockAccess, x, y, z, side);
				break;
			case METADATA_FLUIDPORT:
				icon = getFluidPortIcon(blockAccess, x, y, z, side);
				break;
			case METADATA_ENERGYCELL_CONTROLLER:
				icon = getControllerIcon(blockAccess, x, y, z, side);
				break;
			case METADATA_BOILER_CONTROLLER:
				icon = getControllerIcon(blockAccess, x, y, z, side);
				break;
			case METADATA_TURBINE_CONTROLLER:
				icon = getControllerIcon(blockAccess, x, y, z, side);
				break;
			case METADATA_REACTOR_CONTROLLER:
				icon = getControllerIcon(blockAccess, x, y, z, side);
				break;
		}

		return icon != null ? icon : getIcon(side, metadata);
	}


	@Override
	public IIcon getIcon(int side, int metadata)
	{
		if(side > 1 && (metadata >= 0 && metadata < _icons.length)) {
			return _icons[metadata][0];
		}
		return blockIcon;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister)
	{
		String prefix = Reference.TEXTURE_NAME_PREFIX + getUnlocalizedName() + ".";

		for(int metadata = 0; metadata < _states.length; ++metadata) {
			String[] blockStates = _states[metadata];
			_icons[metadata] = new IIcon[blockStates.length];

			for(int state = 0; state < blockStates.length; state++) {
				_icons[metadata][state] = par1IconRegister.registerIcon(prefix + _subBlocks[metadata] + "." + blockStates[state]);
			}
		}
		
		this.blockIcon = par1IconRegister.registerIcon(Reference.TEXTURE_NAME_PREFIX + getUnlocalizedName());
	}

		
	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		switch(metadata) {
		case METADATA_FLUXPORT:
			return new TileEntityMultiblockFluxPort();
		case METADATA_ITEMPORT:
			return new TileEntityMultiblockItemPort();
		case METADATA_FLUIDPORT:
			return new TileEntityMultiblockFluidPort();
		default:
			return new TileEntityMultiblockPart();
		}
	}


	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block neighborBlock) {
		TileEntity te = StaticUtils.TE.getTileEntityUnsafe(world, x, y, z);

		// Signal power taps when their neighbors change, etc.
		if(te instanceof INeighborUpdatableEntity) {
			((INeighborUpdatableEntity)te).onNeighborBlockChange(world, x, y, z, neighborBlock);
		}
	}

	@Override
	public void onNeighborChange(IBlockAccess world, int x, int y, int z, int neighborX, int neighborY, int neighborZ) {
		TileEntity te = StaticUtils.TE.getTileEntityUnsafe(world, x, y, z);

		// Signal power taps when their neighbors change, etc.
		if(te instanceof INeighborUpdatableEntity) {
			((INeighborUpdatableEntity)te).onNeighborTileChange(world, x, y, z, neighborX, neighborY, neighborZ);
		}
	}


	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		if(player.isSneaking()) {
			return false;
		}

		int metadata = world.getBlockMetadata(x, y, z);
		TileEntity te = world.getTileEntity(x, y, z);
		IMultiblockPart part = null;
		MultiblockMasterBase master = null;

		if(te instanceof IMultiblockPart) {
			part = (IMultiblockPart)te;
			master = part.getMultiblockMaster();
		}
		
		if(isFrame(metadata)) {
			// If the player's hands are empty and they rightclick on a multiblock, they get a 
			// multiblock-debugging message if the machine is not assembled.
			if(player.getCurrentEquippedItem() == null) {
				//LogHelper.info("BMbP - This block's master is a " + master.getClass().getSimpleName());
				if(master != null) {
					Exception e = master.getLastValidationException();
					if(e != null) {
						player.addChatMessage(new ChatComponentText(e.getMessage()));
						return true;
					}
				}
				else {
					player.addChatMessage(new ChatComponentText("Block is not connected to a multiblock. This could be due to lag, or a bug. If the problem persists, try breaking and re-placing the block."));
					return true;
				}
			}

			// If nonempty, or there was no error, just fall through
			return false;
		}
		// Do toggly fiddly things for access/coolant/flux ports
		if(!world.isRemote && (isItemPort(metadata) || isFluidPort(metadata) || isFluxPort(metadata))) {
			if(StaticUtils.Inventory.isPlayerHoldingWrench(player)) {
				if(te instanceof TileEntityMultiblockFluidPort) {
					TileEntityMultiblockFluidPort cp = (TileEntityMultiblockFluidPort)te;
					cp.setInlet(!cp.isInlet(), true);
					return true;
				}
				else if(te instanceof TileEntityMultiblockItemPort) {
					TileEntityMultiblockItemPort cp = (TileEntityMultiblockItemPort)te;
					cp.setInlet(!cp.isInlet());
					return true;
				}
				else if(te instanceof TileEntityMultiblockFluxPort) {
					TileEntityMultiblockFluxPort cp = (TileEntityMultiblockFluxPort)te;
					cp.setInlet(!cp.isInlet(), true);
					//LogHelper.info("BMbP - Toggling FluxPort");
					return true;
				}
			}
			else if(isFluidPort(metadata) || isFluxPort(metadata)) {
				return false;
			}
		}
		
		// Don't open the controller GUI if the reactor isn't assembled
		if(isController(metadata) && (master == null || !master.isAssembled())) {LogHelper.info("BMbP - This block's master is " + master); return false; }
		//LogHelper.info("BMbP - Clicked on Controller");
		if(!world.isRemote) {
			player.openGui(BigRF.instance, 0, world, x, y, z);
			LogHelper.info("BMbP - Opening Gui");
		}
		return true;
	}
		


	@Override
	public boolean isOpaqueCube()
	{
		return true;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return true;
	}
	
	@Override
	public int damageDropped(int metadata)
	{
		return metadata;
	}
		
	public ItemStack getMultiblockFrameItemStack() {
		return new ItemStack(this, 1, METADATA_FRAME);
	}
	
	public ItemStack getMultiblockFluxPortItemStack() {
		return new ItemStack(this, 1, METADATA_FLUXPORT);
	}	
	
	public ItemStack getMultiblockItemPortItemStack() {
		return new ItemStack(this, 1, METADATA_ITEMPORT);
	}
	
	public ItemStack getMultiblockFluidPortItemStack() {
		return new ItemStack(this, 1, METADATA_FLUIDPORT);
	}

	public ItemStack getMultiblockEnergyCellControllerItemStack() {
		return new ItemStack(this, 1, METADATA_ENERGYCELL_CONTROLLER);
	}	

	public ItemStack getMultiblockBoilerControllerItemStack() {
		return new ItemStack(this, 1, METADATA_BOILER_CONTROLLER);
	}	

	public ItemStack getMultiblockTurbineControllerItemStack() {
		return new ItemStack(this, 1, METADATA_TURBINE_CONTROLLER);
	}	

	public ItemStack getMultiblockReactorControllerItemStack() {
		return new ItemStack(this, 1, METADATA_REACTOR_CONTROLLER);
	}	
	
	@Override
	public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		for(int metadata = 0; metadata < _subBlocks.length; metadata++) {
			par3List.add(new ItemStack(this, 1, metadata));
		}
	}
	
	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta)
	{
		// Drop everything inside inventory blocks
		TileEntity te = world.getTileEntity(x, y, z);
		if(te instanceof IInventory)
		{
			IInventory inventory = ((IInventory)te);
inv:		for(int i = 0; i < inventory.getSizeInventory(); i++)
			{
				ItemStack itemstack = inventory.getStackInSlot(i);
				if(itemstack == null)
				{
					continue;
				}
				float xOffset = world.rand.nextFloat() * 0.8F + 0.1F;
				float yOffset = world.rand.nextFloat() * 0.8F + 0.1F;
				float zOffset = world.rand.nextFloat() * 0.8F + 0.1F;
				do
				{
					if(itemstack.stackSize <= 0)
					{
						continue inv;
					}
					int amountToDrop = world.rand.nextInt(21) + 10;
					if(amountToDrop > itemstack.stackSize)
					{
						amountToDrop = itemstack.stackSize;
					}
					itemstack.stackSize -= amountToDrop;
					EntityItem entityitem = new EntityItem(world, (float)x + xOffset, (float)y + yOffset, (float)z + zOffset, new ItemStack(itemstack.getItem(), amountToDrop, itemstack.getItemDamage()));
					if(itemstack.getTagCompound() != null)
					{
						entityitem.getEntityItem().setTagCompound(itemstack.getTagCompound());
					}
					float motionMultiplier = 0.05F;
					entityitem.motionX = (float)world.rand.nextGaussian() * motionMultiplier;
					entityitem.motionY = (float)world.rand.nextGaussian() * motionMultiplier + 0.2F;
					entityitem.motionZ = (float)world.rand.nextGaussian() * motionMultiplier;
					world.spawnEntityInWorld(entityitem);
				} while(true);
			}
		}

		super.breakBlock(world, x, y, z, block, meta);
	}
	
	@Override
    public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z)
    {
		return false;
    }
	
	
	//// UGLY UI CODE HERE ////
	private IIcon getFluidPortIcon(IBlockAccess blockAccess, int x, int y,
			int z, int side) {
		TileEntity te = blockAccess.getTileEntity(x, y, z);
		if(te instanceof TileEntityMultiblockFluidPort) {
			TileEntityMultiblockFluidPort port = (TileEntityMultiblockFluidPort)te;
			
			if(!isMultiblockAssembled(port) || isOutwardsSide(port, side)) {
				if(port.isInlet()) {
					return _icons[METADATA_FLUIDPORT][PORT_IN];
				}
				else {
					return _icons[METADATA_FLUIDPORT][PORT_OUT];
				}
			}
		}
		return blockIcon;
	}

	private IIcon getItemPortIcon(IBlockAccess blockAccess, int x, int y,
			int z, int side) {
		TileEntity te = blockAccess.getTileEntity(x, y, z);
		if(te instanceof TileEntityMultiblockItemPort) {
			TileEntityMultiblockItemPort port = (TileEntityMultiblockItemPort)te;

			if(!isMultiblockAssembled(port) || isOutwardsSide(port, side)) {
				if(port.isInlet()) {
					return _icons[METADATA_ITEMPORT][PORT_IN];
				}
				else {
					return _icons[METADATA_ITEMPORT][PORT_OUT];
				}
			}
		}
		return blockIcon;
	}

	private IIcon getFluxPortIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
		TileEntity te = blockAccess.getTileEntity(x, y, z);
		if(te instanceof TileEntityMultiblockFluxPort) {
			TileEntityMultiblockFluxPort port = (TileEntityMultiblockFluxPort)te;
			
			if(!isMultiblockAssembled(port) || isOutwardsSide(port, side)) {
				if(port.isInlet()) {
					return _icons[METADATA_FLUXPORT][PORT_IN];
				}
				else if(!port.isInlet()) {
					return _icons[METADATA_FLUXPORT][PORT_OUT];
				}
				else {
					return _icons[METADATA_FLUXPORT][PORT_OFF];
				}
			}
		}
		return blockIcon;
	}

	//TODO make sure it works for all master types
	private IIcon getControllerIcon(IBlockAccess blockAccess, int x, int y,
			int z, int side) {
		TileEntity te = blockAccess.getTileEntity(x, y, z);
		int METADATA_CONTROLLER = blockAccess.getBlockMetadata(x,y,z);;
		if(te instanceof TileEntityMultiblockPart) {
			TileEntityMultiblockPart part = (TileEntityMultiblockPart)te;
			MultiblockMasterBase master = part.getMultiblockMaster();

			//TODO set it up for multiple multiblock types
			if(master == null || !master.isAssembled()) {
				return _icons[METADATA_CONTROLLER][CONTROLLER_OFF];
			}
			else if(!isOutwardsSide(part, side)) {
				return blockIcon;
			}
			else if(master.getActive()) {
				return _icons[METADATA_CONTROLLER][CONTROLLER_ON];
			}
			else {
				return _icons[METADATA_CONTROLLER][CONTROLLER_IDLE];
			}
		}
		return blockIcon;
	}

	private static final int DEFAULT = 0;
	private static final int FACE = 1;
	private static final int CORNER = 2;
	private static final int EASTWEST = 3;
	private static final int NORTHSOUTH = 4;
	private static final int VERTICAL = 5;
	
	private IIcon getFrameIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
		TileEntity te = blockAccess.getTileEntity(x, y, z);
		if(te instanceof TileEntityMultiblockPart) {
			TileEntityMultiblockPart part = (TileEntityMultiblockPart)te;
			PartPosition position = part.getPartPosition();
			MultiblockMasterBase master = part.getMultiblockMaster();
			
			if(master == null || !master.isAssembled()) {
				return _icons[METADATA_FRAME][DEFAULT];
			}
			
			switch(position) {
			case BottomFace:
			case TopFace:
			case EastFace:
			case WestFace:
			case NorthFace:
			case SouthFace:
				return _icons[METADATA_FRAME][FACE];
			case FrameCorner:
				return _icons[METADATA_FRAME][CORNER];
			case Frame:
				return getFrameEdgeIcon(part, master, side);
			case Interior:
			case Unknown:
			default:
				return _icons[METADATA_FRAME][DEFAULT];
			}
		}
		return _icons[METADATA_FRAME][DEFAULT];
	}
	
	private IIcon getFrameEdgeIcon(TileEntityMultiblockPart part, MultiblockMasterBase master, int side) {
		if(master == null || !master.isAssembled()) { return _icons[METADATA_FRAME][DEFAULT]; }

		CoordTriplet minCoord = master.getMinimumCoord();
		CoordTriplet maxCoord = master.getMaximumCoord();

		boolean xExtreme, yExtreme, zExtreme;
		xExtreme = yExtreme = zExtreme = false;

		if(part.xCoord == minCoord.x || part.xCoord == maxCoord.x) { xExtreme = true; }
		if(part.yCoord == minCoord.y || part.yCoord == maxCoord.y) { yExtreme = true; }
		if(part.zCoord == minCoord.z || part.zCoord == maxCoord.z) { zExtreme = true; }
		
		int idx = DEFAULT;
		if(!xExtreme) {
			if(side < 4) { idx = EASTWEST; }
		}
		else if(!yExtreme) {
			if(side > 1) {
				idx = VERTICAL;
			}
		}
		else { // !zExtreme
			if(side < 2) {
				idx = NORTHSOUTH;
			}
			else if(side > 3) {
				idx = EASTWEST;
			}
		}
		return _icons[METADATA_FRAME][idx];
	}
	
	private IIcon getFaceOrBlockIcon(IBlockAccess blockAccess, int x, int y, int z, int side, int metadata) {
		TileEntity te = blockAccess.getTileEntity(x, y, z);
		if(te instanceof TileEntityMultiblockPart) {
			TileEntityMultiblockPart part = (TileEntityMultiblockPart)te;
			if(!isMultiblockAssembled(part) || isOutwardsSide(part, side)) {
				return _icons[metadata][0];
			}
		}
		return this.blockIcon;
	}
		

	/**
	 * @param part The part whose sides we're checking
	 * @param side The side to compare to the part
	 * @return True if `side` is the outwards-facing face of `part`
	 */
	private boolean isOutwardsSide(TileEntityMultiblockPart part, int side) {
		ForgeDirection outDir = part.getOutwardsDir();
		return outDir.ordinal() == side;
	}
	
	private boolean isMultiblockAssembled(TileEntityMultiblockPart part) {
		MultiblockMasterBase multiblock = part.getMultiblockMaster();
		return multiblock != null && multiblock.isAssembled();
	}
	
}
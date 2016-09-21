package com.dora_goon.bigrf.block;

import com.dora_goon.bigrf.reference.CreativeTabBigRF;
import com.dora_goon.bigrf.reference.Reference;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;

public class BlockBigRF extends Block {
	public BlockBigRF(Material material){
		super(material);
		this.setCreativeTab(CreativeTabBigRF.BigRF_TAB);
	}
	
	public BlockBigRF(){
		this(Material.iron);
	}
	
	@Override
	public String getUnlocalizedName(){
		return String.format("tile.%s%s", Reference.Mod_ID.toLowerCase() + ":", getUnwrappedUnlocalizedName(super.getUnlocalizedName()));
	}
	/*	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister){
		blockIcon = iconRegister.registerIcon(this.getUnlocalizedName().substring(this.getUnlocalizedName().indexOf(".") + 1));
	}
	*/
	protected String getUnwrappedUnlocalizedName(String unlocalizedName){
		return unlocalizedName.substring(unlocalizedName.indexOf(".") + 1);
	}
	
}

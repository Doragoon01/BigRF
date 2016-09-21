package com.dora_goon.bigrf.integration;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.ItemStack;

public abstract class TEStuff {
    public static ItemStack powerCoilElectrum = null;
    public static ItemStack powerCoilGold = null;
    
    public static void init() {
        powerCoilElectrum = GameRegistry.findItemStack("ThermalExpansion", "powerCoilElectrum", 1);
        powerCoilGold = GameRegistry.findItemStack("ThermalExpansion", "powerCoilGold", 1);
    }
}

package mekanism.common.item.block.machine;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.client.MekanismClient;
import mekanism.common.Upgrade;
import mekanism.common.base.IRedstoneControl.RedstoneControl;
import mekanism.common.base.ISustainedInventory;
import mekanism.common.block.machine.BlockLogisticalSorter;
import mekanism.common.item.block.ItemBlockAdvancedTooltip;
import mekanism.common.security.ISecurityItem;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockLogisticalSorter extends ItemBlockAdvancedTooltip implements ISustainedInventory, ISecurityItem {

    public ItemBlockLogisticalSorter(BlockLogisticalSorter block) {
        super(block);
        setMaxStackSize(1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addDetails(@Nonnull ItemStack itemstack, World world, @Nonnull List<String> list, @Nonnull ITooltipFlag flag) {
        list.add(SecurityUtils.getOwnerDisplay(Minecraft.getMinecraft().player, MekanismClient.clientUUIDMap.get(getOwnerUUID(itemstack))));
        list.add(EnumColor.GREY + LangUtils.localize("gui.security") + ": " + SecurityUtils.getSecurityDisplay(itemstack, Side.CLIENT));
        if (SecurityUtils.isOverridden(itemstack, Side.CLIENT)) {
            list.add(EnumColor.RED + "(" + LangUtils.localize("gui.overridden") + ")");
        }
        if (ItemDataUtils.hasData(itemstack, "upgrades")) {
            Map<Upgrade, Integer> upgrades = Upgrade.buildMap(ItemDataUtils.getDataMap(itemstack));
            for (Entry<Upgrade, Integer> entry : upgrades.entrySet()) {
                list.add(entry.getKey().getColor() + "- " + entry.getKey().getName() + (entry.getKey().canMultiply() ? ": " + EnumColor.GREY + "x" + entry.getValue() : ""));
            }
        }
    }

    @Override
    public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, World world, @Nonnull BlockPos pos, EnumFacing side, float hitX, float hitY,
          float hitZ, @Nonnull IBlockState state) {
        if (super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, state)) {
            TileEntityLogisticalSorter tile = (TileEntityLogisticalSorter) world.getTileEntity(pos);
            if (tile != null) {
                //Security
                tile.getSecurity().setOwnerUUID(getOwnerUUID(stack));
                tile.getSecurity().setMode(getSecurity(stack));
                if (getOwnerUUID(stack) == null) {
                    tile.getSecurity().setOwnerUUID(player.getUniqueID());
                }
                //Upgrades
                if (ItemDataUtils.hasData(stack, "upgrades")) {
                    tile.getComponent().read(ItemDataUtils.getDataMap(stack));
                }
                //Sustained Data
                if (stack.getTagCompound() != null) {
                    tile.readSustainedData(stack);
                }
                //Redstone Control
                if (ItemDataUtils.hasData(stack, "controlType")) {
                    tile.setControlType(RedstoneControl.values()[ItemDataUtils.getInt(stack, "controlType")]);
                }
                //Sustained Inventory
                tile.setInventory(getInventory(stack));
            }
            return true;
        }
        return false;
    }

    @Override
    public void setInventory(NBTTagList nbtTags, Object... data) {
        if (data[0] instanceof ItemStack) {
            ItemDataUtils.setList((ItemStack) data[0], "Items", nbtTags);
        }
    }

    @Override
    public NBTTagList getInventory(Object... data) {
        if (data[0] instanceof ItemStack) {
            return ItemDataUtils.getList((ItemStack) data[0], "Items");
        }
        return null;
    }
}
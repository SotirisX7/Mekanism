package mekanism.common.capabilities.energy;

import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.annotations.FieldsAreNonnullByDefault;
import mekanism.api.annotations.NonNull;
import mekanism.api.block.IBlockElectric;
import mekanism.api.inventory.AutomationType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;

@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineEnergyContainer extends BasicEnergyContainer {

    public static MachineEnergyContainer input(TileEntityMekanism tile) {
        Block block = tile.getBlockType().getBlock();
        if (!(block instanceof IBlockElectric)) {
            throw new IllegalArgumentException("Block provider must be an electric block");
        }
        Objects.requireNonNull(tile, "Tile cannot be null");
        IBlockElectric electricBlock = (IBlockElectric) block;
        return new MachineEnergyContainer(electricBlock.getStorage(), electricBlock.getUsage(), notExternal, alwaysTrue, tile);
    }

    private final TileEntityMekanism upgradeTile;
    private final double baseEnergyPerTick;
    private double currentMaxEnergy;
    private double currentEnergyPerTick;

    protected MachineEnergyContainer(double maxEnergy, double energyPerTick, Predicate<@NonNull AutomationType> canExtract, Predicate<@NonNull AutomationType> canInsert,
          TileEntityMekanism tile) {
        super(maxEnergy, canExtract, canInsert, tile);
        this.baseEnergyPerTick = energyPerTick;
        this.upgradeTile = tile;
        //Make sure we have the proper max energy set initially
        updateMaxEnergy();
        updateEnergyPerTick();
    }

    @Override
    public double getMaxEnergy() {
        return currentMaxEnergy;
    }

    public double getBaseMaxEnergy() {
        return super.getMaxEnergy();
    }

    public void setMaxEnergy(double maxEnergy) {
        if (maxEnergy < 0) {
            throw new IllegalArgumentException("Max energy must be at least zero");
        }
        this.currentMaxEnergy = maxEnergy;
        if (getEnergy() > getMaxEnergy()) {
            setEnergy(getMaxEnergy());
        }
    }

    public double getEnergyPerTick() {
        return currentEnergyPerTick;
    }

    public double getBaseEnergyPerTick() {
        return baseEnergyPerTick;
    }

    public void setEnergyPerTick(double energyPerTick) {
        if (energyPerTick < 0) {
            throw new IllegalArgumentException("Energy must be at least zero");
        }
        this.currentEnergyPerTick = energyPerTick;
    }

    public void updateMaxEnergy() {
        //TODO: Do we want to validate it actually supports energy upgrades?
        setMaxEnergy(MekanismUtils.getMaxEnergy(upgradeTile, getBaseMaxEnergy()));
    }

    public void updateEnergyPerTick() {
        //TODO: Do we want to validate it actually supports energy or speed upgrades?
        setEnergyPerTick(MekanismUtils.getEnergyPerTick(upgradeTile, baseEnergyPerTick));
    }
}
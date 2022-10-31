package earth.terrarium.ad_astra.client.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import earth.terrarium.ad_astra.blocks.machines.entity.CryoFreezerBlockEntity;
import earth.terrarium.ad_astra.screen.handler.CryoFreezerScreenHandler;
import earth.terrarium.ad_astra.util.ModResourceLocation;
import java.awt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CryoFreezerScreen extends AbstractMachineScreen<CryoFreezerBlockEntity, CryoFreezerScreenHandler> {

    public static final int SNOWFLAKE_LEFT = 54;
    public static final int SNOWFLAKE_TOP = 71;
    public static final int INPUT_TANK_LEFT = 85;
    public static final int INPUT_TANK_TOP = 37;
    public static final int ENERGY_LEFT = 149;
    public static final int ENERGY_TOP = 27;
    private static final ResourceLocation TEXTURE = new ModResourceLocation("textures/gui/screens/cryo_freezer.png");

    public CryoFreezerScreen(CryoFreezerScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title, TEXTURE);
        this.imageWidth = 177;
        this.imageHeight = 181;
        this.inventoryLabelY = this.imageHeight - 93;
    }

    @Override
    protected void renderBg(PoseStack matrices, float delta, int mouseX, int mouseY) {

        super.renderBg(matrices, delta, mouseX, mouseY);

        GuiUtil.drawEnergy(matrices, this.leftPos + ENERGY_LEFT, this.topPos + ENERGY_TOP, this.menu.getEnergyAmount(), this.machine.getMaxCapacity());
        GuiUtil.drawFluidTank(matrices, this.leftPos + INPUT_TANK_LEFT, this.topPos + INPUT_TANK_TOP, this.machine.getFluidContainer().getTankCapacity(0), this.menu.getFluids().get(0));
        GuiUtil.drawSnowflake(matrices, this.leftPos + SNOWFLAKE_LEFT, this.topPos + SNOWFLAKE_TOP, this.machine.getCookTime(), this.machine.getCookTimeTotal());
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        if (GuiUtil.isHovering(this.getEnergyBounds(), mouseX, mouseY)) {
            GuiUtil.drawEnergyTooltip(this, matrices, this.menu.getEnergyAmount(), this.machine.getMaxCapacity(), mouseX, mouseY);
        }

        if (GuiUtil.isHovering(this.getOutputTankBounds(), mouseX, mouseY)) {
            GuiUtil.drawTankTooltip(this, matrices, this.menu.getFluids().get(0), this.machine.getFluidContainer().getTankCapacity(0), mouseX, mouseY);
        }
    }

    public Rectangle getOutputTankBounds() {
        return GuiUtil.getFluidTankBounds(this.leftPos + INPUT_TANK_LEFT, this.topPos + INPUT_TANK_TOP);
    }

    public Rectangle getEnergyBounds() {
        return GuiUtil.getEnergyBounds(this.leftPos + ENERGY_LEFT, this.topPos + ENERGY_TOP);
    }

    @Override
    public int getTextColour() {
        return 0xccffff;
    }
}
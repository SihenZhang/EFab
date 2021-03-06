package mcjty.efab.sound;

import net.minecraft.block.Block;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EFabSound extends MovingSound {

    private int ticksRemaining;

    public EFabSound(SoundEvent event, World world, BlockPos pos, float baseVolume, int ticks) {
        super(event, SoundCategory.BLOCKS);
        this.world = world;
        this.pos = pos;
        this.xPosF = pos.getX();
        this.yPosF = pos.getY();
        this.zPosF = pos.getZ();
        this.attenuationType = AttenuationType.LINEAR;
        this.repeat = true;
        this.repeatDelay = 0;
        this.sound = event;
        this.volume = baseVolume;
        ticksRemaining = ticks;
    }

    private final World world;
    private final BlockPos pos;
    private final SoundEvent sound;

    @Override
    public void update() {
        Block block = world.getBlockState(pos).getBlock();
        if (!(block instanceof ISoundProducer)) {
            donePlaying = true;
            return;
        }
        if (ticksRemaining != -1) {
            ticksRemaining--;
            if (ticksRemaining <= 0) {
                donePlaying = true;
            }
        }
    }

    protected boolean isSoundType(SoundEvent event){
        return sound == event;
    }

}

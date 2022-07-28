package cofh.redstonearsenal.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.ObjectHolder;

import static cofh.lib.util.constants.Constants.ID_REDSTONE_ARSENAL;
import static cofh.redstonearsenal.RedstoneArsenal.SOUND_EVENTS;

public class RSASounds {

    private RSASounds() {

    }

    public static void register() {

        registerSound(ID_SOUND_SHIELDING_BREAK);
        registerSound(ID_SOUND_SHIELDING_RECHARGE);
    }

    public static void registerSound(String soundID) {

        SOUND_EVENTS.register(soundID, () -> new SoundEvent(new ResourceLocation(soundID)));
    }

    // region IDs
    public static final String ID_SOUND_SHIELDING_BREAK = ID_REDSTONE_ARSENAL + ":shielding.break";
    public static final String ID_SOUND_SHIELDING_RECHARGE = ID_REDSTONE_ARSENAL + ":shielding.recharge";
    // endregion

    // region REFERENCES
    @ObjectHolder(ID_SOUND_SHIELDING_BREAK)
    public static final SoundEvent SOUND_SHIELDING_BREAK = null;
    @ObjectHolder(ID_SOUND_SHIELDING_RECHARGE)
    public static final SoundEvent SOUND_SHIELDING_RECHARGE = null;
    // endregion{
}

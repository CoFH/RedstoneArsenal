package cofh.redstonearsenal.init.registries;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;

import static cofh.lib.util.constants.ModIds.ID_REDSTONE_ARSENAL;
import static cofh.redstonearsenal.RedstoneArsenal.SOUND_EVENTS;

public class ModSounds {

    private ModSounds() {

    }

    public static void register() {

    }

    public static RegistryObject<SoundEvent> registerSound(String soundID) {

        return SOUND_EVENTS.register(soundID, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ID_REDSTONE_ARSENAL, soundID)));
    }

    // region IDs
    public static final String ID_SOUND_SHIELDING_BREAK = "shielding.break";
    public static final String ID_SOUND_SHIELDING_RECHARGE = "shielding.recharge";
    public static final String ID_SOUND_EMPOWER = "empower.on";
    public static final String ID_SOUND_QUELL = "empower.off";
    // endregion

    public static final RegistryObject<SoundEvent> SOUND_SHIELDING_BREAK = registerSound(ID_SOUND_SHIELDING_BREAK);
    public static final RegistryObject<SoundEvent> SOUND_SHIELDING_RECHARGE = registerSound(ID_SOUND_SHIELDING_RECHARGE);
    public static final RegistryObject<SoundEvent> SOUND_EMPOWER = registerSound(ID_SOUND_EMPOWER);
    public static final RegistryObject<SoundEvent> SOUND_QUELL = registerSound(ID_SOUND_QUELL);

}

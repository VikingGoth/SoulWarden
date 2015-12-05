package vikinggoth.soulwarden.proxies;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import vikinggoth.soulwarden.registries.BlockRenderRegister;

/**
 * Created by Friedrich on 8/18/2015.
 */
public class ClientProxy extends CommonProxy{

    public void preInit(FMLPreInitializationEvent e)
    {
        super.preInit(e);

        BlockRenderRegister.preInit();
    }

    public void init(FMLInitializationEvent e)
    {
        super.init(e);

        //ItemRenderRegister.
        BlockRenderRegister.registerBlockRenderer();
    }

    public void postInit(FMLPostInitializationEvent e)
    {
        super.postInit(e);
    }
}
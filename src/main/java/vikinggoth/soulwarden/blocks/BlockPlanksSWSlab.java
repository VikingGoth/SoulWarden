package vikinggoth.soulwarden.blocks;

import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vikinggoth.soulwarden.SoulWarden;
import vikinggoth.soulwarden.registries.ConfigBlocks;

import java.util.List;
import java.util.Random;

/**
 * Created by Friedrich on 11/12/2015.
 */
public abstract class BlockPlanksSWSlab extends BlockSlab
{
    private static final PropertyEnum VARIANT = PropertyEnum.create("variant", ConfigBlocks.WoodType.class);

    public BlockPlanksSWSlab(Material materialIn)
    {
        super(materialIn);
        IBlockState blockState = this.blockState.getBaseState();
        if(!this.isDouble())
        {
            blockState = blockState.withProperty(HALF, EnumBlockHalf.BOTTOM);
        }
        this.setDefaultState(blockState.withProperty(VARIANT, ConfigBlocks.WoodType.GHOUL));
        this.setCreativeTab(SoulWarden.SWTab);
        this.useNeighborBrightness = !this.isDouble();
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return Item.getItemFromBlock(ConfigBlocks.soulStoneSlab);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Item getItem(World worldIn, BlockPos pos)
    {
        return Item.getItemFromBlock(ConfigBlocks.planksSWSlab);
    }

    @Override
    public String getUnlocalizedName(int meta)
    {
        return super.getUnlocalizedName() + "." + ConfigBlocks.WoodType.byMetadata(meta).getUnlocalizedName();
    }

    @Override
    public IProperty getVariantProperty()
    {
        return this.VARIANT;
    }

    @Override
    public Object getVariant(ItemStack stack)
    {
        return ConfigBlocks.WoodType.byMetadata(stack.getMetadata() & 7);
    }

    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List list)
    {
        if (itemIn != Item.getItemFromBlock(ConfigBlocks.planksSWSlabDouble))
        {
            ConfigBlocks.WoodType[] aenumtype = ConfigBlocks.WoodType.values();
            int i = aenumtype.length;

            for (int j = 0; j < i; ++j) {
                ConfigBlocks.WoodType enumtype = aenumtype[j];
                list.add(new ItemStack(itemIn, 1, enumtype.getMetadata()));
            }
        }
    }

    @Override
    public IBlockState getStateFromMeta(final int meta)
    {
        IBlockState iblockstate = this.getDefaultState().withProperty(VARIANT, ConfigBlocks.WoodType.byMetadata(meta & 7));

        if (!this.isDouble())
        {
            iblockstate = iblockstate.withProperty(HALF, (meta & 8) == 0 ? EnumBlockHalf.BOTTOM : EnumBlockHalf.TOP);
        }

        return iblockstate;
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        byte b0 = 0;
        int i = b0 | ((ConfigBlocks.WoodType)state.getValue(VARIANT)).getMetadata();

        if (!this.isDouble() && state.getValue(HALF) == EnumBlockHalf.TOP)
        {
            i |= 8;
        }

        return i;
    }

    @Override
    protected BlockState createBlockState()
    {
        return this.isDouble() ? new BlockState(this, new IProperty[] {VARIANT}): new BlockState(this, new IProperty[] {HALF, VARIANT});
    }

    @Override
    public int damageDropped(final IBlockState state) {
        return ((ConfigBlocks.WoodType)state.getValue(VARIANT)).getMetadata();
    }
}

package vikinggoth.soulwarden.world.dimension;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ReportedException;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeCache;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vikinggoth.soulwarden.world.gen.layer.GenLayerStygia;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WorldChunkManagerStygia extends WorldChunkManager {

    private GenLayer genBiomes;
    /** A GenLayer containing the indices into BiomeGenBase.biomeList[] */
    private GenLayer biomeIndexLayer;
    /** The BiomeCache object for this world. */
    private BiomeCache biomeCache;
    /** A list of biomes that the player can spawn in. */
    private List<BiomeGenBase> biomesToSpawnIn;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public WorldChunkManagerStygia()
    {
        this.biomeCache = new BiomeCache(this);
        this.biomesToSpawnIn = new ArrayList();
        this.biomesToSpawnIn.addAll(allowedBiomes);
    }

    public WorldChunkManagerStygia(long seed, WorldType worldType)
    {
        this();
        // i changed this to my GenLayerTutorial
        GenLayer[] agenlayer = GenLayerStygia.makeTheWorld(seed, worldType);
        agenlayer = getModdedBiomeGenerators(worldType, seed, agenlayer);
        this.genBiomes = agenlayer[0];
        this.biomeIndexLayer = agenlayer[1];
    }

    public WorldChunkManagerStygia(World world)
    {
        this(world.getSeed(), world.getWorldInfo().getTerrainType());
    }

    /**
     * Gets the list of valid biomes for the player to spawn in.
     */
    @Override
    public List<BiomeGenBase> getBiomesToSpawnIn()
    {
        return this.biomesToSpawnIn;
    }

    /**
     * Returns a list of rainfall values for the specified blocks. Args: listToReuse, x, z, width, length.
     */
    @Override
    public float[] getRainfall(float[] listToReuse, int x, int z, int width, int length)
    {
        IntCache.resetIntCache();

        if (listToReuse == null || listToReuse.length < width * length)
        {
            listToReuse = new float[width * length];
        }

        int[] aint = this.biomeIndexLayer.getInts(x, z, width, length);

        for (int i1 = 0; i1 < width * length; ++i1)
        {
            try
            {
                float f = (float)BiomeGenBase.getBiome(aint[i1]).getIntRainfall() / 65536.0F;

                if (f > 1.0F)
                {
                    f = 1.0F;
                }

                listToReuse[i1] = f;
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Invalid Biome id");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("DownfallBlock");
                crashreportcategory.addCrashSection("biome id", Integer.valueOf(i1));
                crashreportcategory.addCrashSection("downfalls[] size", Integer.valueOf(listToReuse.length));
                crashreportcategory.addCrashSection("x", Integer.valueOf(x));
                crashreportcategory.addCrashSection("z", Integer.valueOf(z));
                crashreportcategory.addCrashSection("w", Integer.valueOf(width));
                crashreportcategory.addCrashSection("h", Integer.valueOf(length));
                throw new ReportedException(crashreport);
            }
        }

        return listToReuse;
    }

    /**
     * Return an adjusted version of a given temperature based on the y height
     */
    @Override
    @SideOnly(Side.CLIENT)
    public float getTemperatureAtHeight(float par1, int par2)
    {
        return par1;
    }

    /**
     * Returns an array of biomes for the location input.
     */
    @Override
    public BiomeGenBase[] getBiomesForGeneration(BiomeGenBase[] par1ArrayOfBiomeGenBase, int par2, int par3, int par4, int par5)
    {
        IntCache.resetIntCache();

        if (par1ArrayOfBiomeGenBase == null || par1ArrayOfBiomeGenBase.length < par4 * par5)
        {
            par1ArrayOfBiomeGenBase = new BiomeGenBase[par4 * par5];
        }

        int[] aint = this.genBiomes.getInts(par2, par3, par4, par5);

        try
        {
            for (int i = 0; i < par4 * par5; ++i)
            {
                par1ArrayOfBiomeGenBase[i] = BiomeGenBase.getBiome(aint[i]);
            }

            return par1ArrayOfBiomeGenBase;
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Invalid Biome id");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("RawBiomeBlock");
            crashreportcategory.addCrashSection("biomes[] size", Integer.valueOf(par1ArrayOfBiomeGenBase.length));
            crashreportcategory.addCrashSection("x", Integer.valueOf(par2));
            crashreportcategory.addCrashSection("z", Integer.valueOf(par3));
            crashreportcategory.addCrashSection("w", Integer.valueOf(par4));
            crashreportcategory.addCrashSection("h", Integer.valueOf(par5));
            throw new ReportedException(crashreport);
        }
    }

    /**
     * Returns biomes to use for the blocks and loads the other data like temperature and humidity onto the
     * WorldChunkManager Args: oldBiomeList, x, z, width, depth
     */
    @Override
    public BiomeGenBase[] loadBlockGeneratorData(BiomeGenBase[] oldBiomeList, int x, int z, int width, int depth)
    {
        return this.getBiomeGenAt(oldBiomeList, x, z, width, depth, true);
    }

    /**
     * Return a list of biomes for the specified blocks. Args: listToReuse, x, y, width, length, cacheFlag (if false,
     * don't check biomeCache to avoid infinite loop in BiomeCacheBlock)
     */
    @Override
    public BiomeGenBase[] getBiomeGenAt(BiomeGenBase[] listToReuse, int x, int y, int width, int length, boolean cacheFlag)
    {
        IntCache.resetIntCache();

        if (listToReuse == null || listToReuse.length < width * length)
        {
            listToReuse = new BiomeGenBase[width * length];
        }

        if (cacheFlag && width == 16 && length == 16 && (x & 15) == 0 && (y & 15) == 0)
        {
            BiomeGenBase[] abiomegenbase1 = this.biomeCache.getCachedBiomes(x, y);
            System.arraycopy(abiomegenbase1, 0, listToReuse, 0, width * length);
            return listToReuse;
        }
        else
        {
            int[] aint = this.biomeIndexLayer.getInts(x, y, width, length);

            for (int i = 0; i < width * length; ++i)
            {
                listToReuse[i] = BiomeGenBase.getBiome(aint[i]);
            }
            return listToReuse;
        }
    }

    /**
     * checks given Chunk's Biomes against List of allowed ones
     */
    @Override
    @SuppressWarnings("rawtypes")
    public boolean areBiomesViable(int x, int y, int z, List par4List)
    {
        IntCache.resetIntCache();
        int l = x - z >> 2;
        int i1 = y - z >> 2;
        int j1 = x + z >> 2;
        int k1 = y + z >> 2;
        int l1 = j1 - l + 1;
        int i2 = k1 - i1 + 1;
        int[] aint = this.genBiomes.getInts(l, i1, l1, i2);

        try
        {
            for (int j2 = 0; j2 < l1 * i2; ++j2)
            {
                BiomeGenBase biomegenbase = BiomeGenBase.getBiome(aint[j2]);

                if (!par4List.contains(biomegenbase))
                {
                    return false;
                }
            }

            return true;
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Invalid Biome id");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Layer");
            crashreportcategory.addCrashSection("Layer", this.genBiomes.toString());
            crashreportcategory.addCrashSection("x", Integer.valueOf(x));
            crashreportcategory.addCrashSection("z", Integer.valueOf(y));
            crashreportcategory.addCrashSection("radius", Integer.valueOf(z));
            crashreportcategory.addCrashSection("allowed", par4List);
            throw new ReportedException(crashreport);
        }
    }

    @Override
    public BlockPos findBiomePosition(int x, int z, int range, List<BiomeGenBase> biomes, Random random)
    {
        IntCache.resetIntCache();
        int i = x - range >> 2;
        int j = z - range >> 2;
        int k = x + range >> 2;
        int l = z + range >> 2;
        int i1 = k - i + 1;
        int j1 = l - j + 1;
        int[] aint = this.genBiomes.getInts(i, j, i1, j1);
        BlockPos blockpos = null;
        int k1 = 0;

        for (int l1 = 0; l1 < i1 * j1; ++l1)
        {
            int i2 = i + l1 % i1 << 2;
            int j2 = j + l1 / i1 << 2;
            BiomeGenBase biomegenbase = BiomeGenBase.getBiome(aint[l1]);

            if (biomes.contains(biomegenbase) && (blockpos == null || random.nextInt(k1 + 1) == 0))
            {
                blockpos = new BlockPos(i2, 0, j2);
                ++k1;
            }
        }

        return blockpos;
    }

    /**
     * Calls the WorldChunkManager's biomeCache.cleanupCache()
     */
    @Override
    public void cleanupCache()
    {
        this.biomeCache.cleanupCache();
    }
}
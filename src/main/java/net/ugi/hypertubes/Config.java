package net.ugi.hypertubes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = HyperTubes.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue MAX_HYPERTUBE_LENGTH = BUILDER
            .comment("Maximum survival/adventure build length for hypertubes")
            .defineInRange("maxHypertubeLength", 128, 0, Integer.MAX_VALUE);

    //creativeBypassMaxBuildLength? (default true)

    private static final ModConfigSpec.IntValue HYPERTUBE_PLACE_REACH = BUILDER
            .comment("The max reach for placing hypertubes")
            .defineInRange("hypertubePlaceReach", 15, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue UNBOOSTED_HYPERTUBE_SPEED = BUILDER
            .comment("max speed for unboosted hypertubes in m/s")
            .defineInRange("unboostedSpeed", 10, 0, Float.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue MANUAL_ACCELERATION_STRENGTH = BUILDER
            .comment("manual acceleration strength for slowing down or speeding up in hypertubes")
            .defineInRange("manualAccelerationStrength", 0.05, 0, Float.MAX_VALUE);

    //max booster speed (tier 1)
    private static final ModConfigSpec.IntValue MAX_TIER_1_BOOST_SPEED = BUILDER
            .comment("Maximum speed that you can achieve with boosters in m/s")
            .defineInRange("maxTier1BoostSpeed", 25, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue TIER_1_BOOST_MULTIPLIER = BUILDER
            .comment("How much the speed multiplies with each booster")
            .defineInRange("tier1BoostMultiplier", 1.2, 1, 128);

    //enabled tube types?



    //booster Boosting multiplier

    //allow survival dismounting (default: true)




/*    private static final ModConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .define("logDirtBlock", true);

    public static final ModConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "The magic number is... ");

    // a list of strings that are treated as resource locations for items
    private static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("A list of items to log on common setup.")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), Config::validateItemName);*/

    static final ModConfigSpec SPEC = BUILDER.build();


    public static int maxHypertubeLength;
    public static int hypertubePlaceReach;
    public static double unBoostedSpeed;
    public static Double manualAccelerationStrength;
    public static int maxTier1BoostSpeed;
    public static double tier1BoostMultiplier;
/*    public static boolean logDirtBlock;

    public static String magicNumberIntroduction;
    public static Set<Item> items;*/

    private static boolean validateItemName(final Object obj)
    {
        return obj instanceof String itemName && BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        maxHypertubeLength = MAX_HYPERTUBE_LENGTH.get();
        hypertubePlaceReach = HYPERTUBE_PLACE_REACH.get();
        unBoostedSpeed = UNBOOSTED_HYPERTUBE_SPEED.get()/20;
        manualAccelerationStrength = MANUAL_ACCELERATION_STRENGTH.get();
        maxTier1BoostSpeed = MAX_TIER_1_BOOST_SPEED.get()/20;
        tier1BoostMultiplier = TIER_1_BOOST_MULTIPLIER.get();
/*        logDirtBlock = LOG_DIRT_BLOCK.get();*/

/*        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();*/

        // convert the list of strings into a set of items
/*        items = ITEM_STRINGS.get().stream()
                .map(itemName -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemName)))
                .collect(Collectors.toSet());*/
    }
}

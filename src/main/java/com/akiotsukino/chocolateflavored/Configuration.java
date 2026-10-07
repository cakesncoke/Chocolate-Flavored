package com.akiotsukino.chocolateflavored;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Configuration {
    public static final ModConfigSpec CLIENT_SPEC;
    public static final ModConfigSpec.BooleanValue ENABLE_COOKING_POT_RECIPE_BOOK;
    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        ENABLE_COOKING_POT_RECIPE_BOOK = builder.comment("Enable the Cooking Pot recipe book.")
                .define("enableCookingPotRecipeBook", true);
        CLIENT_SPEC = builder.build();
    }
    private Configuration() {}
}

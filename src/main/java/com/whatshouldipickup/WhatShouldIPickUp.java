package com.whatshouldipickup;

import com.whatshouldipickup.config.ClientConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(WhatShouldIPickUp.MODID)
public class WhatShouldIPickUp {

    public static final String MODID = "whatshouldipickup";

    public WhatShouldIPickUp(ModContainer container) {
        container.registerConfig(
                net.neoforged.fml.config.ModConfig.Type.CLIENT,
                ClientConfig.SPEC
        );
    }
}
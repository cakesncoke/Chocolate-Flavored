package com.akiotsukino.chocolateflavored.client;

import com.akiotsukino.chocolateflavored.echo.EchoLinkPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(modid="chocolateflavored",value=Dist.CLIENT)
public final class EchoShardClient {
    @SubscribeEvent public static void payloads(RegisterClientPayloadHandlersEvent event){event.register(EchoLinkPayload.TYPE,(payload,context)->EchoLinkPayload.apply(payload));}
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event){EchoLinkPayload.clearClient();}
    private EchoShardClient(){}
}

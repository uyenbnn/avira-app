package com.avira.commonlib.messaging;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessagingProviderFactory {

    private final MessagingProviderModule defaultModule;
    private final Map<MessagingProvider, MessagingProviderModule> modules;

    public MessagingProviderFactory(MessagingProviderModule defaultModule,
                                    List<MessagingProviderModule> availableModules) {
        this.defaultModule = defaultModule;
        this.modules = new HashMap<>();
        for (MessagingProviderModule module : availableModules) {
            this.modules.put(module.provider(), module);
        }
        this.modules.putIfAbsent(defaultModule.provider(), defaultModule);
    }

    public MessagingProviderModule resolve(MessagingProperties properties) {
        if (properties == null || !properties.isEnabled()) {
            return defaultModule;
        }

        MessagingProvider provider = properties.getProvider() == null
                ? MessagingProvider.NONE
                : properties.getProvider();

        return modules.getOrDefault(provider, defaultModule);
    }
}


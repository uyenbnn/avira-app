package com.avira.commonlib.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "avira.messaging")
public class MessagingProperties {

    private boolean enabled;
    private MessagingProvider provider = MessagingProvider.NONE;
    private String topicPrefix = "";
    private final RabbitStream rabbitStream = new RabbitStream();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public MessagingProvider getProvider() {
        return provider;
    }

    public void setProvider(MessagingProvider provider) {
        this.provider = provider;
    }

    public String getTopicPrefix() {
        return topicPrefix;
    }

    public void setTopicPrefix(String topicPrefix) {
        this.topicPrefix = topicPrefix;
    }

    public RabbitStream getRabbitStream() {
        return rabbitStream;
    }

    public String resolveTopicName(String domain) {
        if (topicPrefix == null || topicPrefix.isBlank()) {
            return domain;
        }
        return topicPrefix + domain;
    }

    public static class RabbitStream {

        private String host = "localhost";
        private int port = 5552;
        private String username = "guest";
        private String password = "guest";
        private String virtualHost = "/";

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getVirtualHost() {
            return virtualHost;
        }

        public void setVirtualHost(String virtualHost) {
            this.virtualHost = virtualHost;
        }
    }
}


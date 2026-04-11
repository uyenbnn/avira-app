package com.avira.platformservice.mvp.dto;

import java.util.Map;

import com.avira.platformservice.mvp.AuthMode;

public record ApplicationRequest(String name, String domain, AuthMode authMode, Map<String, Object> config) {
}

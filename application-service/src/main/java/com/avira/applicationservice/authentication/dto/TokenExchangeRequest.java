package com.avira.applicationservice.authentication.dto;

import com.avira.applicationservice.authentication.AuthMode;

public record TokenExchangeRequest(String subjectToken, AuthMode authMode) {
}

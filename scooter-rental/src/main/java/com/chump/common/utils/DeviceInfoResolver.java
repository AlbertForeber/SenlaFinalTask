package com.chump.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ua_parser.Client;
import ua_parser.Parser;

@Component
@RequiredArgsConstructor
public class DeviceInfoResolver {

    private final Parser parser;

    public String resolveIpAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    public String resolveDeviceName(String userAgentHeader) {
        if (userAgentHeader == null) {
            return "Unknown device";
        }

        Client client = parser.parse(userAgentHeader);
        String browser = client.userAgent.family;
        String os = client.os.family;
        String device = client.device.family;

        if (!device.equals("Other")) {
            return device + " (" + os + ")";
        }

        return browser + " on " + os;
    }
}

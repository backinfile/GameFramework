package com.backinfile.GameFramework;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogCore {
    public static Logger core = LoggerFactory.getLogger("core");
    public static Logger serialize = LoggerFactory.getLogger("core.serialize");
    public static Logger event = LoggerFactory.getLogger("core.event");
    public static Logger net = LoggerFactory.getLogger("core.net");
    public static Logger client = LoggerFactory.getLogger("core.client");
    public static Logger server = LoggerFactory.getLogger("core.server");
}
